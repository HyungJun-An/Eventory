package com.eventory.auth.controller;

import com.eventory.auth.dto.AdminLoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /** 시스템 관리자 로그인 */
    @PostMapping("/system/login")
    public ResponseEntity<LoginResponse> systemLogin(@RequestBody @Valid AdminLoginRequest req) {
        return ResponseEntity.ok(adminAuthService.loginSystemAdmin(req));
    }

    /** 박람회 관리자 로그인 */
    @PostMapping("/expo/login")
    public ResponseEntity<LoginResponse> expoLogin(@RequestBody @Valid AdminLoginRequest req) {
        return ResponseEntity.ok(adminAuthService.loginExpoAdmin(req));
    }
}