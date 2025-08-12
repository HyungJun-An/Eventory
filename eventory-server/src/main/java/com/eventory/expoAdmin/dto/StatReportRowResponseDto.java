package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
// 일별, 주별, 월별 예약 수 통계 리포트
public class StatReportRowResponseDto {

    @NotBlank(message = "라벨은 비어 있을 수 없습니다.")
    private String label;              // 날짜(요일) / 주차 / 월

    @NotNull(message = "예약 건수는 null일 수 없습니다.")
    @PositiveOrZero(message = "예약 건수는 0 이상이어야 합니다.")
    private Long reservationCount;    // 예약 건수

    @NotNull(message = "총 예약 인원은 null일 수 없습니다.")
    @PositiveOrZero(message = "총 예약 인원은 0 이상이어야 합니다.")
    private Long peopleCount;         // 총 예약 인원

    @NotNull(message = "총 결제 금액은 null일 수 없습니다.")
    @DecimalMin(value = "0.00", inclusive = true, message = "총 결제 금액은 0 이상이어야 합니다.")
    private BigDecimal paymentTotal;  // 총 결제 금액

    @NotNull(message = "예약당 평균 인원은 null일 수 없습니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "예약당 평균 인원은 0 이상이어야 합니다.")
    private Double avgPeoplePerResv;  // 예약당 평균 인원

    @NotNull(message = "예약 취소 건수는 null일 수 없습니다.")
    @PositiveOrZero(message = "예약 취소 건수는 0 이상이어야 합니다.")
    private Long cancelledCount;      // 예약 취소 건수
}
