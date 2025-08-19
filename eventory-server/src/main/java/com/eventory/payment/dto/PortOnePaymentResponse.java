package com.eventory.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

/** 일부 필드만 사용하는 DTO */
@Data
public class PortOnePaymentResponse {
    private String id;          // paymentId
    private String status;      // PAID, VIRTUAL_ACCOUNT_ISSUED, CANCELLED ...
    private Amount amount;      // total 금액 비교 용
    private PaymentMethod paymentMethod; // 결제수단 표시

    @Data
    public static class Amount {
        private BigDecimal total;
    }

    @Data public static class PaymentMethod {
        private String method; /* e.g. CARD */
    }
}