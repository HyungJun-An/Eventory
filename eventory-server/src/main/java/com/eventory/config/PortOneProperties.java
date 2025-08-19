package com.eventory.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

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
}