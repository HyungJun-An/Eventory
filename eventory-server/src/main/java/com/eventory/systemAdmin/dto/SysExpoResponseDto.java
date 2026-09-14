package com.eventory.systemAdmin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoStatus;

import lombok.Data;

@Data
public class SysExpoResponseDto {

	Long id;
	String title;
	String category;
	LocalDate startDate;
	LocalDate endDate;
	String location;
	LocalDateTime createdAt;
	ExpoStatus status;

	public static SysExpoResponseDto from(Expo expo, String category) {
		SysExpoResponseDto dto = new SysExpoResponseDto();
		dto.id = expo.getExpoId();
		dto.title = expo.getTitle();
		dto.category = category;
		dto.startDate = expo.getStartDate();
		dto.endDate = expo.getEndDate();
		dto.location = expo.getLocation();
		dto.createdAt = expo.getCreatedAt();
		dto.status = expo.getStatus();

		return dto;
	}
}
