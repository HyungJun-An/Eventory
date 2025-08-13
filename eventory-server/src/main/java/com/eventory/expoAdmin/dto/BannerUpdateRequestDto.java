package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class BannerUpdateRequestDto {

    @NotBlank
    private String imageUrl;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
