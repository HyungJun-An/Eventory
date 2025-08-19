package com.eventory.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequest {
    @NotNull
    private Long reservationId;

    @NotBlank
    private String reason;
}
