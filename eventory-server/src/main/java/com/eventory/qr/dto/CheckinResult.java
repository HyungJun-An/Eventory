package com.eventory.qr.dto;

import lombok.Getter;

@Getter
@lombok.AllArgsConstructor
public class CheckinResult {
    private String expoTitle;
    private Long reservationId;
    private String reservationCode;
}
