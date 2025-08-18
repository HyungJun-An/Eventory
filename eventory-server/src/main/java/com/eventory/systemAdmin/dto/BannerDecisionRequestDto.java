package com.eventory.systemadmin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BannerDecisionRequestDto {
    @NotNull(message = "배너 상태는 필수입니다.")
    private String status; // APPROVED / REJECTED

    private String reason; // 반려 시 거절 사유
}
