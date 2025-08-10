package com.eventory.expoAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
// 티켓 종류별(무료/유료) 예약 비율 (파이차트)
public class TicketTypeRatioResponseDto {
    private String type;           // "FREE" or "PAID"
    private Long reservationCount; // 예약 건수(건 단위) (예: 1명이 예약한 건 1건, 4명이 예약한 건도 1건으로 계산)
    private Long peopleCount;      // 총 인원 수(명 단위) (예: 4명이 예약한 1건이면 peopleCount에는 +4) (옵션)
    private Double percentage;     // 파이차트에 필요한 비율 (예약 건수 기준(%)) ((reservationCount / totalReservations(무료 + 유료 예약 건수 합)) * 100)
}
