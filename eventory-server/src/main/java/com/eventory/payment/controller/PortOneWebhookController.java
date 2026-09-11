package com.eventory.payment.controller;

import com.eventory.payment.service.PaymentService;
import com.eventory.payment.webhook.PortOneWebhookEvent;
import com.eventory.payment.webhook.PortOneWebhookVerifier;
import com.eventory.payment.webhook.PortOneWebhookVerifier.InvalidWebhookException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * PortOne 웹훅 수신 — 사용자가 결제 직후 브라우저를 닫아 결제 완료 요청(/api/payment/complete)이
 * 오지 않아도 예약이 확정되도록 하는 보조 경로.
 * <p>
 * 기존 문제
 * - 서명 검증이 주석 처리 → 누구나 호출 가능
 * - 호출될 때마다 예약과 연결되지 않은 PAID 결제를 새로 저장 → 중복 레코드, 플랫폼 매출 통계 부풀림
 */
@Slf4j
@RestController
@RequestMapping("/api/portone-webhook")
@RequiredArgsConstructor
public class PortOneWebhookController {

    private final PortOneWebhookVerifier verifier;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<String> handle(@RequestBody String body,
                                         @RequestHeader(value = "webhook-id", required = false) String webhookId,
                                         @RequestHeader(value = "webhook-timestamp", required = false) String timestamp,
                                         @RequestHeader(value = "webhook-signature", required = false) String signature) {
        // 1) 서명 검증 — 원문 body 그대로 검증해야 하므로 DTO 가 아닌 String 으로 받는다
        verifier.verify(body, webhookId, timestamp, signature);

        PortOneWebhookEvent event = parse(body);
        if (event == null || !StringUtils.hasText(event.paymentId())) {
            return ResponseEntity.badRequest().body("missing paymentId");
        }
        // 2) 결제 완료 이벤트만 처리 (Ready·Failed 등은 확인 응답만)
        if (!event.isPaid()) {
            log.info("[Webhook] 처리 대상 아님 type={} paymentId={}", event.type(), event.paymentId());
            return ResponseEntity.ok("ignored");
        }
        // 3) 결제 완료 로직 재사용 — 이미 처리된 결제면 아무것도 하지 않는다 (멱등)
        paymentService.completeByWebhook(event.paymentId());
        return ResponseEntity.ok("ok");
    }

    private PortOneWebhookEvent parse(String body) {
        try {
            return objectMapper.readValue(body, PortOneWebhookEvent.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @ExceptionHandler(InvalidWebhookException.class)
    public ResponseEntity<String> invalidWebhook(InvalidWebhookException e) {
        log.warn("[Webhook] 검증 실패로 거부: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature");
    }
}
