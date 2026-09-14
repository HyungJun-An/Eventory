package com.eventory.payment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 결제 준비 요청 — 무엇을 몇 명 예매할지만 받는다.
 * 결제자(JWT)와 금액(가격 × 인원)은 서버가 정하므로 클라이언트에서 받지 않는다.
 */
@Getter
@Setter
public class ReadyRequest {
    @NotNull
    private Long expoId;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer people;
}
