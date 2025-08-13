package com.eventory.common.repository;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpoRepository extends JpaRepository<Expo, Long> {

    @Query("SELECT e FROM expo e " +
            "WHERE e.expoAdmin.expoAdminId = :expoAdminId " +
            "AND e.status = :status " +
            "ORDER BY e.title ASC")
    List<Expo> findByExpoAdminIdAndStatusOrderByTitleAsc(Long expoAdminId, ExpoStatus status);
}
