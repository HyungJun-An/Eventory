package com.eventory.auth.service;

import com.eventory.auth.dto.LoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserTypeRepository;
import com.eventory.auth.security.JwtTokenProvider;
import com.eventory.auth.tokenStore.AccountType;
import com.eventory.auth.tokenStore.RefreshTokenOwner;
import com.eventory.auth.tokenStore.TokenStore;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.SystemAdmin;
import com.eventory.common.entity.UserType;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoAdminRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

/**
 * 관리자(박람회관리자·시스템관리자) 인증.
 * 리프레시 토큰은 계정 종류와 함께 저장하고, 재발급 API는 자기 종류의 토큰만 받는다.
 * (기존: id 만 저장하고 재발급 시 system_admin 테이블부터 조회 → 참관객 토큰으로 시스템관리자 토큰 발급 가능)
 */
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final long REFRESH_TTL_MS = Duration.ofDays(7).toMillis();
    private static final long MIN_BLACKLIST_TTL_MS = 60_000L;

    private final SystemAdminRepository systemAdminRepository;
    private final ExpoAdminRepository expoAdminRepository;
    private final UserTypeRepository userTypeRepository; // user_type 조회용 (role name 동기화)
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;

    // 시스템 관리자 로그인
    @Override
    public LoginResponse loginSystemAdmin(LoginRequest req) {
        SystemAdmin admin = systemAdminRepository.findByCustomerId(req.getCustomerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        validatePassword(req.getPassword(), admin.getPassword());

        String authority = toAuthority(getRoleNameByTypeId(admin.getType().getTypeId())); // ROLE_SYSTEM_ADMIN
        return issueTokens(AccountType.SYSTEM_ADMIN, admin.getSystemAdminId(), authority);
    }

    // 박람회 관리자 로그인
    @Override
    public LoginResponse loginExpoAdmin(LoginRequest req) {
        ExpoAdmin admin = expoAdminRepository.findByCustomerId(req.getCustomerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        validatePassword(req.getPassword(), admin.getPassword());

        // 유효기간 정책(회사 마지막 박람회 신청일 기준 3년) 체크 — 실제 로직은 도메인 서비스로 분리 권장
        if (!isWithin3YearsFromCompanyLastExpo(admin)) {
            throw new CustomException(CustomErrorCode.EXPO_ADMIN_EXPIRED);
        }
        String authority = toAuthority(getRoleNameByTypeId(admin.getType().getTypeId())); // ROLE_EXPO_ADMIN
        return issueTokens(AccountType.EXPO_ADMIN, admin.getExpoAdminId(), authority);
    }

    @Override
    public void logoutSystemAdmin(String accessToken) {
        logout(accessToken, "ROLE_SYSTEM_ADMIN", AccountType.SYSTEM_ADMIN);
    }

    @Override
    public void logoutExpoAdmin(String accessToken) {
        logout(accessToken, "ROLE_EXPO_ADMIN", AccountType.EXPO_ADMIN);
    }

    // 박람회관리자 토큰 재발급 — 박람회관리자 리프레시 토큰만 허용
    @Override
    public LoginResponse refreshExpoAdmin(String refreshToken) {
        RefreshTokenOwner owner = requireOwner(refreshToken, AccountType.EXPO_ADMIN);
        ExpoAdmin admin = expoAdminRepository.findById(owner.id())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        if (!isWithin3YearsFromCompanyLastExpo(admin)) {
            throw new CustomException(CustomErrorCode.EXPO_ADMIN_EXPIRED);
        }
        return issueTokens(AccountType.EXPO_ADMIN, admin.getExpoAdminId(),
                toAuthority(getRoleNameByTypeId(admin.getType().getTypeId())));
    }

    // 시스템관리자 토큰 재발급 — 시스템관리자 리프레시 토큰만 허용
    @Override
    public LoginResponse refreshSystemAdmin(String refreshToken) {
        RefreshTokenOwner owner = requireOwner(refreshToken, AccountType.SYSTEM_ADMIN);
        SystemAdmin admin = systemAdminRepository.findById(owner.id())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        return issueTokens(AccountType.SYSTEM_ADMIN, admin.getSystemAdminId(),
                toAuthority(getRoleNameByTypeId(admin.getType().getTypeId())));
    }

    /** AccessToken 발급 + RefreshToken 회전 (이전 리프레시 토큰은 폐기) */
    private LoginResponse issueTokens(AccountType type, Long id, String authority) {
        String access = jwtTokenProvider.createAccessTokenWithRole(id, authority);
        String refresh = UUID.randomUUID().toString();
        tokenStore.saveRefreshToken(type, id, refresh, REFRESH_TTL_MS);
        return new LoginResponse(access, refresh);
    }

    private RefreshTokenOwner requireOwner(String refreshToken, AccountType expected) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }
        return tokenStore.findOwner(refreshToken)
                .filter(owner -> owner.type() == expected)
                .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN));
    }

    private void logout(String accessToken, String requiredRole, AccountType type) {
        Claims claims = jwtTokenProvider.parseClaims(accessToken);
        if (!requiredRole.equals(String.valueOf(claims.get("role")))) {
            throw new CustomException(CustomErrorCode.ACCESS_DENIED); // 해당 관리자 종류 전용 로그아웃 API
        }
        // 남은 유효시간만큼 AccessToken 차단 (이미 만료됐어도 최소 60초)
        long remaining = jwtTokenProvider.getRemainingValidity(accessToken);
        tokenStore.blacklistAccessToken(accessToken, remaining > 0 ? remaining : MIN_BLACKLIST_TTL_MS);
        tokenStore.deleteRefreshToken(type, Long.valueOf(claims.getSubject()));
    }

    private void validatePassword(String raw, String encoded) {
        if (raw == null || !passwordEncoder.matches(raw, encoded)) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }
    }

    private String getRoleNameByTypeId(Long typeId) {
        return userTypeRepository.findById(typeId)
                .map(UserType::getName)
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_TYPE_NOT_FOUND));
    }

    private String toAuthority(String roleName) {
        // DB의 소문자/카멜케이스 네이밍을 권한 포맷으로 표준화 (SYSTEM_ADMIN → ROLE_SYSTEM_ADMIN)
        return "ROLE_" + roleName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

    // 실제 회사 마지막 박람회 신청일 기준 3년 로직 구현 예정
    private boolean isWithin3YearsFromCompanyLastExpo(ExpoAdmin admin) {
        return true;
    }
}
