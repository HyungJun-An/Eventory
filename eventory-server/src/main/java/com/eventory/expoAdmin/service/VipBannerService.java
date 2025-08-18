package com.eventory.vipbanner.service;

import com.eventory.vipbanner.dto.VipBannerResponseDto;
import com.eventory.vipbanner.entity.VipBanner;
import com.eventory.vipbanner.repository.VipBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VipBannerService {

    private final VipBannerRepository vipBannerRepository;

    // 배너 목록 조회
    public List<VipBannerResponseDto> findVipBanners(Long expoAdminId) {
        return vipBannerRepository.findByExpoAdminIdOrderByCreatedAtDesc(expoAdminId)
                .stream()
                .map(banner -> VipBannerResponseDto.builder()
                        .vipBannerId(banner.getVipBannerId())
                        .imageUrl(banner.getImageUrl())
                        .status(banner.getStatus())
                        .reason(banner.getReason())
                        .startDate(banner.getStartDate())
                        .endDate(banner.getEndDate())
                        .build())
                .collect(Collectors.toList());
    }

    // 배너 승인
    public void updateVipBannerApprove(Long vipBannerId) {
        VipBanner banner = vipBannerRepository.findById(vipBannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));
        banner = VipBanner.builder()
                .vipBannerId(banner.getVipBannerId())
                .imageUrl(banner.getImageUrl())
                .status("승인")
                .reason(null)
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .expoAdminId(banner.getExpoAdminId())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
        vipBannerRepository.save(banner);
    }

    // 배너 거절
    public void updateVipBannerReject(Long vipBannerId, String reason) {
        VipBanner banner = vipBannerRepository.findById(vipBannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));
        banner = VipBanner.builder()
                .vipBannerId(banner.getVipBannerId())
                .imageUrl(banner.getImageUrl())
                .status("거절")
                .reason(reason)
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .expoAdminId(banner.getExpoAdminId())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
        vipBannerRepository.save(banner);
    }

    // 배너 기간 수정
    public void updateVipBannerPeriod(Long vipBannerId, String startDate, String endDate) {
        VipBanner banner = vipBannerRepository.findById(vipBannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));
        banner = VipBanner.builder()
                .vipBannerId(banner.getVipBannerId())
                .imageUrl(banner.getImageUrl())
                .status(banner.getStatus())
                .reason(banner.getReason())
                .startDate(java.time.LocalDate.parse(startDate))
                .endDate(java.time.LocalDate.parse(endDate))
                .expoAdminId(banner.getExpoAdminId())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
        vipBannerRepository.save(banner);
    }
}
