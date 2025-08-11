package com.eventory.auth.controller;

import com.eventory.auth.dto.LoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.dto.SignupRequest;
import com.eventory.auth.dto.SignupResponse;
import com.eventory.auth.service.AuthService;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자 회원가입 요청을 처리
     * @param request 회원가입 정보
     * @return 가입 성공 메시지 또는 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody @Valid SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    /**
     * 사용자 로그인 요청을 처리
     * @param request 로그인 요청 정보 (아이디, 비밀번호)
     * @return 액세스 토큰, 리프레시 토큰 등의 로그인 응답
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /** 리프레시 토큰 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 "Bearer " 이후의 토큰 값 추출
        String authHeader = request.getHeader("Authorization");
        String refreshToken = (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) // "Bearer " 다음부터 토큰 문자열 잘라내기
                : null;
        // 2. RefreshToken 값이 없으면 예외 처리
        if (!StringUtils.hasText(refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN); // 잘못된 요청 예외
        }
        // 3. AuthService를 통해 새로운 AccessToken 발급
        LoginResponse newTokens = authService.refreshAccessToken(refreshToken);
        // 4. 발급된 토큰 응답 반환
        return ResponseEntity.ok(newTokens);
    }

    /** 로그아웃 */
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("⭐authHeader : " + authHeader);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new CustomException(CustomErrorCode.INVALID_ACCESS_TOKEN);
        }
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.ok().build();
    }
}