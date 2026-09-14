package com.eventory.systemAdmin.dto;

import com.eventory.common.entity.ExpoStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 시스템관리자용 박람회 상세 (승인 심사 화면) */
@Builder
public record SysExpoDetailResponseDto(
        Long id,
        String title,
        String description,
        String imageUrl,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        int maxCapacity,
        int reservedCount,
        ExpoStatus status,
        String reason,
        List<String> categories,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        Applicant applicant
) {
    /** 신청 담당자 (승인 전에는 신청 시 만든 임시 관리자, 승인 후에는 정식 박람회관리자) */
    public record Applicant(String name, String email, String phone, String loginId) {
    }
}
