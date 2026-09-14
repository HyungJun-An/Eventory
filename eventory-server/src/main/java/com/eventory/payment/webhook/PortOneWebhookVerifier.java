package com.eventory.payment.webhook;

import com.eventory.config.PortOneProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;

/**
 * PortOne V2 웹훅 서명 검증 (Standard Webhooks 규격).
 * <pre>
 * 서명 대상 = "{webhook-id}.{webhook-timestamp}.{원문 body}"
 * 서명 값   = Base64( HMAC-SHA256( Base64Decode(시크릿에서 "whsec_" 제거), 서명 대상 ) )
 * 헤더      = webhook-signature: "v1,{서명}" (키 교체 기간에는 공백으로 여러 개)
 * </pre>
 * 기존: 검증 코드가 주석 처리돼 있어 누구나 웹훅 URL 로 결제 완료 이벤트를 보낼 수 있었다.
 */
@Component
public class PortOneWebhookVerifier {

    /** 재전송(replay) 공격 방지 — 서명 시각이 이 범위를 벗어나면 거부 */
    static final Duration TOLERANCE = Duration.ofMinutes(5);
    private static final String SECRET_PREFIX = "whsec_";

    private final PortOneProperties props;
    private final Clock clock;

    @Autowired
    public PortOneWebhookVerifier(PortOneProperties props) {
        this(props, Clock.systemUTC());
    }

    /** 테스트에서 시각을 고정하기 위한 생성자 */
    PortOneWebhookVerifier(PortOneProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    public void verify(String body, String webhookId, String timestamp, String signatureHeader) {
        String secret = props.getWebhookSecret();
        if (!StringUtils.hasText(secret)) {
            throw new InvalidWebhookException("웹훅 시크릿(PORTONE_WEBHOOK_SECRET) 미설정");
        }
        if (!StringUtils.hasText(webhookId) || !StringUtils.hasText(timestamp) || !StringUtils.hasText(signatureHeader)) {
            throw new InvalidWebhookException("서명 헤더 누락");
        }

        long signedAt;
        try {
            signedAt = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException e) {
            throw new InvalidWebhookException("잘못된 webhook-timestamp");
        }
        long now = clock.instant().getEpochSecond();
        if (Math.abs(now - signedAt) > TOLERANCE.toSeconds()) {
            throw new InvalidWebhookException("허용 시간을 벗어난 웹훅 (재전송 의심)");
        }

        byte[] expected = sign(secret, webhookId.trim() + "." + signedAt + "." + body);
        for (String candidate : signatureHeader.trim().split(" ")) {
            int comma = candidate.indexOf(',');
            if (comma < 0 || !"v1".equals(candidate.substring(0, comma))) continue;
            byte[] actual;
            try {
                actual = Base64.getDecoder().decode(candidate.substring(comma + 1));
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (MessageDigest.isEqual(expected, actual)) { // 타이밍 공격 방지용 상수 시간 비교
                return;
            }
        }
        throw new InvalidWebhookException("서명 불일치");
    }

    /** 서명 생성 (검증과 테스트에서 공용) */
    static byte[] sign(String secret, String content) {
        String base64Key = secret.startsWith(SECRET_PREFIX) ? secret.substring(SECRET_PREFIX.length()) : secret;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(base64Key), "HmacSHA256"));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new InvalidWebhookException("웹훅 시크릿 형식 오류");
        }
    }

    public static class InvalidWebhookException extends RuntimeException {
        public InvalidWebhookException(String message) {
            super(message);
        }
    }
}
