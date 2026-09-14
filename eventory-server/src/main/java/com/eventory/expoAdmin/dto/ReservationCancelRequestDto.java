package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 관리자 예약 취소 요청 (취소 사유는 결제사 환불 사유로도 전달된다) */
public record ReservationCancelRequestDto(
        @NotBlank(message = "취소 사유를 입력해주세요.")
        @Size(max = 200)
        String reason
) {
}
