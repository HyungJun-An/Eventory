package com.eventory.expoAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// 일별, 주별, 월별 예약 수
public class ReservationStatResponseDto {
    private String label;       // X축 라벨: 날짜, 주차, 월
    private Long reservationCount; // Y축: 예약 수
}
