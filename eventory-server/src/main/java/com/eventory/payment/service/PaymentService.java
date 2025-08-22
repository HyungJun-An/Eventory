package com.eventory.payment.service;

import com.eventory.payment.dto.CompleteRequest;
import com.eventory.payment.dto.CompleteResponse;
import com.eventory.payment.dto.ReadyRequest;
import com.eventory.payment.dto.ReadyResponse;

public interface PaymentService {
    ReadyResponse ready(ReadyRequest req, String baseRedirectUrl);
    CompleteResponse complete(CompleteRequest req);
    void refund(Long reservationId, String reason);
}
