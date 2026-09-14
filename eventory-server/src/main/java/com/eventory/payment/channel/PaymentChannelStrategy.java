package com.eventory.payment.channel;

/**
 * 결제 채널(PG + 결제수단)별 결제창 파라미터 전략.
 * 새 PG·결제수단을 붙일 때는 이 인터페이스 구현체(@Component)를 하나 추가하고
 * PaymentChannelType 에 값을 추가한 뒤 PORTONE_CHANNEL_TYPE 을 바꾸면 된다.
 */
public interface PaymentChannelStrategy {

    PaymentChannelType type();

    /** PortOne SDK requestPayment 에 넘길 결제수단 파라미터 */
    PaymentMethodParams methodParams(Buyer buyer);
}
