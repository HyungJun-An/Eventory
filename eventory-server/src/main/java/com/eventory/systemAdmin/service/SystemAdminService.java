package com.eventory.systemAdmin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoCategory;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoCategoryRepository;
import com.eventory.expoAdmin.repository.ExpoRepository;
import com.eventory.systemAdmin.dto.SysExpoResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemAdminService {

	private final ExpoRepository expoRepository;
	private final ExpoCategoryRepository expoCategoryRepository;

	public Page<SysExpoResponseDto> getAllSysExpoPages(String status, String title, int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		Page<Expo> expoPage = expoRepository.findAll(pageable);
		
		return expoPage.map(expo -> {
			ExpoCategory expoCategory = expoCategoryRepository.findByExpo(expo).orElseThrow(() -> new CustomException(CustomErrorCode.CATEGORY_NOT_FOUND));
			String category = expoCategory.getCategory().getName();
			return SysExpoResponseDto.from(expo, category);
		});

	}
	
	
}
