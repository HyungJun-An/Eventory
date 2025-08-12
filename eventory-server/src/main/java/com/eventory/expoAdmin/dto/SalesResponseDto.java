package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SalesResponseDto {

    @NotNull
    @Positive
    private Long expoId;

    @NotNull
    @PositiveOrZero
    private Long viewCount;

    @NotNull
    @PositiveOrZero
    private Long reservationCount;

    @NotNull
    @PositiveOrZero
    private BigDecimal paymentTotal;

    @NotNull
    @PositiveOrZero
    private Long refundCount;
}
