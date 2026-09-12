package com.eventory.payment.service;

import com.eventory.auth.repository.UserRepository;
import com.eventory.common.entity.Payment;
import com.eventory.common.entity.PaymentStatus;
import com.eventory.common.entity.Reservation;
import com.eventory.common.entity.User;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.common.repository.PaymentRepository;
import com.eventory.common.repository.RefundRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.config.PortOneProperties;
import com.eventory.payment.channel.PaymentChannelRegistry;
import com.eventory.payment.dto.CompleteResponse;
import com.eventory.payment.dto.PortOneCancelRequest;
import com.eventory.payment.dto.PortOnePaymentResponse;
import com.eventory.payment.order.PendingPayment;
import com.eventory.payment.order.PendingPaymentStore;
import com.eventory.qr.service.CheckinService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String PAYMENT_ID = "pay_test";
    private static final Long BUYER = 1L;
    private static final BigDecimal PRICE = new BigDecimal("15000");

    @Mock PortOneProperties props;
    @Mock PortOneClient portOne;
    @Mock PaymentChannelRegistry channels;
    @Mock PendingPaymentStore pendingPayments;
    @Mock ReservationCompletionService completion;
    @Mock CheckinService checkinService;
    @Mock PaymentRepository paymentRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock RefundRepository refundRepository;
    @Mock UserRepository userRepository;
    @Mock ExpoRepository expoRepository;

    @InjectMocks PaymentServiceImpl service;

    private final PendingPayment order = new PendingPayment(PAYMENT_ID, BUYER, 7L, 1, PRICE, "테스트 박람회 입장권 1매");

    @Nested
    @DisplayName("결제 완료 요청 (/api/payment/complete)")
    class Complete {

        @Test
        @DisplayName("PortOne 결제 금액이 주문 금액과 같으면 예약을 확정한다")
        void success() {
            givenOrder();
            givenPortOne("PAID", PRICE);
            CompleteResponse expected = CompleteResponse.builder().reservationId(10L).status("PAID").build();
            when(completion.complete(eq(order), any())).thenReturn(expected);

            assertThat(service.complete(BUYER, PAYMENT_ID)).isSameAs(expected);
            verify(portOne, never()).cancelPayment(any(), any());
            verify(pendingPayments).finish(PAYMENT_ID);
        }

        @Test
        @DisplayName("실제 결제 금액이 다르면(위변조) PG 결제를 자동 취소하고 예약하지 않는다")
        void amountMismatch_cancelsAtPg() {
            givenOrder();
            givenPortOne("PAID", new BigDecimal("100"));
            when(portOne.cancelPayment(eq(PAYMENT_ID), any())).thenReturn(Mono.empty());

            assertError(() -> service.complete(BUYER, PAYMENT_ID), CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
            assertThat(cancelledAmount()).isEqualByComparingTo("100");
            verify(completion, never()).complete(any(), any());
        }

        @Test
        @DisplayName("결제 후 예약 확정이 실패하면(정원 초과 등) PG 결제를 자동 취소한다 — 보상 트랜잭션")
        void completionFails_cancelsAtPg() {
            givenOrder();
            givenPortOne("PAID", PRICE);
            when(completion.complete(eq(order), any())).thenThrow(new CustomException(CustomErrorCode.EXPO_CAPACITY_EXCEEDED));
            when(portOne.cancelPayment(eq(PAYMENT_ID), any())).thenReturn(Mono.empty());

            assertError(() -> service.complete(BUYER, PAYMENT_ID), CustomErrorCode.EXPO_CAPACITY_EXCEEDED);
            assertThat(cancelledAmount()).isEqualByComparingTo(PRICE);
        }

        @Test
        @DisplayName("정원 락을 3초 안에 얻지 못하면 PG 결제를 자동 취소하고 '요청이 몰림'(R017)으로 응답한다")
        void lockTimeout_cancelsAtPg() {
            givenOrder();
            givenPortOne("PAID", PRICE);
            when(completion.complete(eq(order), any())).thenThrow(new CannotAcquireLockException("Lock wait timeout exceeded"));
            when(portOne.cancelPayment(eq(PAYMENT_ID), any())).thenReturn(Mono.empty());

            assertError(() -> service.complete(BUYER, PAYMENT_ID), CustomErrorCode.RESERVATION_BUSY);
            assertThat(cancelledAmount()).isEqualByComparingTo(PRICE);
        }

        @Test
        @DisplayName("결제가 완료 상태가 아니면 예약하지 않는다")
        void notPaid() {
            givenOrder();
            givenPortOne("READY", PRICE);

            assertError(() -> service.complete(BUYER, PAYMENT_ID), CustomErrorCode.PAYMENT_NOT_PAID);
            verify(completion, never()).complete(any(), any());
        }

        @Test
        @DisplayName("다른 사용자의 주문이면 거부하고, 주인이 다시 완료할 수 있게 주문을 되돌린다")
        void otherUsersOrder() {
            givenOrder();

            assertError(() -> service.complete(99L, PAYMENT_ID), CustomErrorCode.ACCESS_DENIED);
            verify(pendingPayments).restore(order);
            verify(portOne, never()).getPayment(any());
        }

        @Test
        @DisplayName("PortOne 조회가 실패하면 주문을 되돌려 재시도할 수 있게 한다")
        void lookupFails_restoresOrder() {
            givenOrder();
            when(portOne.getPayment(PAYMENT_ID)).thenReturn(Mono.error(
                    WebClientResponseException.create(HttpStatus.BAD_GATEWAY.value(), "Bad Gateway", null, null, null)));

            assertError(() -> service.complete(BUYER, PAYMENT_ID), CustomErrorCode.PAYMENT_LOOKUP_FAILED);
            verify(pendingPayments).restore(order);
            verify(portOne, never()).cancelPayment(any(), any());
        }

        @Test
        @DisplayName("웹훅이 먼저 처리 중이면 끝날 때까지 기다렸다가 같은 결과를 돌려준다")
        void webhookProcessingFirst_waitsAndReturnsResult() {
            when(pendingPayments.take(PAYMENT_ID)).thenReturn(Optional.empty());
            when(pendingPayments.isProcessing(PAYMENT_ID)).thenReturn(true, true, false);
            when(reservationRepository.findByPayment_PortonePaymentId(PAYMENT_ID)).thenReturn(Optional.of(reservation(BUYER)));

            CompleteResponse res = service.complete(BUYER, PAYMENT_ID);

            assertThat(res.getReservationId()).isEqualTo(10L);
            verify(pendingPayments, times(3)).isProcessing(PAYMENT_ID);
        }

        @Test
        @DisplayName("이미 완료된 다른 사람의 결제는 조회할 수 없다")
        void completedByOtherUser() {
            when(pendingPayments.take(PAYMENT_ID)).thenReturn(Optional.empty());
            when(pendingPayments.isProcessing(PAYMENT_ID)).thenReturn(false);
            when(reservationRepository.findByPayment_PortonePaymentId(PAYMENT_ID)).thenReturn(Optional.of(reservation(BUYER)));

            assertError(() -> service.complete(99L, PAYMENT_ID), CustomErrorCode.PAYMENT_ORDER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("웹훅 (Transaction.Paid)")
    class Webhook {

        @Test
        @DisplayName("이미 확정된 결제는 아무것도 하지 않는다 — 같은 웹훅이 여러 번 와도 중복 저장 없음")
        void alreadyCompleted_isIdempotent() {
            when(reservationRepository.findByPayment_PortonePaymentId(PAYMENT_ID)).thenReturn(Optional.of(reservation(BUYER)));

            service.completeByWebhook(PAYMENT_ID);

            verify(pendingPayments, never()).take(any());
            verifyNoInteractions(completion, paymentRepository);
        }

        @Test
        @DisplayName("대기 주문이 없으면(결제 완료 요청이 처리 중이거나 우리 주문이 아님) 무시한다")
        void noPendingOrder() {
            when(reservationRepository.findByPayment_PortonePaymentId(PAYMENT_ID)).thenReturn(Optional.empty());
            when(pendingPayments.take(PAYMENT_ID)).thenReturn(Optional.empty());

            service.completeByWebhook(PAYMENT_ID);

            verifyNoInteractions(portOne, completion);
        }

        @Test
        @DisplayName("사용자가 브라우저를 닫아 완료 요청이 없어도 웹훅으로 예약을 확정한다")
        void confirmsWithoutCompleteRequest() {
            when(reservationRepository.findByPayment_PortonePaymentId(PAYMENT_ID)).thenReturn(Optional.empty());
            givenOrder();
            givenPortOne("PAID", PRICE);

            service.completeByWebhook(PAYMENT_ID);

            verify(completion).complete(eq(order), any());
        }

        @Test
        @DisplayName("PortOne 조회 실패는 예외를 던져 PortOne 이 웹훅을 다시 보내게 한다")
        void lookupFails_rethrowsForRetry() {
            when(reservationRepository.findByPayment_PortonePaymentId(PAYMENT_ID)).thenReturn(Optional.empty());
            givenOrder();
            when(portOne.getPayment(PAYMENT_ID)).thenReturn(Mono.error(
                    WebClientResponseException.create(HttpStatus.SERVICE_UNAVAILABLE.value(), "Unavailable", null, null, null)));

            assertError(() -> service.completeByWebhook(PAYMENT_ID), CustomErrorCode.PAYMENT_LOOKUP_FAILED);
            verify(pendingPayments).restore(order);
        }
    }

    // ─── helpers ───

    private void givenOrder() {
        when(pendingPayments.take(PAYMENT_ID)).thenReturn(Optional.of(order));
    }

    private void givenPortOne(String status, BigDecimal total) {
        PortOnePaymentResponse pay = new PortOnePaymentResponse();
        pay.setId(PAYMENT_ID);
        pay.setStatus(status);
        PortOnePaymentResponse.Amount amount = new PortOnePaymentResponse.Amount();
        amount.setTotal(total);
        pay.setAmount(amount);
        when(portOne.getPayment(PAYMENT_ID)).thenReturn(Mono.just(pay));
    }

    private BigDecimal cancelledAmount() {
        ArgumentCaptor<PortOneCancelRequest> captor = ArgumentCaptor.forClass(PortOneCancelRequest.class);
        verify(portOne).cancelPayment(eq(PAYMENT_ID), captor.capture());
        return captor.getValue().getCancelAmount();
    }

    private static Reservation reservation(Long ownerId) {
        return Reservation.builder()
                .reservationId(10L)
                .user(User.builder().userId(ownerId).build())
                .payment(Payment.builder().paymentId(20L).status(PaymentStatus.PAID).portonePaymentId(PAYMENT_ID).build())
                .code("RES-TEST")
                .build();
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, CustomErrorCode code) {
        assertThatThrownBy(call)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(code);
    }
}
