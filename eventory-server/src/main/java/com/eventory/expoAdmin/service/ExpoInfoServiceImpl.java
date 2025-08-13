package com.eventory.expoAdmin.service;

import com.eventory.common.entity.Banner;
import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.BannerRepository;
import com.eventory.common.repository.ExpoAdminRepository;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.expoAdmin.dto.*;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpoInfoServiceImpl implements ExpoInfoService {

    private final ExpoAdminRepository expoAdminRepository;
    private final ExpoRepository expoRepository;
    private final BannerRepository bannerRepository;
    private final ExpoMapper expoMapper;

    // 박람회 담당자 정보 조회
    /*@Override
    public ManagerResponseDto findExpoManagerInfo(Long expoAdminId) {
        ExpoAdmin expoAdmin = expoAdminRepository.findById(expoAdminId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_MANAGER));
        return expoMapper.toManagerResponseDto(expoAdmin);
    }

    // 박람회 담당자 정보 수정
    @Override
    public void updateExpoManagerInfo(Long expoAdminId, ManagerRequestDto requestDto) {
        Optional<ExpoAdmin> expoAdmin = expoAdminRepository.findById(expoAdminId);
    }

    // 특정 박람회 정보 조회
    @Override
    public ExpoResponseDto findExpoInfo(Long expoId) {
        Optional<Expo> expo = expoRepository.findById(expoId);
        return null;
    }

    // 특정 박람회 정보 수정
    @Override
    public void updateExpoInfo(Long expoId, ExpoRequestDto requestDto) {
        Optional<Expo> expo = expoRepository.findById(expoId);
    }

    // 특정 박람회 배너 신청
    @Override
    public void createExpoBanner(Long expoId, BannerCreateRequestDto requestDto) {
        Optional<Expo> expo = expoRepository.findById(expoId);
    }

    // 특정 박람회 배너 조회
    @Override
    public BannerResponseDto findExpoBanner(Long expoId) {
        Optional<Banner> banner = bannerRepository.findByExpo_ExpoId(expoId);
        return null;
    }

    // 특정 박람회 배너 수정
    @Override
    public void updateExpoBanner(Long expoId, BannerUpdateRequestDto requestDto) {
        Optional<Banner> banner = bannerRepository.findByExpo_ExpoId(expoId);
    }*/
}
