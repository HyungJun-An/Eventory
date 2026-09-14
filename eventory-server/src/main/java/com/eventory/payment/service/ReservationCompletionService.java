package com.eventory.payment.service;

import com.eventory.auth.repository.UserRepository;
import com.eventory.common.entity.*;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.common.repository.PaymentRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.payment.dto.CompleteResponse;
import com.eventory.payment.dto.PortOnePaymentResponse;
import com.eventory.payment.event.ReservationPaidEvent;
import com.eventory.payment.order.PendingPayment;
import com.eventory.qr.service.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 결제 검증이 끝난 주문을 예약으로 확정하는 단일 트랜잭션.
 * PaymentServiceImpl 과 분리한 이유: 여기서 예외가 나면 DB 는 전부 롤백되고,
 * 호출한 쪽(트랜잭션 밖)이 PG 결제를 자동 취소할 수 있어야 하기 때문이다.
 */
@Service
@RequiredArgsConstructor
public class ReservationCompletionService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ExpoRepository expoRepository;
    private final QrService qrService;
    private final ApplicationEventPublisher events;

    @Transactional
    public CompleteResponse complete(PendingPayment order, PortOnePaymentResponse pay) {
        LocalDateTime now = LocalDateTime.now();

        Payment payment = paymentRepository.save(Payment.builder()
                .amount(order.amount())
                .method(methodLabel(pay))
                .status(PaymentStatus.PAID)
                .paidAt(now)
                .portonePaymentId(order.paymentId())
                .build());

        // 비관적 락으로 정원 확인 → 동시 결제 시 초과 예약 방지 (초과 시 E005 → 호출 측에서 PG 자동 취소)
        Expo expo = expoRepository.findByIdWithLock(order.expoId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        expo.increaseReservedCount(order.people());

        String code = generateReservationCode();
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .user(userRepository.getReferenceById(order.userId()))
                .expo(expo)
                .payment(payment)
                .status(ReservationStatus.RESERVED)
                .code(code)
                .people(order.people())
                .createdAt(now)
                .updatedAt(now)
                .build());

        qrService.issue(reservation);
        events.publishEvent(new ReservationPaidEvent(reservation.getReservationId())); // 커밋 후 QR 메일

        return CompleteResponse.builder()
                .paymentPk(payment.getPaymentId())
                .reservationPk(reservation.getReservationId())
                .reservationId(reservation.getReservationId())
                .reservationCode(code)
                .status("PAID")
                .portonePaymentId(order.paymentId())
                .build();
    }

    private String generateReservationCode() {
        String date = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "RES-" + date + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * PortOne V2 결제수단(method.type + method.provider)을 우리 표기 문자열로 매핑
     * 예) {"type":"PaymentMethodEasyPay","provider":"TOSSPAY"} → TossPay
     * (기존: V1 필드명 paymentMethod/easyPay 를 읽어 V2 응답에서는 항상 UNKNOWN 으로 저장됐다)
     */
    private String methodLabel(PortOnePaymentResponse pay) {
        PortOnePaymentResponse.Method method = pay.getMethod();
        String type = method != null && method.getType() != null ? method.getType() : "";
        if ("PaymentMethodEasyPay".equals(type)) {
            String provider = method.getProvider() != null ? method.getProvider().trim().toUpperCase() : "";
            return switch (provider) {
                case "KAKAOPAY" -> "KakaoPay";
                case "TOSSPAY" -> "TossPay";
                case "NAVERPAY" -> "NaverPay";
                case "APPLEPAY" -> "ApplePay";
                case "SAMSUNGPAY" -> "SamsungPay";
                default -> "EasyPay";
            };
        }
        return switch (type) {
            case "PaymentMethodCard" -> "Credit Card";
            case "PaymentMethodTransfer" -> "Bank Transfer";
            case "PaymentMethodVirtualAccount" -> "Virtual Account";
            case "PaymentMethodMobile" -> "Mobile Phone";
            case "PaymentMethodGiftCertificate" -> "Gift Certificate";
            default -> "UNKNOWN";
        };
    }
}
