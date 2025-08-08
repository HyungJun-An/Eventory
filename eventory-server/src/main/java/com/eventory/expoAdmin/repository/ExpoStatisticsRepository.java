package com.eventory.expoAdmin.repository;

import com.eventory.common.entity.ExpoStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpoStatisticsRepository extends JpaRepository<ExpoStatistics, Long> {
    Optional<ExpoStatistics> findById(Long expoId);
}
