package com.eventory.expoAdmin.dto;

import com.eventory.common.entity.BoothStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BoothRequestDto {

    @NotNull
    private BoothStatus status;

    @NotBlank
    private String reason;
}
