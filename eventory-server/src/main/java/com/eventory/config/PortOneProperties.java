package com.eventory.config;

import com.eventory.payment.channel.PaymentChannelType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "portone")
public class PortOneProperties {
    private String apiBaseUrl;
    private String v2Secret;
    private String webhookSecret;
    private String storeId;
    private String channelKey;
    /** 결제 채널 종류 — 결제창 파라미터를 만들 전략(PaymentChannelStrategy)을 고른다 */
    private PaymentChannelType channelType = PaymentChannelType.TOSSPAY;

    /**
     * 결제 키가 비어 있으면 결제창 호출·결제 검증·환불이 모두 실패한다.
     * 서버는 그대로 띄우되(결제 외 기능은 정상), 기동 시점에 누락을 알려 .env 설정 실수를 바로 발견하게 한다.
     */
    @PostConstruct
    void warnIfMissing() {
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(storeId)) missing.add("PORTONE_STORE_ID");
        if (!StringUtils.hasText(channelKey)) missing.add("PORTONE_CHANNEL_KEY");
        if (!StringUtils.hasText(v2Secret)) missing.add("PORTONE_V2_SECRET");
        if (!missing.isEmpty()) {
            log.warn("[PortOne] 결제 설정 누락: {} — 루트 .env 에 값을 넣고 서버를 재시작하세요 (.env 참고)", missing);
        }
    }
}
