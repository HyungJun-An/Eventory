package com.eventory.expoAdmin.controller;

import com.eventory.expoAdmin.entity.VipBanner;
import com.eventory.expoAdmin.service.VipBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/vip-banners")
@RequiredArgsConstructor
public class VipBannerController {

    private final VipBannerService vipBannerService;

    @GetMapping
    public ResponseEntity<List<VipBanner>> getAllBanners() {
        return ResponseEntity.ok(vipBannerService.getAllBanners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VipBanner> getBanner(@PathVariable Long id) {
        return ResponseEntity.ok(vipBannerService.getBannerById(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveBanner(@PathVariable Long id) {
        vipBannerService.approveBanner(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectBanner(@PathVariable Long id, @RequestParam String reason) {
        vipBannerService.rejectBanner(id, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/period")
    public ResponseEntity<Void> updatePeriod(
            @PathVariable Long id,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        vipBannerService.updateBannerPeriod(id, start, end);
        return ResponseEntity.ok().build();
    }
}
