package com.eventory.common.repository;

import com.eventory.common.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
    SELECT YEAR(p.paidAt), SUM(p.amount)
    FROM reservation r
    JOIN r.payment p
    WHERE r.expo.expoId = :expoId
    GROUP BY YEAR(p.paidAt)
    ORDER BY YEAR(p.paidAt)
    """)
    List<Object[]> findYearlySalesByExpoId(@Param("expoId") Long expoId);

    @Query("""
    SELECT MONTH(p.paidAt), SUM(p.amount)
    FROM reservation r
    JOIN r.payment p
    WHERE r.expo.expoId = :expoId
      AND YEAR(p.paidAt) = :year
    GROUP BY MONTH(p.paidAt)
    ORDER BY MONTH(p.paidAt)
    """)
    List<Object[]> findMonthySalesByExpoId(@Param("expoId") Long expoId, int year);

    @Query("""
    SELECT DATE(p.paidAt), SUM(p.amount)
    FROM reservation r
    JOIN r.payment p
    WHERE r.expo.expoId = :expoId
      AND p.paidAt >= :startDate
      AND p.paidAt < :endDate
    GROUP BY DATE(p.paidAt)
    ORDER BY DATE(p.paidAt)
    """)
    List<Object[]> findDailySalesLast7Days(@Param("expoId") Long expoId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("""
    SELECT r.payment.paymentId
    FROM reservation r
    WHERE r.expo.expoId = :expoId
    """)
    List<Long> findPaymentIdsByExpoId(@Param("expoId") Long expoId);

    Optional<Reservation> findByPayment_PaymentId(Long paymentId);

    @Query("""
    SELECT r
    FROM reservation r
    WHERE r.expo.expoId = :expoId
      AND (:code IS NULL OR r.code = :code)
    """)
    Page<Reservation> findByExpoIdAndReservationCode(@Param("expoId") Long expoId,
                                                     @Param("code") String code,
                                                     Pageable pageable);

    @Query("""
    SELECT r
    FROM reservation r
    WHERE r.expo.expoId = :expoId
      AND (:code IS NULL OR r.code = :code)
    """)
    List<Reservation> findByExpoIdAndReservationCode(@Param("expoId") Long expoId,
                                                     @Param("code") String code);
}
