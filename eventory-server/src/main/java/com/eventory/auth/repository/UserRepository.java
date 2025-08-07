package com.eventory.auth.repository;

import com.eventory.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByCustomerId(String customerId);
    boolean existsByEmail(String email);
    Optional<User> findByCustomerId(String customerId);
}
