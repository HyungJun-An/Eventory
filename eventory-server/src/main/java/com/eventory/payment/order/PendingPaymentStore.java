package com.eventory.payment.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/** 결제 대기 주문 저장소 (Redis, 30분 후 자동 만료) */
@Component
@RequiredArgsConstructor
public class PendingPaymentStore {

    private static final String KEY_PREFIX = "payment:pending:";
    private static final Duration TTL = Duration.ofMinutes(30); // 결제창을 띄워 두고 결제를 마칠 수 있는 시간

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public void save(PendingPayment order) {
        redis.opsForValue().set(KEY_PREFIX + order.paymentId(), write(order), TTL);
    }

    /**
     * 주문을 꺼내면서 삭제한다 (GETDEL, 원자적).
     * 결제 완료가 동시에 두 번 호출돼도(버튼 연타, PC 응답 + 모바일 리디렉션) 한 요청만 주문을 얻어 처리한다.
     */
    public Optional<PendingPayment> take(String paymentId) {
        String json = redis.opsForValue().getAndDelete(KEY_PREFIX + paymentId);
        return Optional.ofNullable(json).map(this::read);
    }

    private String write(PendingPayment order) {
        try {
            return objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("결제 주문 직렬화 실패", e);
        }
    }

    private PendingPayment read(String json) {
        try {
            return objectMapper.readValue(json, PendingPayment.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("결제 주문 역직렬화 실패", e);
        }
    }
}
