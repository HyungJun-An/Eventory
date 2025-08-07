package com.eventory.expoAdmin.repository;

import com.eventory.common.entity.Expo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpoRepository extends JpaRepository<Expo, Long> {
    List<Expo> findByExpoAdmin_ExpoAdminIdOrderByTitleAsc(Long expoAdminId);
}
