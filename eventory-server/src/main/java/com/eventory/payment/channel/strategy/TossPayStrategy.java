package com.eventory.payment.channel.strategy;

import com.eventory.payment.channel.Buyer;
import com.eventory.payment.channel.PaymentChannelStrategy;
import com.eventory.payment.channel.PaymentChannelType;
import com.eventory.payment.channel.PaymentMethodParams;
import org.springframework.stereotype.Component;

/** 토스페이(간편결제 직연동) — 채널이 payMethod 로 EASY_PAY 만 허용한다 (CARD 요청 시 결제창 호출 실패) */
@Component
public class TossPayStrategy implements PaymentChannelStrategy {

    @Override
    public PaymentChannelType type() {
        return PaymentChannelType.TOSSPAY;
    }

    @Override
    public PaymentMethodParams methodParams(Buyer buyer) {
        return PaymentMethodParams.ofEasyPay();
    }
}
