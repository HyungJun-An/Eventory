package com.eventory.systemadmin.service;

import com.eventory.common.entity.Banner;
import com.eventory.common.entity.BannerStatus;
import com.eventory.common.repository.BannerRepository;
import com.eventory.systemadmin.dto.BannerDetailResponseDto;
import com.eventory.systemadmin.dto.BannerUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
 /// //
@Service
@RequiredArgsConstructor
@Transactional
public class BannerService {

    private final BannerRepository bannerRepository;

    // VIP 배너 목록 조회
    @Transactional(readOnly = true)
    public List<BannerDetailResponseDto> findAllBanners() {
        return bannerRepository.findAll().stream()
                .map(banner -> BannerDetailResponseDto.builder()
                        .bannerId(banner.getBannerId())
                        .imageUrl(banner.getImageUrl())
                        .status(banner.getStatus())
                        .reason(banner.getReason())
                        .startDate(banner.getStartDate())
                        .endDate(banner.getEndDate())
                        .clickCount(0L)      // TODO: 추후 통계 서비스 연동
                        .conversionRate(0.0) // TODO: 추후 통계 서비스 연동
                        .build())
                .collect(Collectors.toList());
    }

    // 특정 배너 상세 조회 (클릭수/전환율 포함)
    @Transactional(readOnly = true)
    public BannerDetailResponseDto findBannerDetail(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));

        return BannerDetailResponseDto.builder()
                .bannerId(banner.getBannerId())
                .imageUrl(banner.getImageUrl())
                .status(banner.getStatus())
                .reason(banner.getReason())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .clickCount(123L)       // TODO: 실제 통계 데이터로 대체
                .conversionRate(2.5)    // TODO: 실제 수수료 전환율 계산
                .build();
    }

    // 배너 기간 수정
    public void updateBannerPeriod(Long bannerId, BannerUpdateRequestDto requestDto) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));
        banner.updateBanner(requestDto);
        bannerRepository.save(banner);
    }

    // 배너 승인/반려
    public void updateBannerDecision(Long bannerId, com.eventory.systemadmin.dto.BannerDecisionRequestDto requestDto) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));

        if ("APPROVED".equalsIgnoreCase(requestDto.getStatus())) {
            banner = Banner.builder()
                    .bannerId(banner.getBannerId())
                    .expo(banner.getExpo())
                    .payment(banner.getPayment())
                    .imageUrl(banner.getImageUrl())
                    .startDate(banner.getStartDate())
                    .endDate(banner.getEndDate())
                    .status(BannerStatus.APPROVED)
                    .reason(null)
                    .createdAt(banner.getCreatedAt())
                    .updatedAt(banner.getUpdatedAt())
                    .build();
        } else if ("REJECTED".equalsIgnoreCase(requestDto.getStatus())) {
            banner = Banner.builder()
                    .bannerId(banner.getBannerId())
                    .expo(banner.getExpo())
                    .payment(banner.getPayment())
                    .imageUrl(banner.getImageUrl())
                    .startDate(banner.getStartDate())
                    .endDate(banner.getEndDate())
                    .status(BannerStatus.REJECTED)
                    .reason(requestDto.getReason())
                    .createdAt(banner.getCreatedAt())
                    .updatedAt(banner.getUpdatedAt())
                    .build();
        } else {
            throw new IllegalArgumentException("잘못된 상태 값입니다. (APPROVED / REJECTED)");
        }

        bannerRepository.save(banner);
    }
}

