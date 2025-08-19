package com.eventory.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ReadyResponse {
    private final String paymentId; // PortOne paymentId
    private final String storeId;
    private final String channelKey;
    private final String orderName;
    private final BigDecimal totalAmount;
    private final String currency; // CURRENCY_KRW
    private final String payMethod; // CARD
    private final String redirectUrl; // 모바일 대응
}
