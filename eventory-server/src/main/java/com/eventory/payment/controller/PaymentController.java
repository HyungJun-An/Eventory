package com.eventory.payment.controller;

import com.eventory.payment.dto.CompleteRequest;
import com.eventory.payment.dto.CompleteResponse;
import com.eventory.payment.dto.ReadyRequest;
import com.eventory.payment.dto.ReadyResponse;
import com.eventory.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // 1) 결제 준비 — paymentId, storeId/channelKey 반환
    @PostMapping("/ready")
    public ResponseEntity<ReadyResponse> ready(@RequestBody @Valid ReadyRequest req, HttpServletRequest servletReq) {
        String base = servletReq.getScheme() + "://" + servletReq.getServerName()
                + (servletReq.getServerPort() == 80 || servletReq.getServerPort() == 443 ? "" : ":" + servletReq.getServerPort());
        return ResponseEntity.ok(paymentService.ready(req, base));
    }

    // 2) 결제 완료 통지(브라우저/redirect 후 서버 확인)
    @PostMapping("/complete")
    public ResponseEntity<CompleteResponse> complete(@RequestBody @Valid CompleteRequest req) {
        return ResponseEntity.ok(paymentService.complete(req));
    }
}