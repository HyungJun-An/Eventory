package com.eventory.auth.service;

import com.eventory.auth.dto.LoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserTypeRepository;
import com.eventory.auth.security.JwtTokenProvider;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.SystemAdmin;
import com.eventory.common.entity.UserType;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoAdminRepository;
import com.eventory.common.repository.ExpoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final SystemAdminRepository systemAdminRepository;
    private final ExpoAdminRepository expoAdminRepository;
    private final UserTypeRepository userTypeRepository; // user_type 조회용 (role name 동기화)

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder; // BCrypt 주입!

    private static final String BLACKLIST_KEY = "blacklist:access:";
//    private static final String REFRESH_KEY_FMT = "refresh:%s:%s";

    private static final String USER_TYPE_SYSTEM_ADMIN = "1";
    private static final String USER_TYPE_EXPO_ADMIN = "2";

    @Value("${jwt.refresh-token-validity}")
    private long refreshTtlMs;
    private final ExpoRepository expoRepository;

    // 시스템 관리자 로그인
    @Override
    @Transactional
    public LoginResponse loginSystemAdmin(LoginRequest req) {
        SystemAdmin admin = systemAdminRepository.findByCustomerId(req.getCustomerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        // 비밀번호 검증 (BCrypt)
        validatePassword(req.getPassword(), admin.getPassword());

        // DB의 user_type을 기준으로 role 동기화 → 하드코딩 제거
        String roleName = getRoleNameByTypeId(admin.getType().getTypeId()); //  "systemAdmin"
        String authority = toAuthority(roleName);  //  "ROLE_SYSTEM_ADMIN"

        // 토큰 생성 (role 클레임 포함)
        String access = jwtTokenProvider.createAccessTokenWithRole(admin.getSystemAdminId(), authority, USER_TYPE_SYSTEM_ADMIN );
        String refresh = jwtTokenProvider.createRefreshToken(); // UUID 기반 생성

        // RefreshToken 저장 (userId 기준 키)
        saveRefreshToken(admin.getSystemAdminId(), refresh);

        return new LoginResponse(access, refresh);
    }

    // 박람회 관리자 로그인
    @Override
    @Transactional
    public LoginResponse loginExpoAdmin(LoginRequest req) {
        ExpoAdmin admin = expoAdminRepository.findByCustomerId(req.getCustomerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        validatePassword(req.getPassword(), admin.getPassword());

        // 유효기간 정책(회사 마지막 박람회 신청일 기준 3년) 체크 — 실제 로직은 도메인 서비스로 분리 권장
        if (!isWithin3YearsFromCompanyLastExpo(admin)) {
            throw new CustomException(CustomErrorCode.EXPO_ADMIN_EXPIRED); // 예: 403/401 적절히
        }

        String roleName = getRoleNameByTypeId(admin.getType().getTypeId());       //  "expoAdmin"
        String authority = toAuthority(roleName);                        //  "ROLE_EXPO_ADMIN"

        String access = jwtTokenProvider.createAccessTokenWithRole(admin.getExpoAdminId(), authority, USER_TYPE_EXPO_ADMIN);
        String refresh = jwtTokenProvider.createRefreshToken(); // UUID 기반 생성

        saveRefreshToken(admin.getExpoAdminId(), refresh);
        return new LoginResponse(access, refresh);
    }

    @Override
    public void logoutSystemAdmin(String accessToken) {
        logoutByType(accessToken, USER_TYPE_SYSTEM_ADMIN ); // 1 = systemAdmin
    }

    @Override
    public void logoutExpoAdmin(String accessToken) {
        logoutByType(accessToken, USER_TYPE_EXPO_ADMIN ); // 2 = expoAdmin
    }

    private void logoutByType(String accessToken, String requiredType) {
        Map<String, Object> claims = jwtTokenProvider.parseClaims(accessToken);
        String userType = String.valueOf(claims.getOrDefault("userType", ""));
        String userId = String.valueOf(claims.getOrDefault("sub", ""));

        if (!requiredType.equals(userType)) {
            throw new IllegalStateException("해당 관리자 타입 전용 로그아웃 API임");
        }

        long remainingMs = jwtTokenProvider.getRemainingValidity(accessToken);
        if (remainingMs > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_KEY + accessToken, USER_TYPE_SYSTEM_ADMIN, Duration.ofMillis(remainingMs));
        }

//        String rtKey = String.format(REFRESH_KEY_FMT, userType, userId);
//        redisTemplate.delete(rtKey);
        redisTemplate.delete(refreshKey(Long.valueOf(userId)));
    }

    // ===============================================================================
    // 토큰 재발급 (역할 동기화 전략 반영)
    @Transactional
    public LoginResponse refreshAccessToken(String refreshToken) {
        // 1) 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2) subject(userId) 추출
        Long userId = Long.parseLong(jwtTokenProvider.getSubject(refreshToken));

        // 3) Redis 검증
        String saved = redisTemplate.opsForValue().get(refreshKey(userId));
        if (!refreshToken.equals(saved)) {
            throw new CustomException(CustomErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 4) RefreshToken에서 role, userType 재사용
        String authority = jwtTokenProvider.getRoleFromToken(refreshToken);
        String userType = jwtTokenProvider.getUserTypeFromToken(refreshToken); // 새 메서드 필요

        // 5) 새 AccessToken 발급
        String newAccess = jwtTokenProvider.createAccessTokenWithRole(userId, authority, userType);
        return new LoginResponse(newAccess, refreshToken);
    }

    private void validatePassword(String raw, String encoded) {
        if (raw == null || !passwordEncoder.matches(raw, encoded)) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }
    }

    private String getRoleNameByTypeId(Long typeId) {
        return userTypeRepository.findById(typeId)
                .map(UserType::getName)         // "systemAdmin" | "expoAdmin" | ...
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_TYPE_NOT_FOUND));
    }

    private String toAuthority(String roleName) {
        // DB의 소문자/카멜케이스 네이밍을 권한 포맷으로 표준화
        return "ROLE_" + roleName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
        // systemAdmin → ROLE_SYSTEM_ADMIN, expoAdmin → ROLE_EXPO_ADMIN, generalUser → ROLE_GENERAL_USER
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(refreshKey(userId), refreshToken, refreshTtlMs, TimeUnit.MILLISECONDS);
    }

    private String refreshKey(Long userId) {
        return "refresh:" + userId;
    }

    private Long safeGetUserId(String accessToken) {
        try { return Long.parseLong(jwtTokenProvider.getSubject(accessToken)); }
        catch (Exception ignore) { return null; }
    }

    // 실제 회사 마지막 박람회 신청일 기준 3년 로직 구현
    private boolean isWithin3YearsFromCompanyLastExpo(ExpoAdmin admin) {
        // 예시: expoAdminRepository.findCompanyLastExpoDate(admin).plusYears(3).isAfter(now)
//        LocalDate lastAppliedDate = expoRepository.findLastAppliedDateByAdminId(admin.getExpoAdminId());
//        if (lastAppliedDate == null) return false; // 신청 이력이 없으면 유효기간 없음
        return true;
    }
}
