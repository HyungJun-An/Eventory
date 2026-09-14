package com.eventory.common.repository;

import com.eventory.common.entity.Payment;
import com.eventory.common.entity.PaymentStatus;
import com.eventory.systemAdmin.dto.ChartResponseDto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByStatus(PaymentStatus status);

    // 플랫폼 총 결제 금액 — 엔티티를 불러오지 않고 DB 에서 합계만 계산
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    // 일별
    @Query(value = "SELECT DATE(p.paid_at) AS date, SUM(p.amount) AS uv " +
            "FROM payment p " +
            "GROUP BY DATE(p.paid_at)",
            nativeQuery = true)
    List<ChartResponseDto> countDailyPayments();


	// 주별
    @Query(value = "SELECT YEARWEEK(p.paid_at, 1) AS date, SUM(p.amount) AS uv " +
            "FROM payment p " +
            "GROUP BY YEARWEEK(p.paid_at, 1) " +
            "ORDER BY date",
            nativeQuery = true)
    List<ChartResponseDto> countWeeklyPayments();


	// 월별
	@Query(value = "SELECT DATE_FORMAT(p.paid_at, '%Y-%m') AS date, SUM(p.amount) AS uv " +
            "FROM payment p " +
            "GROUP BY DATE_FORMAT(p.paid_at, '%Y-%m') " +
            "ORDER BY date",
            nativeQuery = true)
	List<ChartResponseDto> countMonthlyPayments();
}
