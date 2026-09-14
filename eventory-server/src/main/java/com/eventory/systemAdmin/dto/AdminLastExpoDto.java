package com.eventory.systemAdmin.dto;

import java.time.LocalDateTime;

/** 관리자별 마지막 박람회 신청 시각 (관리자 목록 한 페이지를 GROUP BY 쿼리 1번으로 조회) */
public record AdminLastExpoDto(Long adminId, LocalDateTime lastCreatedAt) {
}
