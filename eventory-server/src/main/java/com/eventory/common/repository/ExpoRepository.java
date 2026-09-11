package com.eventory.common.repository;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.ExpoStatus;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpoRepository extends JpaRepository<Expo, Long> {

    // 결제 완료/환불 시 동시성 제어용 — SELECT FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT e FROM expo e WHERE e.expoId = :expoId")
    Optional<Expo> findByIdWithLock(Long expoId);

    @Query("SELECT e FROM expo e " +
            "WHERE e.expoAdmin.expoAdminId = :expoAdminId " +
            "AND e.status = :status " +
            "ORDER BY e.title ASC")
    List<Expo> findByExpoAdminIdAndStatusOrderByTitleAsc(Long expoAdminId, ExpoStatus status);

    Optional<Expo> findFirstByExpoAdminOrderByCreatedAtDesc(ExpoAdmin expoAdmin);

	Page<Expo> findByExpoAdmin(ExpoAdmin expoAdmin, Pageable pageable);

	boolean existsByExpoAdmin(ExpoAdmin expoAdmin);
	
	Page<Expo> findByStatus(ExpoStatus status, Pageable pageable);
	
	Page<Expo> findByTitleContaining(String keyword, Pageable pageable);
	
	Page<Expo> findByStatusAndTitleContaining(ExpoStatus status, String keyword, Pageable pageable);

}
