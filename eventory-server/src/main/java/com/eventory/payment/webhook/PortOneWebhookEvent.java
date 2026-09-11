package com.eventory.payment.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PortOne V2 웹훅 본문 (2024-04-01 버전)
 * 예) {"type":"Transaction.Paid","timestamp":"...","data":{"storeId":"...","paymentId":"...","transactionId":"..."}}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneWebhookEvent(String type, String timestamp, Data data) {

    public static final String TRANSACTION_PAID = "Transaction.Paid";

    public boolean isPaid() {
        return TRANSACTION_PAID.equals(type);
    }

    public String paymentId() {
        return data == null ? null : data.paymentId();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String storeId, String paymentId, String transactionId) {
    }
}
