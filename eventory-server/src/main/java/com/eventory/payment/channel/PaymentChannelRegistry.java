package com.eventory.payment.channel;

import com.eventory.config.PortOneProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 결제 채널 전략 선택기.
 * 스프링이 모든 PaymentChannelStrategy 구현체를 주입하고, 설정(portone.channel-type)에 맞는 전략을 돌려준다.
 */
@Slf4j
@Component
public class PaymentChannelRegistry {

    private final Map<PaymentChannelType, PaymentChannelStrategy> strategies;
    private final PaymentChannelStrategy current;

    public PaymentChannelRegistry(List<PaymentChannelStrategy> strategyList, PortOneProperties props) {
        this.strategies = strategyList.stream().collect(Collectors.toMap(
                PaymentChannelStrategy::type,
                Function.identity(),
                (a, b) -> {
                    throw new IllegalStateException("같은 결제 채널 전략이 두 개 등록됨: " + a.type());
                },
                () -> new EnumMap<>(PaymentChannelType.class)));

        // 설정한 채널의 전략이 없으면 기동 단계에서 바로 실패시켜 잘못된 설정을 조기에 발견한다
        PaymentChannelType type = props.getChannelType();
        this.current = strategies.get(type);
        if (current == null) {
            throw new IllegalStateException("PORTONE_CHANNEL_TYPE=" + type + " 에 해당하는 결제 전략이 없습니다. 가능한 값: " + strategies.keySet());
        }
        log.info("[PortOne] 결제 채널: {} ({})", type.getLabel(), current.methodParams(new Buyer(null, null, null)).payMethod());
    }

    public PaymentChannelStrategy current() {
        return current;
    }
}
