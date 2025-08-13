package com.eventory.systemAdmin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.ExpoCategory;
import com.eventory.common.entity.ExpoStatus;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoAdminRepository;
import com.eventory.common.repository.ExpoCategoryRepository;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.expoAdmin.dto.ManagerRequestDto;
import com.eventory.systemAdmin.dto.ExpoStatusRequestDto;
import com.eventory.systemAdmin.dto.SysExpoAdminResponseDto;
import com.eventory.systemAdmin.dto.SysExpoResponseDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemAdminService {

	private final ExpoRepository expoRepository;
	private final ExpoCategoryRepository expoCategoryRepository;
	private final ExpoAdminRepository expoAdminRepository;

	public Page<SysExpoResponseDto> findAllSysExpoPages(String status, String title, int page, int size) {
		
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

	public Page<SysExpoAdminResponseDto> findAllExpoAdminPages(String keyword, int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		Page<ExpoAdmin> expoAdminPage = expoAdminRepository.findAll(pageable);
		
		return expoAdminPage.map(admin -> {
			Expo lastExpo = expoRepository.findFirstByExpoAdminOrderByCreatedAtDesc(admin).orElse(null);
			return SysExpoAdminResponseDto.from(admin, lastExpo != null ? lastExpo.getCreatedAt() : null);
		});
	}

	public Page<SysExpoResponseDto> findExpoByExpoAdminPages(Long adminId, int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		ExpoAdmin admin = expoAdminRepository.findById(adminId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
		Page<Expo> expoPage = expoRepository.findByExpoAdmin(admin, pageable);
		
		return expoPage.map(expo -> {
			ExpoCategory expoCategory = expoCategoryRepository.findByExpo(expo).orElseThrow(() -> new CustomException(CustomErrorCode.CATEGORY_NOT_FOUND));
			String category = expoCategory.getCategory().getName();
			return SysExpoResponseDto.from(expo, category);
		});
	}

	public SysExpoAdminResponseDto findExpoAdmin(Long adminId) {
		
		ExpoAdmin admin = expoAdminRepository.findById(adminId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
		
		return SysExpoAdminResponseDto.from(admin, null);
	}

	@Transactional
	public void updateExpoAdmin(Long adminId, ManagerRequestDto requestDto) {
		
		ExpoAdmin admin = expoAdminRepository.findById(adminId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
		admin.updateExpoAdmin(requestDto);
	}

	@Transactional
	public void deleteExpoAdmin(Long adminId) {
		
		expoAdminRepository.deleteById(adminId);
	}
	
	
}
