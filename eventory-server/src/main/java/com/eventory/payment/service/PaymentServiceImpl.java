package com.eventory.payment.service;

import com.eventory.auth.repository.UserRepository;
import com.eventory.common.entity.*;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.common.repository.PaymentRepository;
import com.eventory.common.repository.RefundRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.config.PortOneProperties;
import com.eventory.payment.channel.Buyer;
import com.eventory.payment.channel.PaymentChannelRegistry;
import com.eventory.payment.channel.PaymentChannelStrategy;
import com.eventory.payment.channel.PaymentMethodParams;
import com.eventory.payment.dto.*;
import com.eventory.payment.order.PendingPayment;
import com.eventory.payment.order.PendingPaymentStore;
import com.eventory.qr.service.CheckinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 결제 흐름 조율 (ready → PG 결제창 → complete, 환불).
 * - 금액·사용자·박람회는 서버가 ready 에서 확정해 Redis 에 보관하고, complete 에서 PortOne 실제 결제와 대조한다.
 * - 결제 승인 후 예약 확정이 실패하면 PG 결제를 자동 취소한다 (보상 트랜잭션).
 * - 결제창 파라미터(결제수단 등)는 PaymentChannelStrategy 가 결정한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String CURRENCY = "CURRENCY_KRW";
    private static final int MAX_PEOPLE = 10;
    // 데모 데이터(db/demo-data.sql)의 결제는 실제 PG 결제가 아니므로 환불 시 PortOne 호출을 건너뛴다
    private static final String DEMO_PAYMENT_PREFIX = "seed_";
    // 다른 요청이 결제를 확정하는 중일 때 기다리는 최대 시간 (PortOne 조회 + 예약 저장이 보통 1초 이내)
    private static final Duration AWAIT_PROCESSING = Duration.ofSeconds(5);
    private static final long AWAIT_POLL_MS = 200;

    @Value("${eventory.seed.enabled:false}")
    private boolean demoDataEnabled;

    private final PortOneProperties props;
    private final PortOneClient portOne;
    private final PaymentChannelRegistry channels;
    private final PendingPaymentStore pendingPayments;
    private final ReservationCompletionService completion;
    private final CheckinService checkinService;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RefundRepository refundRepository;
    private final UserRepository userRepository;
    private final ExpoRepository expoRepository;

    @Override
    public PaymentChannelInfo currentChannel() {
        PaymentChannelStrategy channel = channels.current();
        return new PaymentChannelInfo(channel.type().name(), channel.type().getLabel());
    }

    // ───────────────────────── 결제 준비 ─────────────────────────

    @Override
    public ReadyResponse ready(Long userId, ReadyRequest req) {
        Expo expo = expoRepository.findById(req.getExpoId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        validateReservable(expo, req.getPeople());
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        // 금액은 서버가 계산한다 (기존: 클라이언트가 보낸 금액을 그대로 사용 → 금액 조작 가능)
        BigDecimal amount = expo.getPrice().multiply(BigDecimal.valueOf(req.getPeople()));
        String paymentId = "pay_" + UUID.randomUUID().toString().replace("-", "");
        String orderName = expo.getTitle() + " 입장권 " + req.getPeople() + "매";
        pendingPayments.save(new PendingPayment(paymentId, userId, expo.getExpoId(), req.getPeople(), amount, orderName));

        PaymentChannelStrategy channel = channels.current();
        PaymentMethodParams method = channel.methodParams(new Buyer(buyer.getName(), buyer.getEmail(), buyer.getPhone()));

        return ReadyResponse.builder()
                .paymentId(paymentId)
                .storeId(props.getStoreId())
                .channelKey(props.getChannelKey())
                .orderName(orderName)
                .totalAmount(amount)
                .currency(CURRENCY)
                .payMethod(method.payMethod())
                .easyPay(method.easyPay())
                .customer(method.customer())
                .channelLabel(channel.type().getLabel())
                .build();
    }

    private void validateReservable(Expo expo, int people) {
        boolean open = expo.getStatus() == ExpoStatus.APPROVED
                && Boolean.TRUE.equals(expo.getVisibility())
                && !expo.getEndDate().isBefore(LocalDate.now());
        if (!open) {
            throw new CustomException(CustomErrorCode.EXPO_NOT_RESERVABLE);
        }
        if (people < 1 || people > MAX_PEOPLE) {
            throw new CustomException(CustomErrorCode.INVALID_INPUT);
        }
        // 결제창을 띄우기 전에 걸러 불필요한 결제·자동취소를 줄인다 (최종 보장은 complete 의 비관적 락)
        if (expo.getReservedCount() + people > expo.getMaxCapacity()) {
            throw new CustomException(CustomErrorCode.EXPO_CAPACITY_EXCEEDED);
        }
    }

    // ───────────────────────── 결제 완료 ─────────────────────────

    @Override
    public CompleteResponse complete(Long userId, String paymentId) {
        // 주문을 원자적으로 꺼내 한 요청만 처리한다. 이미 처리된 결제면 같은 결과를 돌려준다(멱등).
        PendingPayment order = pendingPayments.take(paymentId).orElse(null);
        if (order == null) {
            return awaitCompleted(userId, paymentId);
        }
        if (!order.userId().equals(userId)) {
            pendingPayments.restore(order);
            throw new CustomException(CustomErrorCode.ACCESS_DENIED);
        }
        return confirm(order);
    }

    @Override
    public void completeByWebhook(String paymentId) {
        if (reservationRepository.findByPayment_PortonePaymentId(paymentId).isPresent()) {
            log.info("[Webhook] 이미 확정된 결제 paymentId={}", paymentId);
            return;
        }
        // 결제 완료 요청이 이미 주문을 가져갔거나(처리 중) 만료된 경우 → 웹훅은 할 일이 없다
        PendingPayment order = pendingPayments.take(paymentId).orElse(null);
        if (order == null) {
            log.info("[Webhook] 대기 주문 없음(처리 중·만료·우리 주문 아님) paymentId={}", paymentId);
            return;
        }
        try {
            confirm(order);
            log.info("[Webhook] 결제 완료 요청 없이 웹훅으로 예약 확정 paymentId={}", paymentId);
        } catch (CustomException e) {
            if (e.getErrorCode() == CustomErrorCode.PAYMENT_LOOKUP_FAILED) {
                throw e; // 일시적 조회 실패 → 5xx 응답으로 PortOne 재전송을 받는다 (주문은 복구돼 있음)
            }
            log.warn("[Webhook] 예약 확정 실패 paymentId={} code={}", paymentId, e.getErrorCode());
        }
    }

    /**
     * 주문을 가져간 쪽이 수행하는 결제 확정: PortOne 재조회 → PAID·금액 검증 → 예약 확정.
     * 결제는 승인됐는데 이후 단계가 실패하면 PG 결제를 자동 취소한다 (보상 트랜잭션).
     */
    private CompleteResponse confirm(PendingPayment order) {
        String paymentId = order.paymentId();
        try {
            PortOnePaymentResponse pay;
            try {
                pay = fetchPayment(paymentId);
            } catch (CustomException e) {
                pendingPayments.restore(order); // 조회 실패는 일시적일 수 있으므로 재시도 가능하게 되돌려 둔다
                throw e;
            }

            if (!"PAID".equalsIgnoreCase(pay.getStatus())) {
                throw new CustomException(CustomErrorCode.PAYMENT_NOT_PAID);
            }
            BigDecimal paid = pay.getAmount() != null ? pay.getAmount().getTotal() : null;
            if (paid == null || order.amount().compareTo(paid) != 0) {
                cancelSafely(paymentId, paid, "결제 금액 불일치로 자동 취소");
                throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }

            try {
                return completion.complete(order, pay);
            } catch (RuntimeException e) {
                // 결제는 승인됐는데 예약 확정 실패(정원 초과 등) → DB 는 롤백됐으므로 PG 결제를 자동 취소
                cancelSafely(paymentId, order.amount(), "예약 처리 실패로 자동 취소");
                throw e;
            }
        } finally {
            pendingPayments.finish(paymentId);
        }
    }

    /**
     * 주문이 없을 때: 다른 요청(주로 웹훅)이 처리 중이면 끝날 때까지 잠시 기다렸다가 결과를 돌려준다.
     * 웹훅이 결제 완료 요청보다 먼저 도착하는 경우 사용자가 "결제 정보 없음" 오류를 보지 않게 하기 위함.
     */
    private CompleteResponse awaitCompleted(Long userId, String paymentId) {
        long deadline = System.currentTimeMillis() + AWAIT_PROCESSING.toMillis();
        while (pendingPayments.isProcessing(paymentId) && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(AWAIT_POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return findCompleted(userId, paymentId);
    }

    /** 이미 완료된 결제의 결과 조회 (완료 요청 중복, 모바일 리디렉션 재진입 대비) */
    private CompleteResponse findCompleted(Long userId, String paymentId) {
        Reservation reservation = reservationRepository.findByPayment_PortonePaymentId(paymentId)
                .filter(r -> r.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_ORDER_NOT_FOUND));
        return CompleteResponse.builder()
                .paymentPk(reservation.getPayment().getPaymentId())
                .reservationPk(reservation.getReservationId())
                .reservationId(reservation.getReservationId())
                .reservationCode(reservation.getCode())
                .status(reservation.getPayment().getStatus().name())
                .portonePaymentId(paymentId)
                .build();
    }

    private PortOnePaymentResponse fetchPayment(String paymentId) {
        try {
            PortOnePaymentResponse pay = portOne.getPayment(paymentId).block();
            if (pay == null) throw new CustomException(CustomErrorCode.PAYMENT_LOOKUP_FAILED);
            return pay;
        } catch (WebClientResponseException e) {
            log.error("[Payment] PortOne 결제 조회 실패 paymentId={} status={} body={}",
                    paymentId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(CustomErrorCode.PAYMENT_LOOKUP_FAILED);
        }
    }

    /** 보상 취소 — 실패해도 원래 오류를 가리지 않도록 로그만 남긴다 (운영자가 수동 환불할 수 있게 ERROR 로그) */
    private void cancelSafely(String paymentId, BigDecimal amount, String reason) {
        try {
            cancelAtPortOne(paymentId, new PortOneCancelRequest(amount, reason));
            log.warn("[Payment] {} paymentId={} amount={}", reason, paymentId, amount);
        } catch (Exception e) {
            log.error("[Payment] 자동 취소 실패 — 수동 환불 필요 paymentId={} amount={}", paymentId, amount, e);
        }
    }

    // ───────────────────────── 환불 ─────────────────────────

    /** 사용자 본인 환불 — 소유자·입장 여부를 확인하고 전액 환불 + 환불 이력 기록 */
    @Override
    @Transactional
    public void refundByUser(Long userId, Long reservationId, String reason) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_RESERVATION));
        if (!res.getUser().getUserId().equals(userId)) {
            throw new CustomException(CustomErrorCode.ACCESS_DENIED);
        }
        if (res.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(CustomErrorCode.RESERVATION_CANCELLED);
        }
        if (checkinService.isCheckedIn(res)) {
            throw new CustomException(CustomErrorCode.ALREADY_CHECKED_IN);
        }
        refund(reservationId, reason);
        refundRepository.save(Refund.approvedOf(res.getPayment(), reason));
    }

    /** 전액 환불 (PG 취소 → 결제 REFUNDED, 예약 CANCELLED, 정원 복구). 권한 확인은 호출하는 쪽 책임 */
    @Override
    @Transactional
    public void refund(Long reservationId, String reason) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_RESERVATION));
        Payment pay = res.getPayment();
        if (pay.getStatus() == PaymentStatus.REFUNDED) {
            return; // 이미 환불된 결제는 멱등 처리
        }

        String portonePaymentId = pay.getPortonePaymentId();
        if (portonePaymentId == null || portonePaymentId.isBlank()) {
            throw new IllegalStateException("portonePaymentId 미저장 — 결제 완료 저장 로직 확인 필요");
        }
        cancelAtPortOne(portonePaymentId, new PortOneCancelRequest(pay.getAmount(), reason));

        pay.markRefunded();
        res.setStatus(ReservationStatus.CANCELLED);

        // 비관적 락으로 Expo 재조회 후 예약 인원 감소
        Expo expo = expoRepository.findByIdWithLock(res.getExpo().getExpoId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        expo.decreaseReservedCount(res.getPeople());

        reservationRepository.save(res);
        paymentRepository.save(pay);
    }

    /** PortOne 결제 취소. PG 오류는 원인 로그를 남기고 명확한 에러 코드로 변환한다 */
    private void cancelAtPortOne(String portonePaymentId, PortOneCancelRequest cancelReq) {
        if (demoDataEnabled && portonePaymentId.startsWith(DEMO_PAYMENT_PREFIX)) {
            log.info("[Refund] 데모 결제 건이라 PortOne 취소 호출을 건너뜀: {}", portonePaymentId);
            return;
        }
        try {
            portOne.cancelPayment(portonePaymentId, cancelReq).block();
        } catch (WebClientResponseException e) {
            log.error("[Refund] PortOne 취소 실패 paymentId={} status={} body={}",
                    portonePaymentId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(CustomErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }
}
