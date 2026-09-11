package com.eventory.payment.webhook;

import com.eventory.config.PortOneProperties;
import com.eventory.payment.webhook.PortOneWebhookVerifier.InvalidWebhookException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortOneWebhookVerifierTest {

    private static final String SECRET = secretOf("0123456789abcdef0123456789abcdef");
    private static final Instant NOW = Instant.parse("2026-09-11T00:00:00Z");
    private static final String TS = String.valueOf(NOW.getEpochSecond());
    private static final String BODY = "{\"type\":\"Transaction.Paid\",\"data\":{\"paymentId\":\"pay_1\"}}";

    private PortOneProperties props;
    private PortOneWebhookVerifier verifier;

    @BeforeEach
    void setUp() {
        props = new PortOneProperties();
        props.setWebhookSecret(SECRET);
        verifier = new PortOneWebhookVerifier(props, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("올바른 서명이면 통과한다")
    void validSignature() {
        assertThatCode(() -> verifier.verify(BODY, "msg_1", TS, signature(SECRET, "msg_1", TS, BODY)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("본문이 한 글자라도 바뀌면 거부한다")
    void tamperedBody() {
        String sig = signature(SECRET, "msg_1", TS, BODY);
        assertThatThrownBy(() -> verifier.verify(BODY.replace("pay_1", "pay_2"), "msg_1", TS, sig))
                .isInstanceOf(InvalidWebhookException.class);
    }

    @Test
    @DisplayName("다른 시크릿으로 만든 서명은 거부한다")
    void otherSecret() {
        String forged = signature(secretOf("ffffffffffffffffffffffffffffffff"), "msg_1", TS, BODY);
        assertThatThrownBy(() -> verifier.verify(BODY, "msg_1", TS, forged))
                .isInstanceOf(InvalidWebhookException.class);
    }

    @Test
    @DisplayName("허용 시간(5분)을 벗어난 서명은 재전송 공격으로 보고 거부한다")
    void replayed() {
        String old = String.valueOf(NOW.minusSeconds(6 * 60).getEpochSecond());
        assertThatThrownBy(() -> verifier.verify(BODY, "msg_1", old, signature(SECRET, "msg_1", old, BODY)))
                .isInstanceOf(InvalidWebhookException.class)
                .hasMessageContaining("허용 시간");
    }

    @Test
    @DisplayName("서명 헤더가 없으면 거부한다")
    void missingHeaders() {
        assertThatThrownBy(() -> verifier.verify(BODY, null, TS, null))
                .isInstanceOf(InvalidWebhookException.class);
    }

    @Test
    @DisplayName("웹훅 시크릿이 설정되지 않았으면 모든 요청을 거부한다")
    void secretNotConfigured() {
        props.setWebhookSecret("");
        assertThatThrownBy(() -> verifier.verify(BODY, "msg_1", TS, signature(SECRET, "msg_1", TS, BODY)))
                .isInstanceOf(InvalidWebhookException.class);
    }

    @Test
    @DisplayName("키 교체 기간에 서명이 여러 개 오면 하나만 맞아도 통과한다")
    void multipleSignatures() {
        String header = "v1,AAAA " + signature(SECRET, "msg_1", TS, BODY);
        assertThatCode(() -> verifier.verify(BODY, "msg_1", TS, header)).doesNotThrowAnyException();
    }

    private static String signature(String secret, String id, String ts, String body) {
        return "v1," + Base64.getEncoder().encodeToString(PortOneWebhookVerifier.sign(secret, id + "." + ts + "." + body));
    }

    private static String secretOf(String raw) {
        return "whsec_" + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
