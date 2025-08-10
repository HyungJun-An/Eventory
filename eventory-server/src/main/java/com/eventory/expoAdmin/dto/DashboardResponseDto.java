package com.eventory.expoAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
//대시보드 카드
public class DashboardResponseDto {
    private Long viewCount;         // 박람회 페이지 조회수
    private Long totalReservation; // 총 예약 수
    private Double checkInRate;     // 입장률 (퍼센트)
}
