package com.eventory.payment.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 결제 대기 주문 저장소 (Redis)
 * - payment:pending:{paymentId}    주문 (30분 후 자동 만료)
 * - payment:processing:{paymentId} 누군가 주문을 가져가 처리 중이라는 표시 (최대 1분)
 */
@Component
@RequiredArgsConstructor
public class PendingPaymentStore {

    private static final String KEY_PREFIX = "payment:pending:";
    private static final String PROCESSING_PREFIX = "payment:processing:";
    private static final Duration TTL = Duration.ofMinutes(30); // 결제창을 띄워 두고 결제를 마칠 수 있는 시간
    private static final Duration PROCESSING_TTL = Duration.ofMinutes(1); // 처리 중 서버가 죽어도 표시가 남지 않도록

    /** 주문을 꺼내 지우고 "처리 중" 표시를 남기는 것까지 한 번에 (두 명령 사이에 다른 요청이 끼지 않도록 Lua 로 원자 실행) */
    private static final DefaultRedisScript<String> TAKE_SCRIPT = new DefaultRedisScript<>(
            "local v = redis.call('GET', KEYS[1]) "
                    + "if v then redis.call('DEL', KEYS[1]); redis.call('SET', KEYS[2], '1', 'EX', ARGV[1]) end "
                    + "return v",
            String.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public void save(PendingPayment order) {
        redis.opsForValue().set(KEY_PREFIX + order.paymentId(), write(order), TTL);
    }

    /**
     * 주문을 꺼내면서 삭제한다 (원자적).
     * 결제 완료 요청과 웹훅이 동시에 와도(버튼 연타, PC 응답 + 모바일 리디렉션 + 웹훅) 한 요청만 주문을 얻어 처리한다.
     */
    public Optional<PendingPayment> take(String paymentId) {
        String json = redis.execute(TAKE_SCRIPT,
                List.of(KEY_PREFIX + paymentId, PROCESSING_PREFIX + paymentId),
                String.valueOf(PROCESSING_TTL.toSeconds()));
        return Optional.ofNullable(json).map(this::read);
    }

    /** 일시적 오류로 처리하지 못한 주문을 되돌려 재시도할 수 있게 한다 */
    public void restore(PendingPayment order) {
        save(order);
        finish(order.paymentId());
    }

    /** 다른 요청(웹훅 등)이 이 결제를 처리하고 있는지 */
    public boolean isProcessing(String paymentId) {
        return Boolean.TRUE.equals(redis.hasKey(PROCESSING_PREFIX + paymentId));
    }

    public void finish(String paymentId) {
        redis.delete(PROCESSING_PREFIX + paymentId);
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
