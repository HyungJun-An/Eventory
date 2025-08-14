package com.eventory.expoAdmin.repository;

import com.eventory.expoAdmin.entity.VipBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VipBannerRepository extends JpaRepository<VipBanner, Long> {
    List<VipBanner> findByStatusOrderByCreatedAtDesc(VipBanner.BannerStatus status);
}


