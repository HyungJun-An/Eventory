package com.eventory.payment.event;

/** 결제 완료로 예약이 확정됨 (트랜잭션 커밋 후 QR 메일 발송에 사용) */
public record ReservationPaidEvent(Long reservationId) {
}
