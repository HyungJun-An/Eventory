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
    public ExpoResponseDto findExpoInfo(Long expoAdminId, Long expoId) {

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        return expoMapper.toExpoResponseDto(expo);
    }

    // 특정 박람회 정보 수정
    @Override
    public void updateExpoInfo(Long expoAdminId, Long expoId, ExpoRequestDto requestDto) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        expo.updateExpo(requestDto);

        expoRepository.save(expo);
    }

    // 특정 박람회 배너 신청
    @Override
    public void createExpoBanner(Long expoAdminId, Long expoId, BannerCreateRequestDto requestDto) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        Banner banner = expoMapper.toBannerRequestDto(expo, requestDto);

        bannerRepository.save(banner);
    }

    // 특정 박람회 배너 조회
    @Override
    public BannerResponseDto findExpoBanner(Long expoAdminId, Long expoId) {

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        Banner banner = bannerRepository.findByExpo_ExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));

        return expoMapper.toBannerResponseDto(banner);
    }

    // 특정 박람회 배너 수정
    @Override
    public void updateExpoBanner(Long expoAdminId, Long expoId, BannerUpdateRequestDto requestDto) {

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        Banner banner = bannerRepository.findByExpo_ExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));

        banner.updateBanner(requestDto);

        bannerRepository.save(banner);
    }

    private void checkExpo_ExpoAdminAccess(Long expoId, Long expoAdminId) {
        if (!expoAdminId.equals(expoId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
