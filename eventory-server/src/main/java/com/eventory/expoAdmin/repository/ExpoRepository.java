package com.eventory.expoAdmin.repository;

import com.eventory.expoAdmin.entity.Expo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpoRepository extends JpaRepository<Expo, Long> {
    List<Expo> findByExpoAdminIdOrderByTitleAsc(Long expoAdminId);
}
