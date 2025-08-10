package com.eventory.auth.service;

import com.eventory.auth.dto.AdminLoginRequest;
import com.eventory.auth.dto.LoginResponse;

public interface AdminAuthService {
    LoginResponse loginSystemAdmin(AdminLoginRequest request);
    LoginResponse loginExpoAdmin(AdminLoginRequest request);
}
