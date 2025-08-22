package com.eventory.qr.dto;

import lombok.Getter;

@Getter
@lombok.AllArgsConstructor
public class CheckinResponse {
    private String status; // OK / INVALID_OR_EXPIRED / ALREADY_CHECKED_IN / TOKEN_MISMATCH
    private CheckinResult data;
    public static CheckinResponse of(String s, CheckinResult d) { return new CheckinResponse(s, d); }
}
