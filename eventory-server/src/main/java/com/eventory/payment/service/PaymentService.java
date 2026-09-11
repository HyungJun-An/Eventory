package com.eventory.payment.service;

import com.eventory.payment.dto.CompleteResponse;
import com.eventory.payment.dto.PaymentChannelInfo;
import com.eventory.payment.dto.ReadyRequest;
import com.eventory.payment.dto.ReadyResponse;

public interface PaymentService {

    PaymentChannelInfo currentChannel();

    ReadyResponse ready(Long userId, ReadyRequest req);

    CompleteResponse complete(Long userId, String paymentId);

    /** 사용자 본인 예약 환불 (소유자·입장 여부 확인) */
    void refundByUser(Long userId, Long reservationId, String reason);

    /** 전액 환불 — 권한 확인은 호출하는 쪽(관리자 서비스 등) 책임 */
    void refund(Long reservationId, String reason);
}
