package com.eventory.expoAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YearlySalesResponseDto {
    private Integer year;
    private BigDecimal totalAmount;
}
