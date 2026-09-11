package com.eventory.payment.order;

import java.math.BigDecimal;

/**
 * 결제 준비(ready) 시 서버가 확정한 주문 정보.
 * 결제 완료(complete) 때 클라이언트가 보낸 값 대신 이 값으로 금액·사용자·박람회를 검증한다.
 */
public record PendingPayment(String paymentId, Long userId, Long expoId, int people, BigDecimal amount, String orderName) {
}
