package com.eventory.auth.service;

import com.eventory.auth.dto.AdminLoginRequest;
import com.eventory.auth.dto.LoginResponse;

public interface AdminAuthService {
    LoginResponse loginSystemAdmin(AdminLoginRequest request);
    LoginResponse loginExpoAdmin(AdminLoginRequest request);
    void logoutSystemAdmin(String accessToken);
    void logoutExpoAdmin(String accessToken);
}
