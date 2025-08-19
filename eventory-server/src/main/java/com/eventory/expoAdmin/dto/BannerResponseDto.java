package com.eventory.expoAdmin.dto;

import com.eventory.common.entity.BannerStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BannerResponseDto {

    @NotNull
    @Positive
    private Long bannerId;

    @NotNull
    @Positive
    private Long expoId;

    @NotNull
    @Positive
    private Long paymentId;

    @NotBlank
    private String imageUrl;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private BannerStatus status;

    @NotBlank
    private String reason;
}
