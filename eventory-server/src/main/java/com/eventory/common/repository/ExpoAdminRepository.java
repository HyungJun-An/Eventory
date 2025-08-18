package com.eventory.common.repository;

import com.eventory.common.entity.ExpoAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpoAdminRepository extends JpaRepository<ExpoAdmin, Long> {
    Optional<ExpoAdmin> findByCustomerId(String customerId);
    Optional<ExpoAdmin> findByEmail(String email);
}
