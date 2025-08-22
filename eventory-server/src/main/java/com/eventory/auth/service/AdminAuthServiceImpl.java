package com.eventory.auth.service;

import com.eventory.auth.dto.LoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserTypeRepository;
import com.eventory.auth.security.JwtTokenProvider;
import com.eventory.auth.tokenStore.RedisTokenStore;
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
import java.util.UUID;
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
    private final RedisTokenStore tokenStore;

    private static final String BLACKLIST_KEY = "blacklist:access:";
//    private static final String REFRESH_KEY_FMT = "refresh:%s:%s";

//    private static final String USER_TYPE_SYSTEM_ADMIN = "1";
//    private static final String USER_TYPE_EXPO_ADMIN = "2";

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
        if (!passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }

        String roleName = getRoleNameByTypeId(admin.getType().getTypeId()); //  "systemAdmin"
        String authority = toAuthority(roleName);  //  "ROLE_SYSTEM_ADMIN"

        String access = jwtTokenProvider.createAccessToken(admin.getSystemAdminId(), authority);
        String refresh = UUID.randomUUID().toString(); // UUID 기반 생성

        // RefreshToken 저장 (userId 기준 키)
        tokenStore.saveRefreshToken(admin.getSystemAdminId(), refresh, Duration.ofDays(7).toMillis());

        return new LoginResponse(access, refresh);
    }

    // 박람회 관리자 로그인
    @Override
    @Transactional
    public LoginResponse loginExpoAdmin(LoginRequest req) {
        ExpoAdmin admin = expoAdminRepository.findByCustomerId(req.getCustomerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        // 비밀번호 검증 (BCrypt)
        if (!passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }

        // 유효기간 정책(회사 마지막 박람회 신청일 기준 3년) 체크 — 실제 로직은 도메인 서비스로 분리 권장
        if (!isWithin3YearsFromCompanyLastExpo(admin)) {
            throw new CustomException(CustomErrorCode.EXPO_ADMIN_EXPIRED); // 예: 403/401 적절히
        }

        String roleName = getRoleNameByTypeId(admin.getType().getTypeId());       //  "expoAdmin"
        String authority = toAuthority(roleName);                        //  "ROLE_EXPO_ADMIN"

        String access = jwtTokenProvider.createAccessTokenWithRole(admin.getExpoAdminId(), authority);
        String refresh = UUID.randomUUID().toString(); // UUID 기반 생성

        // RefreshToken 저장 (userId 기준 키)
        tokenStore.saveRefreshToken(admin.getExpoAdminId(), refresh, Duration.ofDays(7).toMillis());

        return new LoginResponse(access, refresh);
    }

    @Override
    public void logoutSystemAdmin(String accessToken) {
        logoutByType(accessToken, "ROLE_SYSTEM_ADMIN" ); // 1 = systemAdmin
    }

    @Override
    public void logoutExpoAdmin(String accessToken) {
        logoutByType(accessToken, "ROLE_EXPO_ADMIN" ); // 2 = expoAdmin
    }

    private void logoutByType(String accessToken, String requiredType) {
        Map<String, Object> claims = jwtTokenProvider.parseClaims(accessToken);
        String role = String.valueOf(claims.getOrDefault("role", ""));
        String userId = String.valueOf(claims.getOrDefault("sub", ""));

        if (!requiredType.equals(role)) {
            throw new IllegalStateException("해당 관리자 권한 전용 로그아웃 API임");
        }

        // ② TTL이 0이어도 최소 60초 블랙리스트 등록(운영상 안전)
        long remainingMs = jwtTokenProvider.getRemainingValidity(accessToken);
        long blacklistTtl = remainingMs > 0 ? remainingMs : 60_000L;

        try {
            redisTemplate.opsForValue().set(BLACKLIST_KEY + accessToken, role, Duration.ofMillis(remainingMs));
        } finally {
            if (userId != null) {
                // ② 저장/삭제 통일: tokenStore로 삭제 (역인덱스까지 함께 정리되는 구현을 권장)
                redisTemplate.delete(refreshKey(Long.valueOf(userId)));
            }
        }
    }

    // ===============================================================================
    // 토큰 재발급 (역할 동기화 전략 반영)
    @Override
    @Transactional
    public LoginResponse refreshAccessToken(String refreshToken) {
        if (!org.springframework.util.StringUtils.hasText(refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 1) refresh(UUID) → userId 역인덱스 확인 (Redis)
        Long userId = tokenStore.findUserIdByRefresh(refreshToken);
        if (userId == null) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }
        String saved = tokenStore.findRefreshToken(userId);
        if (!refreshToken.equals(saved)) {
            throw new CustomException(CustomErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 2) 관리자 타입 판별 (SystemAdmin 우선 확인 → 없으면 ExpoAdmin)
        String authority;
        String userType;

        SystemAdmin sys = systemAdminRepository.findById(userId).orElse(null);
        if (sys != null) {
            String roleName = getRoleNameByTypeId(sys.getType().getTypeId()); // e.g. "systemAdmin"
            authority = toAuthority(roleName);                                 // e.g. "ROLE_SYSTEM_ADMIN"
        } else {
            ExpoAdmin expo = expoAdminRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            if (!isWithin3YearsFromCompanyLastExpo(expo)) {
                throw new CustomException(CustomErrorCode.EXPO_ADMIN_EXPIRED);
            }
            String roleName = getRoleNameByTypeId(expo.getType().getTypeId()); // e.g. "expoAdmin"
            authority = toAuthority(roleName);                                  // e.g. "ROLE_EXPO_ADMIN"
        }

        // 3) 역할/타입 동기화된 새 AccessToken 발급 (userType 클레임 포함)
        String newAccess = jwtTokenProvider.createAccessTokenWithRole(userId, authority);

        // 4) RefreshToken은 그대로 재사용 (회전 안 함)
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
