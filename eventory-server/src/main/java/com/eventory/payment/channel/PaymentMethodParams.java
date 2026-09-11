package com.eventory.payment.channel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * PortOne 브라우저 SDK requestPayment 에 넘길 결제수단 관련 파라미터.
 * 프론트는 이 값을 해석하지 않고 그대로 SDK 에 전달한다 → PG를 바꿔도 프론트 수정이 필요 없다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentMethodParams(String payMethod, Map<String, Object> easyPay, Map<String, Object> customer) {

    // 팩토리 이름에 of 접두사: record 컴포넌트 접근자(easyPay())와 이름이 겹치지 않게 한다
    public static PaymentMethodParams ofCard() {
        return new PaymentMethodParams("CARD", null, null);
    }

    public static PaymentMethodParams ofEasyPay() {
        return new PaymentMethodParams("EASY_PAY", null, null);
    }

    public PaymentMethodParams withCustomer(Buyer buyer) {
        return new PaymentMethodParams(payMethod, easyPay, buyer.toCustomer());
    }
}
