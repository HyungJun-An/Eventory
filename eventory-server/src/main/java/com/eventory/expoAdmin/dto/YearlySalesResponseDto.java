package com.eventory.expoAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class YearlySalesResponseDto {
    private Integer year;
    private BigDecimal totalAmount;
}
