package com.eventory.vipbanner.controller;

import com.eventory.vipbanner.dto.VipBannerResponseDto;
import com.eventory.vipbanner.service.VipBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/vip-banners")
@RequiredArgsConstructor
public class VipBannerController {

    private final VipBannerService vipBannerService;

    // 배너 목록 조회
    @GetMapping("/{expoAdminId}")
    public ResponseEntity<List<VipBannerResponseDto>> findVipBanners(@PathVariable Long expoAdminId) {
        return ResponseEntity.ok(vipBannerService.findVipBanners(expoAdminId));
    }

    // 배너 승인
    @PutMapping("/{vipBannerId}/approve")
    public ResponseEntity<Void> updateVipBannerApprove(@PathVariable Long vipBannerId) {
        vipBannerService.updateVipBannerApprove(vipBannerId);
        return ResponseEntity.ok().build();
    }

    // 배너 거절
    @PutMapping("/{vipBannerId}/reject")
    public ResponseEntity<Void> updateVipBannerReject(@PathVariable Long vipBannerId, @RequestParam String reason) {
        vipBannerService.updateVipBannerReject(vipBannerId, reason);
        return ResponseEntity.ok().build();
    }

    // 배너 기간 수정
    @PutMapping("/{vipBannerId}/period")
    public ResponseEntity<Void> updateVipBannerPeriod(
            @PathVariable Long vipBannerId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        vipBannerService.updateVipBannerPeriod(vipBannerId, startDate, endDate);
        return ResponseEntity.ok().build();
    }
}
