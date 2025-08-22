package com.eventory.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CompleteRequest {
    @NotBlank
    private String paymentId; // 프론트에서 전달

    @NotNull
    private Long userId;

    @NotNull
    private Long expoId;

    @NotNull
    @Positive
    private Integer people;

    @NotBlank
    private String orderName;

    @NotNull
    @Positive
    private BigDecimal expectedAmount; // 서버 검증용

    private Long reservationId;

    private String portonePaymentId;
}
