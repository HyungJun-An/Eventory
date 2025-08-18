package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
//대시보드 카드
public class DashboardResponseDto {

    @NotNull(message = "조회수는 null일 수 없습니다.")
    @PositiveOrZero(message = "조회수는 0 이상이어야 합니다.")
    private Long viewCount;         // 박람회 페이지 조회수

    @NotNull(message = "총 예약 수는 null일 수 없습니다.")
    @PositiveOrZero(message = "총 예약 수는 0 이상이어야 합니다.")
    private Long totalReservation; // 총 예약 수

    @NotNull(message = "입장률은 null일 수 없습니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "입장률은 0 이상이어야 합니다.")
    private Double checkInRate;     // 입장률 (퍼센트)
}
