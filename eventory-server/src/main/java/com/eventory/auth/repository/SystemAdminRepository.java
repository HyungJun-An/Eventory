package com.eventory.auth.repository;

import com.eventory.common.entity.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SystemAdminRepository  extends JpaRepository<SystemAdmin, Long> {
    Optional<SystemAdmin> findByCustomerId(String customerId);
}
