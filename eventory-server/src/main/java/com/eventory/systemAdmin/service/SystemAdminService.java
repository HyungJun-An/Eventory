package com.eventory.systemAdmin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoCategory;
import com.eventory.common.entity.ExpoStatus;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoCategoryRepository;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.systemAdmin.dto.ExpoStatusRequestDto;
import com.eventory.systemAdmin.dto.SysExpoResponseDto;

import jakarta.transaction.Transactional;
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

	@Transactional
	public void updateExpoStatus(Long expoId, ExpoStatusRequestDto requestDto) {
		
		Expo expo = expoRepository.findById(expoId).orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
		
		if(!expo.getStatus().equals(ExpoStatus.PENDING)) {
			throw new CustomException(CustomErrorCode.HANDLED_EXPO);
		}
		
		if(requestDto.getStatus().equals(ExpoStatus.APPROVED.toString())) {
			expo.approve();
			// 관리자 계정 존재 확인 로직 및 계정 이메일 전송
		} else if(requestDto.getStatus().equals(ExpoStatus.REJECTED.toString())){
			if(requestDto.getReason().isBlank() || requestDto.getReason() == null) {
				throw new CustomException(CustomErrorCode.REASON_REQUIRED);
			}
			expo.reject(requestDto.getReason());
		}
		
	}
	
	
}
