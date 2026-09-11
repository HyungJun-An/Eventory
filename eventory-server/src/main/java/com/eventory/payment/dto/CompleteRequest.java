package com.eventory.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 결제 완료 요청 — paymentId 만 받는다.
 * 박람회·인원·금액은 결제 준비 때 서버가 저장한 주문으로 검증한다 (기존: 클라이언트가 보낸 금액끼리 비교).
 */
@Getter
@Setter
public class CompleteRequest {
    @NotBlank
    private String paymentId;
}
