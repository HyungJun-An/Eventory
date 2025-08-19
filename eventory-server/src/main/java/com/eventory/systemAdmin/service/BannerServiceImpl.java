package com.eventory.systemAdmin.service;

import com.eventory.common.entity.Banner;
import com.eventory.common.entity.BannerStatus;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.BannerRepository;
import com.eventory.systemAdmin.dto.BannerUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    public List<Banner> findAllBanners() {
        return bannerRepository.findAll();
    }

    @Override
    public Banner findBannerDetail(Long bannerId) {
        return bannerRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));
    }

    @Override
    @Transactional
    public Banner updateBannerPeriod(Long bannerId, BannerUpdateRequestDto requestDto) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));
        banner.updateBanner(requestDto);
        return banner;
    }

    @Override
    @Transactional
    public Banner approveBanner(Long bannerId, String status, String reason) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BANNER));

        BannerStatus bannerStatus;
        try {
            bannerStatus = BannerStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new CustomException(CustomErrorCode.INVALID_INPUT); // 상태 값 오류
        }

        banner.setStatus(bannerStatus);
        banner.setReason(reason);
        return banner;
    }
}
