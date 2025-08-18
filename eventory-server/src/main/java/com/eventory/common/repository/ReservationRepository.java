package com.eventory.common.repository;

import com.eventory.common.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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
      AND (:startDate IS NULL OR r.payment.paidAt >= :startDate)
      AND (:endDate IS NULL OR r.payment.paidAt <= :endDate)
    """)
    Page<Reservation> findByExpoIdAndReservationCode(@Param("expoId") Long expoId,
                                                     @Param("code") String code,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     Pageable pageable);

    @Query("""
    SELECT r
    FROM reservation r
    WHERE r.expo.expoId = :expoId
    """)
    List<Reservation> findByExpoIdAndReservationCode(@Param("expoId") Long expoId);

    // RESERVED 상태인 총 인원 수
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
    """)
    Long countReservedPeopleByExpoId(@Param("expoId") Long expoId);

    // 특정 구간의 예약 수 합계를 조회 (start <= createdAt < end)
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
            AND r.createdAt >= :start
            AND r.createdAt < :end
    """)
    Long sumPeopleByExpoAndCreatedBetween(@Param("expoId") Long expoId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    // 예약된 총 건수 (RESERVED)
    @Query("""
        SELECT COUNT(r)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
            AND r.createdAt >= :start
            AND r.createdAt < :end
    """)
    Long countReservations(@Param("expoId") Long expoId,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    // 예약된 인원 수 총합
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
            AND r.createdAt >= :start
            AND r.createdAt < :end
    """)
    Long sumPeople(@Param("expoId") Long expoId,
                   @Param("start") LocalDateTime start,
                   @Param("end") LocalDateTime end);

    // 결제된 금액 총합
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM reservation r
        JOIN r.payment p
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
            AND r.createdAt >= :start
            AND r.createdAt < :end
    """)
    BigDecimal sumPayments(@Param("expoId") Long expoId,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    // 취소된 예약 건수 (CANCELLED)
    @Query("""
        SELECT COUNT(r)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.CANCELLED
            AND r.createdAt >= :start
            AND r.createdAt < :end
    """)
    Long countCancelled(@Param("expoId") Long expoId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

    // 티켓 유형별(FREE/PAID) 예약 집계 (RESERVED 상태)
    // 결제 레코드가 있는 예약만 포함 (결제 레코드가 없는 무료 예약은 집계에서 제외됨(정책상 모든 예약은 결제가 존재한다는 전제일 때 사용))
    @Query("""
        SELECT
            CASE WHEN p.amount > :zero THEN 'PAID' ELSE 'FREE' END AS type,
            COUNT(r) AS reservationCount,
            COALESCE(SUM(r.people), 0) AS peopleCount
        FROM reservation r
        JOIN r.payment p
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
        GROUP BY CASE WHEN p.amount > :zero THEN 'PAID' ELSE 'FREE' END
    """)
    List<Object[]> aggregateTicketTypeCountsReserved(@Param("expoId") Long expoId, @Param("zero") BigDecimal zero);

}
