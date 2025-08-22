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
        // 기본키(expoAdminId)로 ExpoAdmin 조회
        ExpoAdmin expoAdmin = expoAdminRepository.findById(expoAdminId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_MANAGER));

        return expoMapper.toManagerResponseDto(expoAdmin);
    }

    // 박람회 담당자 정보 수정
    @Override
    public void updateExpoManagerInfo(Long expoAdminId, ManagerRequestDto requestDto) {
        // 기본키(expoAdminId)로 ExpoAdmin 조회
        ExpoAdmin expoAdmin = expoAdminRepository.findById(expoAdminId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_MANAGER));

        // 박람회 담당자 정보 업데이트
        expoAdmin.updateExpoAdmin(requestDto);

        // 수정한 박람회 담당자 정보 저장
        expoAdminRepository.save(expoAdmin);
    }

    // 특정 박람회 정보 조회
    @Override
    public ExpoResponseDto findExpoInfo(Long expoAdminId, Long expoId) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        // dto객체로 변환 및 반환
        return expoMapper.toExpoResponseDto(expo);
    }

    // 특정 박람회 정보 수정
    @Override
    public void updateExpoInfo(Long expoAdminId, Long expoId, ExpoUpdateRequestDto requestDto) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        // 특정 박람회 정보 업데이트
        expo.updateExpo(requestDto);

        // 수정한 박람회 정보 저장
        expoRepository.save(expo);
    }

    // 특정 박람회 배너 신청
    @Override
    public void createExpoBanner(Long expoAdminId, Long expoId, BannerCreateRequestDto requestDto) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        // dto 객체를 entity로 변환
        Banner banner = expoMapper.toBannerRequestDto(expo, requestDto);

        // 박람회 배너 저장
        bannerRepository.save(banner);
    }

    // 특정 박람회 배너 조회
    @Override
    public BannerResponseDto findExpoBanner(Long expoAdminId, Long expoId) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        // 특정 박람회(expoId)에 해당하는 배너 조회
        Banner banner = bannerRepository.findByExpo_ExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));

        // dto 객체로 변환 및 반환
        return expoMapper.toBannerResponseDto(banner);
    }

    // 특정 박람회 배너 수정
    @Override
    public void updateExpoBanner(Long expoAdminId, Long expoId, BannerUpdateRequestDto requestDto) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        // 특정 박람회(expoId)에 해당하는 배너 조회
        Banner banner = bannerRepository.findByExpo_ExpoId(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));

        // 특정 배너 정보 업데이트
        banner.updateBanner(requestDto);

        // 수정한 배너 정보 저장
        bannerRepository.save(banner);
    }

    // 현재 로그인한 사용자가 박람회 담당자인지 확인
    private void checkExpo_ExpoAdminAccess(Long expoId, Long expoAdminId) {
        if (!expoAdminId.equals(expoId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
