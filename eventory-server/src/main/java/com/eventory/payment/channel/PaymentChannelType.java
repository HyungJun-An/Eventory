package com.eventory.payment.channel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PortOne 콘솔에서 만든 결제 채널 종류.
 * 사용할 채널은 환경변수 PORTONE_CHANNEL_TYPE 으로 고르고, 채널별 결제창 파라미터는 PaymentChannelStrategy 구현체가 만든다.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentChannelType {
    TOSSPAY("토스페이"),
    KAKAOPAY("카카오페이"),
    TOSSPAYMENTS_CARD("토스페이먼츠 카드"),
    INICIS_CARD("KG이니시스 카드");

    private final String label;
}
