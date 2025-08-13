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

@Service
@RequiredArgsConstructor
public class ExpoInfoServiceImpl implements ExpoInfoService {

    private final ExpoAdminRepository expoAdminRepository;
    private final ExpoRepository expoRepository;
    private final BannerRepository bannerRepository;
    private final ExpoMapper expoMapper;

    // 박람회 담당자 정보 조회
    @Override
    public ManagerResponseDto findExpoManagerInfo(Long expoAdminId) {

        ExpoAdmin expoAdmin = expoAdminRepository.findById(expoAdminId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_MANAGER));

        return expoMapper.toManagerResponseDto(expoAdmin);
    }

    // 박람회 담당자 정보 수정
    @Override
    public void updateExpoManagerInfo(Long expoAdminId, ManagerRequestDto requestDto) {

        ExpoAdmin expoAdmin = expoAdminRepository.findById(expoAdminId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_MANAGER));

        expoAdmin.updateExpoAdmin(requestDto);

        expoAdminRepository.save(expoAdmin);
    }

    // 특정 박람회 정보 조회
    @Override
    public ExpoResponseDto findExpoInfo(Long expoId) {

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        return expoMapper.toExpoResponseDto(expo);
    }

    // 특정 박람회 정보 수정
    @Override
    public void updateExpoInfo(Long expoId, ExpoRequestDto requestDto) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        expo.updateExpo(requestDto);

        expoRepository.save(expo);
    }

    // 특정 박람회 배너 신청
    @Override
    public void createExpoBanner(Long expoId, BannerCreateRequestDto requestDto) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        Banner banner = expoMapper.toBannerRequestDto(expo, requestDto);

        bannerRepository.save(banner);
    }

    // 특정 박람회 배너 조회
    @Override
    public BannerResponseDto findExpoBanner(Long expoId) {

        Banner banner = bannerRepository.findByExpo_ExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));

        return expoMapper.toBannerResponseDto(banner);
    }

    // 특정 박람회 배너 수정
    @Override
    public void updateExpoBanner(Long expoId, BannerUpdateRequestDto requestDto) {
        Banner banner = bannerRepository.findByExpo_ExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));

        banner.updateBanner(requestDto);

        bannerRepository.save(banner);
    }
}
