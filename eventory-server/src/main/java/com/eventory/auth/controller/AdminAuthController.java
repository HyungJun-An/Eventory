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

    /**
     * 시스템 관리자 userType=1(systemAdmin) 로그아웃
     * - Authorization: Bearer {accessToken}
     * - 토큰의 subject(userId)와 userType을 검증하고 블랙리스트 처리 + RefreshToken 제거
     */
    @PostMapping("/sys/logout")
    public ResponseEntity<Void> systemLogout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        String accessToken = authHeader.substring(7);
        adminAuthService.logoutSystemAdmin(accessToken);
        return ResponseEntity.ok().build();
    }

    /**
     * 시스템 관리자 userType=2(expoAdmin) 로그아웃
     * - Authorization: Bearer {accessToken}
     * - 토큰의 subject(userId) 검증하고 블랙리스트 처리 + RefreshToken 제거
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> expoLogout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        String accessToken = authHeader.substring(7);
        adminAuthService.logoutExpoAdmin(accessToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshHeader) {

        // 1) 우선순위: X-Refresh-Token (UUID)
        String refresh = StringUtils.hasText(refreshHeader) ? refreshHeader : null;

        if (refresh == null && StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            // Bearer <uuid> 도 허용 (이전 호환)
            refresh = auth.substring(7);
        }

        if (!StringUtils.hasText(refresh)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }
        return ResponseEntity.ok(adminAuthService.refreshAccessToken(refresh));
    }
}