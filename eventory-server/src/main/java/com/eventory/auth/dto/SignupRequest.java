package com.eventory.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SignupRequest {
    @NotBlank
    private String customerId;

    @NotBlank
    private String name;

    @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Long typeId;

    @NotNull
    private LocalDate birth;

    @NotBlank
    private String gender;

    @NotBlank
    private String phone;

    // 참가 업체 전용 필드 (typeId로 구분)
    private String companyNameKr;
    private String companyNameEng;
    private String ceoNameKr;
    private String ceoNameEng;
    private String companyAddress;
    private String registrationNum;
}
