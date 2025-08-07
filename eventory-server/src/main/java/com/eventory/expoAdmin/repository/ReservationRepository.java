package com.eventory.expoAdmin.repository;

import com.eventory.common.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    // RESERVED 상태인 총 인원 수
    @Query("SELECT COALESCE(SUM(r.people), 0) FROM reservation r WHERE r.expo.expoId = :expoId AND r.status = 'RESERVED'")
    Long countReservedPeopleByExpoId(@Param("expoId") Long expoId);
}
