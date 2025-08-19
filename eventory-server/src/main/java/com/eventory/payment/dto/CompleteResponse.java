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
}
