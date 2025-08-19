package com.eventory.systemAdmin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
	private final BCryptPasswordEncoder passwordEncoder;

	public Page<SysExpoResponseDto> findAllSysExpoPages(String status, String title, int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		Page<Expo> expoPage;
		
		if(status != null && !status.isBlank()) {
			if(title != null && !title.isBlank()) {
				expoPage = expoRepository.findByStatusAndTitleContaining(ExpoStatus.valueOf(status), title, pageable);
			} else {
				expoPage = expoRepository.findByStatus(ExpoStatus.valueOf(status), pageable);				
			}
		} else if(title != null && !title.isBlank()) {
			expoPage = expoRepository.findByTitleContaining(title, pageable);
		}
		else {
			expoPage = expoRepository.findAll(pageable);			
		}
		
		return expoPage.map(expo -> {
			ExpoCategory expoCategory = expoCategoryRepository.findByExpo(expo).orElse(null);
			return SysExpoResponseDto.from(expo, expoCategory != null ? expoCategory.getCategory().getName() : "");
		});

	}

	@Transactional
	public void updateExpoStatus(Long expoId, ExpoStatusRequestDto requestDto) {
		
		Expo expo = expoRepository.findById(expoId).orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
		
		if(!expo.getStatus().equals(ExpoStatus.PENDING)) {
			throw new CustomException(CustomErrorCode.HANDLED_EXPO);
		}
		
		if(requestDto.getStatus().equals(ExpoStatus.APPROVED.toString())) {
			if(expo.getExpoAdmin() == null) {				
				ExpoAdmin admin = expoAdminRepository.findByCustomerId(expo.getTitle()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
				String customerId = RandomGenerator.generateRandomId(8);
		        String rawPassword = RandomGenerator.generateRandomPassword(12);
		        String encodedPassword = passwordEncoder.encode(rawPassword);
				admin.createAccount(customerId, encodedPassword);
				expo.approveAndConnectAdmin(admin);
				
				// 이메일 전송
			} else {
				expo.approve();				
			}
			
		} else if(requestDto.getStatus().equals(ExpoStatus.REJECTED.toString())){
			if(requestDto.getReason().isBlank() || requestDto.getReason() == null) {
				throw new CustomException(CustomErrorCode.REASON_REQUIRED);
			}
			
			if(expo.getExpoAdmin() == null) {
				expoAdminRepository.deleteByCustomerId(expo.getTitle());
			}
			
			expo.reject(requestDto.getReason());
		}
		
	}

	public Page<SysExpoAdminResponseDto> findAllExpoAdminPages(String keyword, int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		Page<ExpoAdmin> expoAdminPage;
		
		if(keyword != null && !keyword.isBlank()) {
			expoAdminPage = expoAdminRepository.findByNameContainingOrPhoneContainingOrEmailContaining(keyword, keyword, keyword, pageable);
		} else {
			expoAdminPage = expoAdminRepository.findAll(pageable);
		}
		
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
