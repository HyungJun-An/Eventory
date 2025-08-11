package com.eventory.expoAdmin.dto;

import com.eventory.common.entity.RefundStatus;
import lombok.Getter;

@Getter
public class RefundRequestDto {
    private RefundStatus status;
    private String reason;
}
