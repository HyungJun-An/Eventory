package com.eventory.systemAdmin.dto;

import java.time.LocalDateTime;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoStatus;

import lombok.Data;

@Data
public class SysExpoResponseDto {

	Long id;
	String title;
	String category;
	LocalDateTime createdAt;
	ExpoStatus status;
	
	public static SysExpoResponseDto from(Expo expo, String category) {
		SysExpoResponseDto dto = new SysExpoResponseDto();
		dto.id = expo.getExpoId();
		dto.title = expo.getTitle();
		dto.category = category;
		dto.createdAt = expo.getCreatedAt();
		dto.status = expo.getStatus();
		
		return dto;
	}
}
