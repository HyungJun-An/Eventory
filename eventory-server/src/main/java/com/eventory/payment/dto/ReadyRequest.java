package com.eventory.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@Getter
@Setter
public class ReadyRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long expoId;

    @NotNull @Positive
    private Integer people;

    @NotBlank
    private String orderName;

    @NotNull @Positive
    private BigDecimal totalAmount; // 서버에서도 최종 재계산 권장
}
