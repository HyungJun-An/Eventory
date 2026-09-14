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

import com.eventory.systemAdmin.dto.AdminLastExpoDto;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpoRepository extends JpaRepository<Expo, Long> {

    // 결제 완료/환불 시 동시성 제어용 — SELECT FOR UPDATE
    // 락 대기 상한: MySQL 에서는 아래 힌트가 무시되므로(생성 SQL 에 반영 안 됨) application.yml 의
    // innodb_lock_wait_timeout(3초)으로 제한한다. 힌트는 이를 지원하는 DB(Oracle·PostgreSQL 등) 대비로 유지
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

	// 관리자 목록 한 페이지의 "마지막 박람회 신청 시각"을 한 번에 조회 (기존: 관리자마다 1번씩 조회하는 N+1)
	@Query("SELECT new com.eventory.systemAdmin.dto.AdminLastExpoDto(e.expoAdmin.expoAdminId, MAX(e.createdAt)) " +
			"FROM expo e WHERE e.expoAdmin IN :admins GROUP BY e.expoAdmin.expoAdminId")
	List<AdminLastExpoDto> findLastCreatedAtByAdmins(@Param("admins") Collection<ExpoAdmin> admins);
	
	Page<Expo> findByStatus(ExpoStatus status, Pageable pageable);
	
	Page<Expo> findByTitleContaining(String keyword, Pageable pageable);
	
	Page<Expo> findByStatusAndTitleContaining(ExpoStatus status, String keyword, Pageable pageable);

}
