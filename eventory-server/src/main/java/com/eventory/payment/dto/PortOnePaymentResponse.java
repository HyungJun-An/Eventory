package com.eventory.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * PortOne V2 결제 단건 조회 응답 (사용하는 필드만)
 * 예) {"status":"PAID","amount":{"total":15000},"method":{"type":"PaymentMethodEasyPay","provider":"TOSSPAY"}}
 */
@Data
public class PortOnePaymentResponse {
    private String id;          // paymentId
    private String status;      // PAID, VIRTUAL_ACCOUNT_ISSUED, CANCELLED ...
    private Amount amount;      // total 금액 비교 용
    private Method method;      // 결제수단 (V2: type + provider)

    @Data
    public static class Amount {
        private BigDecimal total;
    }

    @Data
    public static class Method {
        private String type;     // PaymentMethodCard | PaymentMethodEasyPay | PaymentMethodTransfer ...
        private String provider; // 간편결제일 때만: TOSSPAY, KAKAOPAY, NAVERPAY ...
    }
}
