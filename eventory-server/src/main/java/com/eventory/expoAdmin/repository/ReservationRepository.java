package com.eventory.expoAdmin.repository;

import com.eventory.common.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = 'RESERVED'
    """)
    Long countReservedPeopleByExpoId(@Param("expoId") Long expoId);

    // 특정 날짜에 생성된 예약 수 합계를 조회
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = 'RESERVED'
            AND DATE(r.createdAt) = :date
    """)
    Long countByExpoIdAndCreatedDate(@Param("expoId") Long expoId, @Param("date") LocalDate date);

    // 특정 기간 동안 생성된 예약 수 합계를 조회
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = 'RESERVED'
            AND r.createdAt
            BETWEEN :start AND :end
    """)
    Long countByExpoIdAndDateRange(@Param("expoId") Long expoId,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);

    // 예약된 총 건수 (RESERVED)
    @Query("""
        SELECT COUNT(r)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = 'RESERVED'
            AND r.createdAt
            BETWEEN :start AND :end
    """)
    Long countReservations(Long expoId, LocalDate start, LocalDate end);

    // 예약된 인원 수 총합
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = 'RESERVED'
            AND r.createdAt
            BETWEEN :start AND :end
    """)
    Long sumPeople(Long expoId, LocalDate start, LocalDate end);

    // 결제된 금액 총합
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM reservation r
        JOIN r.payment p
        WHERE r.expo.expoId = :expoId
            AND r.status = 'RESERVED'
            AND r.createdAt
            BETWEEN :start AND :end
    """)
    BigDecimal sumPayments(Long expoId, LocalDate start, LocalDate end);

    // 취소된 예약 건수 (CANCELLED)
    @Query("""
        SELECT COUNT(r)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = 'CANCELLED'
            AND r.createdAt
            BETWEEN :start AND :end
    """)
    Long countCancelled(Long expoId, LocalDate start, LocalDate end);

}
