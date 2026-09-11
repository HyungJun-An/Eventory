package com.eventory.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * PortOne 브라우저 SDK requestPayment 에 그대로 넘길 값.
 * payMethod / easyPay / customer 는 결제 채널 전략(PaymentChannelStrategy)이 채운다.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReadyResponse {
    private final String paymentId; // PortOne paymentId (서버 채번)
    private final String storeId;
    private final String channelKey;
    private final String orderName;
    private final BigDecimal totalAmount; // 서버 계산 금액
    private final String currency; // CURRENCY_KRW
    private final String payMethod; // CARD | EASY_PAY ...
    private final Map<String, Object> easyPay; // 간편결제 세부 옵션 (필요한 채널만)
    private final Map<String, Object> customer; // 구매자 정보 (필수인 채널만)
    private final String channelLabel; // 화면 표시용 (예: 토스페이)
}
