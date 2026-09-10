package com.eventory.companyUser.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserResponseDto {

    @NotNull
    @Positive
    private Long userId;

    // userType(지연 로딩 엔티티)는 직렬화 오류(500)를, password 는 해시 노출을 일으켜 응답에서 제외한다
    @NotBlank
    private String customerId;

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String companyNameKr;

    @NotBlank
    private String companyNameEng;

    @NotBlank
    private String ceoNameKr;

    @NotBlank
    private String ceoNameEng;

    @NotBlank
    private String companyAddress;

    @NotBlank
    private String registrationNum;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
