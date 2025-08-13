package com.eventory.systemAdmin.dto;

import java.time.LocalDateTime;

import com.eventory.common.entity.ExpoAdmin;

import lombok.Data;

@Data
public class SysExpoAdminResponseDto {

	private String name;
	private String phone;
	private String email;
	private LocalDateTime createdAt;
	private LocalDateTime lastAppliedAt;
	
	public static SysExpoAdminResponseDto from(ExpoAdmin admin, LocalDateTime lastAppliedAt) {
		SysExpoAdminResponseDto dto = new SysExpoAdminResponseDto();
		dto.name = admin.getName();
		dto.phone = admin.getPhone();
		dto.email = admin.getEmail();
		dto.createdAt = admin.getCreatedAt();
		dto.lastAppliedAt = lastAppliedAt;
		
		return dto;
	}
}
