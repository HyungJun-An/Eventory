package com.eventory.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {
    @NotBlank
    private String customerId;

    @NotBlank
    private String password;
}
