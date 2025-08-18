package com.eventory.common.repository;

import com.eventory.common.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    Optional<Banner> findByExpo_ExpoId(@Param("expoId") Long expoId);
}
