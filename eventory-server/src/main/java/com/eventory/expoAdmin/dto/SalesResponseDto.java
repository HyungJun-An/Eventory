package com.eventory.expoAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SalesResponseDto {
    private Long expoId;
    private Long viewCount;
    private Long reservationCount;
    private BigDecimal paymentTotal;
    private Long refundCount;
}
