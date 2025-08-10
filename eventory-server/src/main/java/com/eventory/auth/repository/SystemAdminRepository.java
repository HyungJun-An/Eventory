package com.eventory.auth.repository;

import com.eventory.common.entity.SystemAdmin;

import java.util.Optional;

public interface SystemAdminRepository {
    Optional<SystemAdmin> findByCustomerId(String customerId);
}
