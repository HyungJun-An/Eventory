package com.eventory.auth.repository;

import com.eventory.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByCustomerId(String customerId);
    Optional<User> findByCustomerId(String customerId);
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
 
}
