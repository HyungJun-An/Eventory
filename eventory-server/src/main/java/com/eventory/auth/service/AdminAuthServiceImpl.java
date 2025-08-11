package com.eventory.auth.service;

import com.eventory.auth.dto.AdminLoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.security.JwtTokenProvider;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.SystemAdmin;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final SystemAdminRepository systemAdminRepository;
    private final ExpoAdminRepository expoAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_KEY = "blacklist:access:";
    private static final String REFRESH_KEY_FMT = "refresh:%s:%s";

    @Override
    public LoginResponse loginSystemAdmin(AdminLoginRequest request) {
        SystemAdmin admin = systemAdminRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }
        // 필요 시 유효기간(3년) 검사 로직 추가
        String access = jwtTokenProvider.createAccessTokenWithRole(admin.getSystemAdminId(), "SYSTEM_ADMIN");
        String refresh = jwtTokenProvider.createRefreshToken();
        redisTemplate.opsForValue().set("refresh:sysadmin:" + admin.getSystemAdminId(), refresh, Duration.ofDays(7));
        return new LoginResponse(access, refresh);
    }

    @Override
    public LoginResponse loginExpoAdmin(AdminLoginRequest request) {
        ExpoAdmin admin = expoAdminRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }
        String access = jwtTokenProvider.createAccessTokenWithRole(admin.getExpoAdminId(), "EXPO_ADMIN");
        String refresh = jwtTokenProvider.createRefreshToken();
        redisTemplate.opsForValue().set("refresh:expoadmin:" + admin.getExpoAdminId(), refresh, Duration.ofDays(7));
        return new LoginResponse(access, refresh);
    }

    @Override
    public void logoutSystemAdmin(String accessToken) {
        logoutByType(accessToken, "1"); // 1 = systemAdmin
    }

    @Override
    public void logoutExpoAdmin(String accessToken) {
        logoutByType(accessToken, "2"); // 2 = expoAdmin
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
            redisTemplate.opsForValue().set(BLACKLIST_KEY + accessToken, "1", Duration.ofMillis(remainingMs));
        }

        String rtKey = String.format(REFRESH_KEY_FMT, userType, userId);
        redisTemplate.delete(rtKey);
    }
}
