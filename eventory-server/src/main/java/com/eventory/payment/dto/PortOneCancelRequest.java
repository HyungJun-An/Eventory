package com.eventory.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/** 일부 필드만 사용하는 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortOneCancelRequest {
    private BigDecimal cancelAmount; // 전액 환불 시 total 금액
    private String reason;           // 환불 사유
}