package com.eventory.auth.repository;

import com.eventory.common.entity.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemAdminRepository  extends JpaRepository<SystemAdmin, Long> {
    Optional<SystemAdmin> findByCustomerId(String customerId);
}
