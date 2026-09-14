package com.eventory.payment.channel.strategy;

import com.eventory.payment.channel.Buyer;
import com.eventory.payment.channel.PaymentChannelStrategy;
import com.eventory.payment.channel.PaymentChannelType;
import com.eventory.payment.channel.PaymentMethodParams;
import org.springframework.stereotype.Component;

/** 카카오페이(간편결제 직연동) — EASY_PAY 만 허용 */
@Component
public class KakaoPayStrategy implements PaymentChannelStrategy {

    @Override
    public PaymentChannelType type() {
        return PaymentChannelType.KAKAOPAY;
    }

    @Override
    public PaymentMethodParams methodParams(Buyer buyer) {
        return PaymentMethodParams.ofEasyPay();
    }
}
