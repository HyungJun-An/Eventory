package com.eventory.systemAdmin.dto;

import com.eventory.common.entity.ExpoStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

/** 박람회 승인·반려 결과 — 첫 승인으로 관리자 계정이 새로 발급된 경우에만 credential 이 포함된다 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExpoApprovalResponseDto(Long expoId, ExpoStatus status, AdminCredentialDto credential) {
}
