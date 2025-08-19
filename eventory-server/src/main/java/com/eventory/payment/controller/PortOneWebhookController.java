package com.eventory.payment.controller;

import com.eventory.common.entity.Payment;
import com.eventory.common.entity.PaymentStatus;
import com.eventory.common.repository.PaymentRepository;
//import com.eventory.common.repository.ReservationRepository;
import com.eventory.config.PortOneProperties;
import com.eventory.payment.dto.PortOnePaymentResponse;
import com.eventory.payment.service.PortOneClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/portone-webhook")
@RequiredArgsConstructor
public class PortOneWebhookController {
    private final PortOneProperties props;
    private final PortOneClient portOne;
    private final PaymentRepository paymentRepository;
//    private final ReservationRepository reservationRepository;

    // SDK 서명검증은 io.portone:server-sdk 사용. 간략화를 위해 아래는 바디만 전달/확인 예시로 작성.
    @PostMapping
    @Transactional
    public ResponseEntity<String> handle(@RequestBody String body, @RequestHeader Map<String, String> headers) {
        // 1) 서명 검증 (Server SDK 사용 권장)
        String signature = headers.getOrDefault("portone-webhook-signature", "");
        // PortOneServerSdk.verifySignature(props.getWebhookSecret(), body, signature);
        // ↑ 실제 구현: SDK의 검증 유틸 호출

        // 2) event 파싱 → paymentId 추출 (여기선 매우 단순화)
        // { "type":"PAYMENT_PAID", "data": { "paymentId":"payment-xxxxx" } }
        String paymentId = extractPaymentId(body);
        if (paymentId == null) return ResponseEntity.badRequest().body("missing paymentId");

        // 3) PortOne 단건조회로 상태 확정
        PortOnePaymentResponse pay = portOne.getPayment(paymentId).block();
        if (pay == null) return ResponseEntity.ok("ignored");
        if (!"PAID".equalsIgnoreCase(pay.getStatus())) return ResponseEntity.ok("ignored");

        // 4) 이미 처리한 결제는 무시 (idempotent). 여기서는 간단히 금액/시간 체크 후 저장 예시
        BigDecimal amount = pay.getAmount().getTotal();
        Payment saved = paymentRepository.save(Payment.builder()
                .amount(amount)
                .method(pay.getPaymentMethod() != null ? pay.getPaymentMethod().getMethod() : "UNKNOWN")
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build());

        // TODO: 우리 쪽 주문/예약과 매핑하여 Reservation 생성/갱신 필요
        return ResponseEntity.ok("ok");
    }

    private String extractPaymentId(String body) {
        int idx = body.indexOf("paymentId");
        if (idx < 0) return null;
        int start = body.indexOf(':', idx) + 1;
        int q1 = body.indexOf('"', start);
        int q2 = body.indexOf('"', q1 + 1);
        return (q1 > 0 && q2 > q1) ? body.substring(q1 + 1, q2) : null;
    }
}