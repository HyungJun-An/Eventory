package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.entity.VipBanner;
import com.eventory.expoAdmin.repository.VipBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VipBannerService {

    private final VipBannerRepository vipBannerRepository;

    public List<VipBanner> getAllBanners() {
        return vipBannerRepository.findAll();
    }

    public VipBanner getBannerById(Long id) {
        return vipBannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));
    }

    @Transactional
    public void approveBanner(Long id) {
        VipBanner banner = getBannerById(id);
        banner.approve();
    }

    @Transactional
    public void rejectBanner(Long id, String reason) {
        VipBanner banner = getBannerById(id);
        banner.reject(reason);
    }

    @Transactional
    public void updateBannerPeriod(Long id, LocalDateTime start, LocalDateTime end) {
        VipBanner banner = getBannerById(id);
        banner.updatePeriod(start, end);
    }
}
