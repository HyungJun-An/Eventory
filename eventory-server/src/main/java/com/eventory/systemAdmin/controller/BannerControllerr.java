package com.eventory.systemAdmin.controller;

import com.eventory.systemAdmin.dto.BannerUpdateRequestDto;
import com.eventory.common.entity.Banner;
import com.eventory.systemAdmin.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // VIP 배너 목록 조회
    @GetMapping
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.findAllBanners());
    }

    // 배너 상세 조회
    @GetMapping("/{bannerId}")
    public ResponseEntity<Banner> getBannerDetail(@PathVariable Long bannerId) {
        return ResponseEntity.ok(bannerService.findBannerDetail(bannerId));
    }

    // 배너 기간 수정
    @PutMapping("/{bannerId}")
    public ResponseEntity<Banner> updateBannerPeriod(@PathVariable Long bannerId,
                                                     @RequestBody BannerUpdateRequestDto requestDto) {
        return ResponseEntity.ok(bannerService.updateBannerPeriod(bannerId, requestDto));
    }

    // 배너 승인/반려
    @PutMapping("/{bannerId}/approve")
    public ResponseEntity<Banner> approveBanner(@PathVariable Long bannerId,
                                                @RequestParam String status,
                                                @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(bannerService.approveBanner(bannerId, status, reason));
    }
}

