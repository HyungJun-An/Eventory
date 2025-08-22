package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
// 일별, 주별, 월별 예약 수 (막대그래프)
public class ReservationStatResponseDto {

    @NotBlank(message = "라벨은 비어 있을 수 없습니다.")
    private String label;       // X축 라벨: 날짜, 주차, 월

    @NotNull(message = "예약 수는 null일 수 없습니다.")
    @PositiveOrZero(message = "예약 수는 0 이상이어야 합니다.")
    private Long reservationCount; // Y축: 예약 수
}
