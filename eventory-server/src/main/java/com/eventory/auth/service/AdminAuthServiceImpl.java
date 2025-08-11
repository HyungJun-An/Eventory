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

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final SystemAdminRepository systemAdminRepository;
    private final ExpoAdminRepository expoAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

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
}
