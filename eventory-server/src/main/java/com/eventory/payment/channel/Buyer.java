package com.eventory.payment.channel;

import java.util.LinkedHashMap;
import java.util.Map;

/** 결제자 정보 (일부 PG는 결제창에 구매자 정보가 필수) */
public record Buyer(String name, String email, String phone) {

    /** PortOne SDK requestPayment 의 customer 형식 (값이 없는 항목은 제외) */
    public Map<String, Object> toCustomer() {
        Map<String, Object> customer = new LinkedHashMap<>();
        if (name != null) customer.put("fullName", name);
        if (phone != null) customer.put("phoneNumber", phone);
        if (email != null) customer.put("email", email);
        return customer;
    }
}
