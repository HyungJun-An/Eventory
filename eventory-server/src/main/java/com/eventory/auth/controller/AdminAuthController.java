package com.eventory.auth.controller;

import com.eventory.auth.dto.LoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.service.AdminAuthService;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /** 시스템 관리자 로그인 */
    @PostMapping("/sys/login")
    public ResponseEntity<LoginResponse> systemLogin(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(adminAuthService.loginSystemAdmin(req));
    }

    /** 박람회 관리자 로그인 */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> expoLogin(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(adminAuthService.loginExpoAdmin(req));
    }

    /** 시스템 관리자 로그아웃 — AccessToken 차단 + RefreshToken 제거 */
    @PostMapping("/sys/logout")
    public ResponseEntity<Void> systemLogout(HttpServletRequest request) {
        String accessToken = bearerToken(request);
        if (accessToken == null) return ResponseEntity.badRequest().build();
        adminAuthService.logoutSystemAdmin(accessToken);
        return ResponseEntity.ok().build();
    }

    /** 박람회 관리자 로그아웃 — AccessToken 차단 + RefreshToken 제거 */
    @PostMapping("/logout")
    public ResponseEntity<Void> expoLogout(HttpServletRequest request) {
        String accessToken = bearerToken(request);
        if (accessToken == null) return ResponseEntity.badRequest().build();
        adminAuthService.logoutExpoAdmin(accessToken);
        return ResponseEntity.ok().build();
    }

    /** 박람회 관리자 토큰 재발급 (박람회관리자 리프레시 토큰만 허용) */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshHeader) {
        return ResponseEntity.ok(adminAuthService.refreshExpoAdmin(refreshToken(auth, refreshHeader)));
    }

    /** 시스템 관리자 토큰 재발급 (시스템관리자 리프레시 토큰만 허용) — 기존에는 경로가 없어 15분 뒤 강제 로그아웃 */
    @PostMapping("/sys/refresh")
    public ResponseEntity<LoginResponse> systemRefresh(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshHeader) {
        return ResponseEntity.ok(adminAuthService.refreshSystemAdmin(refreshToken(auth, refreshHeader)));
    }

    /** 우선순위: X-Refresh-Token 헤더 → Authorization: Bearer {uuid} (이전 호환) */
    private String refreshToken(String auth, String refreshHeader) {
        String refresh = StringUtils.hasText(refreshHeader) ? refreshHeader : null;
        if (refresh == null && StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            refresh = auth.substring(7);
        }
        if (!StringUtils.hasText(refresh)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }
        return refresh;
    }

    private String bearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (StringUtils.hasText(header) && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }
}
