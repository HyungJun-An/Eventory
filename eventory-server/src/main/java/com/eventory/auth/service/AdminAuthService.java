package com.eventory.auth.service;

import com.eventory.auth.dto.LoginRequest;
import com.eventory.auth.dto.LoginResponse;

public interface AdminAuthService {
    LoginResponse loginSystemAdmin(LoginRequest request);
    LoginResponse loginExpoAdmin(LoginRequest request);
    void logoutSystemAdmin(String accessToken);
    void logoutExpoAdmin(String accessToken);
    LoginResponse refreshAccessToken(String refreshToken);
}
