package com.eventory.payment.channel.strategy;

import com.eventory.payment.channel.Buyer;
import com.eventory.payment.channel.PaymentChannelStrategy;
import com.eventory.payment.channel.PaymentChannelType;
import com.eventory.payment.channel.PaymentMethodParams;
import org.springframework.stereotype.Component;

/** KG이니시스 카드 — PC 결제창은 구매자 이름·전화번호·이메일이 필수 */
@Component
public class InicisCardStrategy implements PaymentChannelStrategy {

    @Override
    public PaymentChannelType type() {
        return PaymentChannelType.INICIS_CARD;
    }

    @Override
    public PaymentMethodParams methodParams(Buyer buyer) {
        return PaymentMethodParams.ofCard().withCustomer(buyer);
    }
}
