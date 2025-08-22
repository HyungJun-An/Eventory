package com.eventory.companyUser.dto;

import com.eventory.common.entity.BoothStatus;
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
public class BoothResponseDto {

    @NotNull
    @Positive
    private Long boothId;

    @NotNull
    @Positive
    private Long expoId;

    @NotNull
    @Positive
    private Long userId;

    private Long paymentId;

    @NotBlank
    private String title;

    @NotBlank
    private String imageUrl;

    @NotBlank
    private String location;

    @NotBlank
    private String managerName;

    @NotBlank
    private String department;

    @NotBlank
    private String phone;

    @NotBlank
    private String email;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @NotNull
    private BoothStatus status;

    private String reason;
}
