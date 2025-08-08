package com.eventory.expoAdmin.repository;

import com.eventory.common.entity.ExpoAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpoAdminRepository extends JpaRepository<ExpoAdmin, Long> {
}
