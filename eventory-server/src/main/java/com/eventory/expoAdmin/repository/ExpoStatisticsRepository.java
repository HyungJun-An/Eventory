package com.eventory.expoAdmin.repository;

import com.eventory.expoAdmin.entity.ExpoStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpoStatisticsRepository extends JpaRepository<ExpoStatistics, Long> {
    Optional<ExpoStatistics> findById(Long expoId);
}
