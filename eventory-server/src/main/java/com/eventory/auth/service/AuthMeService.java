package com.eventory.auth.service;

import com.eventory.auth.dto.MeResponse;
import com.eventory.auth.security.CustomUserPrincipal;
import com.eventory.auth.security.JwtTokenProvider;
import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserRepository;
import com.eventory.common.repository.ExpoAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthMeService {
    private final UserRepository userRepository;
    private final ExpoAdminRepository expoAdminRepository;
    private final SystemAdminRepository systemAdminRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public MeResponse buildMe(CustomUserPrincipal principal, HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        String token = (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
        long remain = (token != null) ? jwtTokenProvider.getRemainingValidity(token) : 0L;

        return switch (principal.getRole()) {
            case "ROLE_GENERAL_USER" -> userRepository.findById(principal.getId())
                    .map(u -> MeResponse.builder()
                            .id(u.getUserId())
                            .loginId(u.getCustomerId())
                            .name(u.getName())
                            .email(u.getEmail())
                            .phone(u.getPhone())
                            .role(principal.getRole())
                            .tokenRemainingMs(remain)
                            .serverTime(Instant.now())
                            .authorities(principal.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toList()))
                            .build())
                    .orElseThrow(() -> new IllegalStateException("사용자 정보가 존재하지 않습니다."));

            case "ROLE_EXPO_ADMIN" -> expoAdminRepository.findById(principal.getId())
                    .map(a -> MeResponse.builder()
                            .id(a.getExpoAdminId())
                            .loginId(a.getCustomerId())
                            .name(a.getName())
                            .email(a.getEmail())
                            .phone(a.getPhone())
                            .role(principal.getRole())
                            .tokenRemainingMs(remain)
                            .serverTime(Instant.now())
                            .authorities(principal.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()))
                            .build())
                    .orElseThrow(() -> new IllegalStateException("박람회 관리자 정보가 존재하지 않습니다."));

            case "ROLE_SYSTEM_ADMIN" -> systemAdminRepository.findById(principal.getId())
                    .map(s -> MeResponse.builder()
                            .id(s.getSystemAdminId())
                            .loginId(s.getCustomerId())
                            .name(s.getName())
                            .email(s.getEmail())
                            .phone(s.getPhone())
                            .role(principal.getRole())
                            .tokenRemainingMs(remain)
                            .serverTime(Instant.now())
                            .authorities(principal.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()))
                            .build())
                    .orElseThrow(() -> new IllegalStateException("시스템 관리자 정보가 존재하지 않습니다."));

            default -> throw new IllegalStateException("알 수 없는 권한입니다: " + principal.getRole());
        };
    }
}