package com.eventory.common.repository;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoAdmin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpoRepository extends JpaRepository<Expo, Long> {
    List<Expo> findByExpoAdmin_ExpoAdminIdOrderByTitleAsc(Long expoAdminId);
    Optional<Expo> findFirstByExpoAdminOrderByCreatedAtDesc(ExpoAdmin expoAdmin);
}
