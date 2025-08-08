package com.eventory.expoAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
// 일별, 주별, 월별 예약 수 통계 리포트
public class StatReportRowResponseDto {
    private String label;              // 날짜(요일) / 주차 / 월
    private Long reservationCount;    // 예약 건수
    private Long peopleCount;         // 총 예약 인원
    private BigDecimal paymentTotal;  // 총 결제 금액
    private Double avgPeoplePerResv;  // 예약당 평균 인원
    private Long cancelledCount;      // 예약 취소 건수
}
