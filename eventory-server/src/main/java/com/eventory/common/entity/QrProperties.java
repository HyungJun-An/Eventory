package com.eventory.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "qr")
public class QrProperties {
    /** HMAC-SHA256 서명 시드 */
    private String secret;
    /** 발신 메일 주소 */
    private String mailFrom;
    /** 메일 발신자 표시명 */
    private String issuer;
}
