package com.eventory.common.repository;

import com.eventory.common.entity.ExpoAdmin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpoAdminRepository extends JpaRepository<ExpoAdmin, Long> {
    Optional<ExpoAdmin> findByCustomerId(String customerId);
    
    Page<ExpoAdmin> findByNameContainingOrPhoneContainingOrEmailContaining(String nameKeyword, String phoneKeyword, String emailKeyword, Pageable pageable);

	void deleteByCustomerId(String CustomerId);

    Optional<ExpoAdmin> findByEmail(String email);

    Optional<ExpoAdmin> findByExpoAdminId(Long expoAdminId);
}
