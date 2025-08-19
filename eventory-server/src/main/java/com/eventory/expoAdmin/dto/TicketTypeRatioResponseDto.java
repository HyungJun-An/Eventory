package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
// 티켓 종류별(무료/유료) 예약 비율 (파이차트)
public class TicketTypeRatioResponseDto {

    @NotBlank(message = "티켓 종류(type)는 비어 있을 수 없습니다.")
    @Pattern(regexp = "FREE|PAID", message = "티켓 종류는 FREE 또는 PAID여야 합니다.")
    private String type;           // "FREE" or "PAID"

    @NotNull(message = "예약 건수는 null일 수 없습니다.")
    @PositiveOrZero(message = "예약 건수는 0 이상이어야 합니다.")
    private Long reservationCount; // 예약 건수(건 단위) (예: 1명이 예약한 건 1건, 4명이 예약한 건도 1건으로 계산)

    @NotNull(message = "총 인원 수는 null일 수 없습니다.")
    @PositiveOrZero(message = "총 인원 수는 0 이상이어야 합니다.")
    private Long peopleCount;      // 총 인원 수(명 단위) (예: 4명이 예약한 1건이면 peopleCount에는 +4) (옵션)

    @NotNull(message = "비율은 null일 수 없습니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "비율은 0 이상이어야 합니다.")
    @DecimalMax(value = "100.0", inclusive = true, message = "비율은 100 이하여야 합니다.")
    private Double percentage;     // 파이차트에 필요한 비율 (예약 건수 기준(%)) ((reservationCount / totalReservations(무료 + 유료 예약 건수 합)) * 100)
}
