package com.eventory.payment.service;

import com.eventory.auth.repository.UserRepository;
import com.eventory.common.entity.*;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.common.repository.PaymentRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.config.PortOneProperties;
import com.eventory.payment.dto.*;
import com.eventory.qr.service.QrService;
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
    private final QrService qrService;

    public ReadyResponse ready(ReadyRequest req, String baseRedirectUrl) {
        // 실제 서비스에서는 서버에서 금액 재계산하여 req.totalAmount와 비교/대체 권장
        // 총 길이 <= 32 (권장 30~32), 40 넘지 않게만 보장하면 됨
        final String paymentId = "p_" + UUID.randomUUID().toString().replace("-", "").substring(0, 30);
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

        // 2) payment 저장 (결제수단 표준화)
        String payMethodRaw = pay.getPaymentMethod() != null ? pay.getPaymentMethod().getMethod() : null; // e.g. CARD / TRANSFER / EASY_PAY ...
        String easyPayProvider = (pay.getEasyPay() != null) ? pay.getEasyPay().getProvider() : null; // e.g. KAKAOPAY / TOSSPAY / NAVERPAY ...
        String method = mapMethod(payMethodRaw, easyPayProvider);

        Payment savedPay = paymentRepository.save(Payment.builder()
                .amount(paid)
                .method(method)
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .portonePaymentId(req.getPaymentId()) // 포트원 자체 결제 id 저장
                .build());

        // ID만으로 프록시 참조 얻기 (즉시 쿼리 안 나감, 접근 시/flush 시 검증)
        User userRef = userRepository.getReferenceById(req.getUserId());
        Expo expoRef = expoRepository.getReferenceById(req.getExpoId());

        // 3) reservation 생성 (결제 성공 시점에만 생성)
        String reservationCode = generateReservationCode();
        Reservation savedRes = reservationRepository.save(Reservation.builder()
                .user(userRef)
                .expo(expoRef)
                .payment(savedPay)
                .status(ReservationStatus.RESERVED)
                .code(reservationCode)
                .people(req.getPeople())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // 4) QR 발급 & 메일 발송 트리거 → 별도 서비스에서 구현 예정
        qrService.issueAndSend(savedRes.getReservationId());

        return CompleteResponse.builder()
                .paymentPk(savedPay.getPaymentId())
                .reservationPk(savedRes.getReservationId())
                .status("PAID")
                .reservationCode(reservationCode)
                .reservationId(savedRes.getReservationId())   // 프론트에서 navigate할 때 사용
                .portonePaymentId(req.getPaymentId())         // PortOne 결제 건 ID (환불 시 사용)
                .build();
    }

    @Transactional
    public void refund(Long reservationId, String reason) {
        // 3-1) 예약/결제 로드
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음"));
        Payment pay = paymentRepository.findById(res.getPayment().getPaymentId())
                .orElseThrow(() -> new IllegalStateException("결제 없음"));

        if (pay.getStatus() == PaymentStatus.REFUNDED) {
            return; // 이미 환불된 결제는 멱등 처리
        }

        // 3-2) PortOne paymentId 확보(★ DB에 저장된 값 사용)
        String portonePaymentId = pay.getPortonePaymentId();
        if (portonePaymentId == null || portonePaymentId.isBlank())
            throw new IllegalStateException("portonePaymentId 미저장 — 결제 완료 저장 로직 확인 필요");

        // 3-3) 전액 환불(취소) 호출 — 부분 환불은 cancelAmount 조정하면 됨
        PortOneCancelRequest cancelReq = new PortOneCancelRequest(pay.getAmount(), reason);
        portOne.cancelPayment(portonePaymentId, cancelReq).block();

        // 3-4) 로컬 상태 반영
        pay.markRefunded();
        res.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(res);
        paymentRepository.save(pay);
    }

    private String generateReservationCode() {
        String date = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "RES-" + date + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * PortOne 응답(payMethod + easyPay.provider)을 우리 표기 문자열로 매핑
     * - 간편결제(EASY_PAY)는 provider 기준으로 세분화 저장(KakaoPay, TossPay 등)
     * - 일반수단은 표준 라벨로 통일하여 저장
     */
    private String mapMethod(String payMethod, String easyPayProvider) {
        String methodUpper = payMethod == null ? "" : payMethod.trim().toUpperCase();
        // 간편결제 분기: EASY_PAY + provider
        if ("EASY_PAY".equals(methodUpper)) {
            String provider = easyPayProvider == null ? "" : easyPayProvider.trim().toUpperCase();
            return switch (provider) {
                case "KAKAOPAY" -> "KakaoPay";
                case "TOSSPAY" -> "TossPay";
                case "NAVERPAY" -> "NaverPay";
                case "APPLEPAY" -> "ApplePay";
                case "SAMSUNGPAY" -> "SamsungPay";
                default -> "EasyPay"; // 알 수 없는 간편결제 제공사
            };
        }

        // 일반 결제수단 매핑
        return switch (methodUpper) {
            case "CARD" -> "Credit Card"; // 신용/체크카드
            case "TRANSFER" -> "Bank Transfer"; // 계좌이체
            case "VBANK", "VIRTUAL_ACCOUNT" -> "Virtual Account"; // 가상계좌
            case "PHONE" -> "Mobile Phone"; // 휴대폰 결제
            default -> "UNKNOWN"; // 마지막 안전망
        };
    }
}