package com.eventory.payment.service;

import com.eventory.auth.repository.UserRepository;
import com.eventory.common.entity.*;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.common.repository.PaymentRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.config.PortOneProperties;
import com.eventory.payment.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PortOneProperties props;
    private final PortOneClient portOne;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ExpoRepository expoRepository;

    public ReadyResponse ready(ReadyRequest req, String baseRedirectUrl) {
        // 실제 서비스에서는 서버에서 금액 재계산하여 req.totalAmount와 비교/대체 권장
        final String paymentId = "payment-" + UUID.randomUUID();
        final String redirectUrl = baseRedirectUrl + "/payment/redirect"; // 프론트 라우트

        return ReadyResponse.builder()
                .paymentId(paymentId)
                .storeId(props.getStoreId())
                .channelKey(props.getChannelKey())
                .orderName(req.getOrderName())
                .totalAmount(req.getTotalAmount())
                .currency("CURRENCY_KRW")
                .payMethod("CARD")
                .redirectUrl(redirectUrl)
                .build();
    }

    @Transactional
    public CompleteResponse complete(CompleteRequest req) {
        // 1) PortOne 결제내역 단건조회
        PortOnePaymentResponse pay = portOne.getPayment(req.getPaymentId()).block();
        if (pay == null) throw new IllegalStateException("PortOne 응답이 비어있음");

        BigDecimal paid = pay.getAmount().getTotal();
        if (paid == null || req.getExpectedAmount().compareTo(paid) != 0) {
            throw new IllegalStateException("결제 금액 불일치 — 위변조 가능성");
        }
        if (!"PAID".equalsIgnoreCase(pay.getStatus())) {
            throw new IllegalStateException("결제 상태가 PAID가 아님: " + pay.getStatus());
        }

        // 2) payment 저장
        Payment savedPay = paymentRepository.save(Payment.builder()
                .amount(paid)
                .method(pay.getPaymentMethod() != null ? pay.getPaymentMethod().getMethod() : "UNKNOWN")
                .status(PaymentStatus.PAID)
//                .paidAt(OffsetDateTime.now().toLocalDateTime())
                .paidAt(LocalDateTime.now())
                .build());

        // ID만으로 프록시 참조 얻기 (즉시 쿼리 안 나감, 접근 시/flush 시 검증)
        User userRef = userRepository.getReferenceById(req.getUserId());
        Expo expoRef = expoRepository.getReferenceById(req.getExpoId());
//        Payment paymentRef = paymentRepository.getReferenceById(savedPay.getPaymentId());

        // 3) reservation 생성 (결제 성공 시점에만 생성)
        String reservationCode = generateReservationCode();
        Reservation savedRes = reservationRepository.save(Reservation.builder()
                .user(userRef)
                .expo(expoRef)
//                .payment(paymentRef)
                .payment(savedPay)
                .status(ReservationStatus.RESERVED)
                .code(reservationCode)
                .people(req.getPeople())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // 4) (다음 단계) QR 발급 & 메일 발송 트리거 → 별도 서비스에서 구현 예정
        // qrService.issueAndSend(savedRes.getId());

        return CompleteResponse.builder()
                .paymentPk(savedPay.getPaymentId())
                .reservationPk(savedRes.getReservationId())
                .status("PAID")
                .reservationCode(reservationCode)
                .build();
    }

    @Transactional
    public void refund(Long reservationId, String reason) {
        // 예약/결제 로드
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음"));
        Payment pay = paymentRepository.findById(res.getPayment().getPaymentId())
                .orElseThrow(() -> new IllegalStateException("결제 없음"));

        // PortOne 환불 호출 (전액 환불 예시)
        portOne.cancelPayment("payment-unknown-on-merchant", // 실제 paymentId를 별도로 저장/매핑해두는 것을 권장
                new PortOneCancelRequest(pay.getAmount(), reason)).block();

        pay.setStatus(PaymentStatus.REFUNDED);
        reservationRepository.save(res);
        paymentRepository.save(pay);
    }

    private String generateReservationCode() {
        String date = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "RES-" + date + "-" + UUID.randomUUID().toString().substring(0, 6);
    }
}