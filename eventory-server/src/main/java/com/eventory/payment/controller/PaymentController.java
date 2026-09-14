package com.eventory.payment.controller;

import com.eventory.auth.security.CustomUserPrincipal;
import com.eventory.payment.dto.CompleteRequest;
import com.eventory.payment.dto.CompleteResponse;
import com.eventory.payment.dto.PaymentChannelInfo;
import com.eventory.payment.dto.ReadyRequest;
import com.eventory.payment.dto.ReadyResponse;
import com.eventory.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 API — 로그인한 참관객·참가업체만 호출 가능 (SecurityConfig).
 * 결제자는 요청 본문이 아니라 JWT 로 식별한다 (기존: body 의 userId 를 그대로 믿어 타인 명의 결제 가능).
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // 현재 결제 채널(결제수단) — 결제 화면 표시용
    @GetMapping("/channel")
    public ResponseEntity<PaymentChannelInfo> channel() {
        return ResponseEntity.ok(paymentService.currentChannel());
    }

    // 1) 결제 준비 — 서버가 금액을 계산하고 결제창 파라미터(채널 전략)를 돌려준다
    @PostMapping("/ready")
    public ResponseEntity<ReadyResponse> ready(@AuthenticationPrincipal CustomUserPrincipal user,
                                               @RequestBody @Valid ReadyRequest req) {
        return ResponseEntity.ok(paymentService.ready(user.getId(), req));
    }

    // 2) 결제 완료 — PortOne 실제 결제와 주문을 대조한 뒤 예약 확정 (PC 응답·모바일 리디렉션 공통)
    @PostMapping("/complete")
    public ResponseEntity<CompleteResponse> complete(@AuthenticationPrincipal CustomUserPrincipal user,
                                                     @RequestBody @Valid CompleteRequest req) {
        return ResponseEntity.ok(paymentService.complete(user.getId(), req.getPaymentId()));
    }

    // 3) 본인 예약 환불 — 입장 전 전액 환불
    @PostMapping("/{reservationId}/refund")
    public ResponseEntity<Void> refund(@AuthenticationPrincipal CustomUserPrincipal user,
                                       @PathVariable Long reservationId,
                                       @RequestBody @Valid RefundBody body) {
        paymentService.refundByUser(user.getId(), reservationId, body.getReason());
        return ResponseEntity.ok().build();
    }

    @Getter
    public static class RefundBody {
        @NotBlank(message = "환불 사유를 입력해주세요.")
        @Size(max = 200)
        private String reason;
    }
}
