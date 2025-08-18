package com.eventory.systemadmin.controller;

import com.eventory.systemadmin.dto.BannerDetailResponseDto;
import com.eventory.systemadmin.dto.BannerUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/sys/banners")
@RequiredArgsConstructor
public class BannerControllerr {

    private final com.eventory.systemadmin.service.BannerService bannerService;

    // VIP 배너 목록 조회
    @GetMapping
    public ResponseEntity<List<BannerDetailResponseDto>> findAllBanners() {
        return ResponseEntity.ok(bannerService.findAllBanners());
    }

    // 배너 상세 조회 (클릭 수, 전환율 포함)
    @GetMapping("/{bannerId}")
    public ResponseEntity<BannerDetailResponseDto> findBannerDetail(@PathVariable Long bannerId) {
        return ResponseEntity.ok(bannerService.findBannerDetail(bannerId));
    }

    // 배너 기간 수정
    @PutMapping("/{bannerId}")
    public ResponseEntity<Void> updateBannerPeriod(
            @PathVariable Long bannerId,
            @Valid @RequestBody BannerUpdateRequestDto requestDto) {
        bannerService.updateBannerPeriod(bannerId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 배너 승인/반려
    @PutMapping("/{bannerId}/decision")
    public ResponseEntity<Void> updateBannerDecision(
            @PathVariable Long bannerId,
            @Valid @RequestBody com.eventory.systemadmin.dto.BannerDecisionRequestDto requestDto) {
        bannerService.updateBannerDecision(bannerId, requestDto);
        return ResponseEntity.ok().build();
    }
}
