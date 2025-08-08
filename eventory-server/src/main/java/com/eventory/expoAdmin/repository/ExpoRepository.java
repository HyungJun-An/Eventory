package com.eventory.expoAdmin.repository;

import com.eventory.common.entity.Expo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpoRepository extends JpaRepository<Expo, Long> {
    List<Expo> findByExpoAdmin_ExpoAdminIdOrderByTitleAsc(Long expoAdminId);
}
