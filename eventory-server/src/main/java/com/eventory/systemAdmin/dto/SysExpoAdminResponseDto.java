package com.eventory.systemAdmin.dto;

import java.time.LocalDateTime;

import com.eventory.common.entity.ExpoAdmin;

import lombok.Data;

@Data
public class SysExpoAdminResponseDto {

	private Long id;
	private String loginId;
	private String name;
	private String phone;
	private String email;
	private LocalDateTime createdAt;
	private LocalDateTime lastAppliedAt;

	public static SysExpoAdminResponseDto from(ExpoAdmin admin, LocalDateTime lastAppliedAt) {
		SysExpoAdminResponseDto dto = new SysExpoAdminResponseDto();
		dto.id = admin.getExpoAdminId();
		// 승인 전 임시 계정은 customerId 에 박람회 제목이 들어 있으므로, 담당 박람회가 연결된(=승인된) 계정만 아이디를 노출
		dto.loginId = lastAppliedAt != null ? admin.getCustomerId() : null;
		dto.name = admin.getName();
		dto.phone = admin.getPhone();
		dto.email = admin.getEmail();
		dto.createdAt = admin.getCreatedAt();
		dto.lastAppliedAt = lastAppliedAt;

		return dto;
	}
}
