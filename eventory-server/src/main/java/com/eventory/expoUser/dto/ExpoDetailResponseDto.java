package com.eventory.expoUser.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** 참관객용 박람회 상세 (상세 화면·결제 화면에서 사용) */
@Builder
public record ExpoDetailResponseDto(
        Long expoId,
        String title,
        String description,
        String imageUrl,
        String location,
        String startDate,
        String endDate,
        BigDecimal price,
        int remainingCapacity,
        List<String> categories,
        String host,
        String contact
) {
}
