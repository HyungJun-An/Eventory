package com.eventory.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompleteResponse {
    private final Long paymentPk;
    private final Long reservationPk;
    private final String status; // PAID
    private final String reservationCode;
    private final Long reservationId; // 예약 id
    private final String portonePaymentId; // 포트원에서 사용하는 id
}
