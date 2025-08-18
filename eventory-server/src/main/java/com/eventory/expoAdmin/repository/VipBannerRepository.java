package com.eventory.vipbanner.repository;

import com.eventory.vipbanner.entity.VipBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VipBannerRepository extends JpaRepository<VipBanner, Long> {
    List<VipBanner> findByExpoAdminIdOrderByCreatedAtDesc(Long expoAdminId);
}
