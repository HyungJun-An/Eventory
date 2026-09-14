package com.eventory.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 메일 발송 계정 누락 경고.
 * 계정이 없어도 서버는 뜨지만(QR 메일만 실패, 결제·예약은 정상) 기동 시점에 알려 .env 설정 누락을 바로 발견하게 한다.
 */
@Slf4j
@Configuration
public class MailSettingsCheck {

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @PostConstruct
    void warnIfMissing() {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("[Mail] 메일 발송 계정 누락: MAIL_USERNAME / MAIL_PASSWORD — QR 메일이 발송되지 않습니다. 루트 .env 에 값을 넣고 서버를 재시작하세요");
        } else {
            log.info("[Mail] QR 메일 발송 계정: {}", username);
        }
    }
}
