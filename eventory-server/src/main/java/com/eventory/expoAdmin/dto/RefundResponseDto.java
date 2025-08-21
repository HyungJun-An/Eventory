package com.eventory.expoAdmin.dto;

import com.eventory.common.entity.RefundStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RefundResponseDto {

    @NotNull
    private Long refundId;

    @NotBlank
    private String code;

    @NotBlank
    private String method;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;

    @NotBlank
    private String reason;

    @NotNull
    private RefundStatus status;
}
