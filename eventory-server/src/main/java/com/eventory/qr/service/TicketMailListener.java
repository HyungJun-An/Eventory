package com.eventory.qr.service;

import com.eventory.payment.event.ReservationPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 예약 확정 후 QR 입장권 메일 발송.
 * - AFTER_COMMIT: 예약이 실제로 저장된 뒤에만 발송 (롤백된 예약에 메일이 가지 않음)
 * - @Async: 메일 서버가 느리거나 실패해도 결제 응답과 예약에는 영향이 없음
 *   (기존에는 결제 트랜잭션 안에서 발송해, 메일 실패 시 결제는 승인된 채 예약만 롤백됐다)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketMailListener {

    private final QrService qrService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationPaid(ReservationPaidEvent event) {
        try {
            qrService.sendTicketMail(event.reservationId());
        } catch (Exception e) {
            // 메일은 재발송이 가능하므로 결제 흐름을 막지 않고 기록만 남긴다
            log.error("[TicketMail] QR 메일 발송 실패 reservationId={}", event.reservationId(), e);
        }
    }
}
