package com.eventory.systemAdmin.service;

import com.eventory.systemAdmin.dto.BannerUpdateRequestDto;
import com.eventory.common.entity.Banner;

import java.util.List;

public interface BannerService {
    List<Banner> findAllBanners();
    Banner findBannerDetail(Long bannerId);
    Banner updateBannerPeriod(Long bannerId, BannerUpdateRequestDto requestDto);
    Banner approveBanner(Long bannerId, String status, String reason);
}

