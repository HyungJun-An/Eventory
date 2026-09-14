package com.eventory.payment.dto;

/** 현재 사용 중인 결제 채널 (결제 화면의 결제수단 표시용) */
public record PaymentChannelInfo(String type, String label) {
}
