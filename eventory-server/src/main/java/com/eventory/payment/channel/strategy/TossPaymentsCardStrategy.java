package com.eventory.payment.channel.strategy;

import com.eventory.payment.channel.Buyer;
import com.eventory.payment.channel.PaymentChannelStrategy;
import com.eventory.payment.channel.PaymentChannelType;
import com.eventory.payment.channel.PaymentMethodParams;
import org.springframework.stereotype.Component;

/** 토스페이먼츠 일반결제(카드) — 구매자 정보는 선택 */
@Component
public class TossPaymentsCardStrategy implements PaymentChannelStrategy {

    @Override
    public PaymentChannelType type() {
        return PaymentChannelType.TOSSPAYMENTS_CARD;
    }

    @Override
    public PaymentMethodParams methodParams(Buyer buyer) {
        return PaymentMethodParams.ofCard();
    }
}
