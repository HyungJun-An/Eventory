package com.eventory.expoAdmin.repository;

import com.eventory.common.entity.Reservation;
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

    @Query("SELECT r.payment.paymentId FROM reservation r WHERE r.expo.expoId = :expoId")
    List<Long> findPaymentIdsByExpoId(@Param("expoId") Long expoId);

    Optional<Reservation> findByPayment_PaymentId(Long paymentId);

    // RESERVED 상태인 총 인원 수
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
    """)
    Long countReservedPeopleByExpoId(@Param("expoId") Long expoId);

    // 특정 날짜에 생성된 예약 수 합계를 조회
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
            AND DATE(r.createdAt) = :date
    """)
    Long countByExpoIdAndCreatedDate(@Param("expoId") Long expoId, @Param("date") LocalDate date);

    // 특정 기간 동안 생성된 예약 수 합계를 조회
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
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
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
            AND r.createdAt
            BETWEEN :start AND :end
    """)
    Long countReservations(Long expoId, LocalDate start, LocalDate end);

    // 예약된 인원 수 총합
    @Query("""
        SELECT COALESCE(SUM(r.people), 0)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
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
            AND r.status = com.eventory.common.entity.ReservationStatus.RESERVED
            AND r.createdAt
            BETWEEN :start AND :end
    """)
    BigDecimal sumPayments(Long expoId, LocalDate start, LocalDate end);

    // 취소된 예약 건수 (CANCELLED)
    @Query("""
        SELECT COUNT(r)
        FROM reservation r
        WHERE r.expo.expoId = :expoId
            AND r.status = com.eventory.common.entity.ReservationStatus.CANCELLED
            AND r.createdAt
            BETWEEN :start AND :end
    """)
    Long countCancelled(Long expoId, LocalDate start, LocalDate end);

    /**
     * [목적]
     *  - 특정 박람회(expoId)의 "RESERVED 상태" 예약만 대상으로
     *  - "결제 금액 p.amount" 기준으로 FREE/PAID로 분류하여
     *    1) type(FREE/PAID), 2) 예약 건수, 3) 총 인원수를 집계한다.
     *
     * [포함 범위]
     *  - INNER JOIN r.payment p : 결제 레코드가 있는 예약만 포함.
     *  - 결제 레코드가 없는 무료 예약은 집계에서 제외됨(정책상 모든 예약은 결제가 존재한다는 전제일 때 사용).
     *
     * [FREE/PAID 판정]
     *  - BigDecimal 비교를 타입 안전하게 하기 위해 :zero(BigDecimal.ZERO)를 파라미터로 받음.
     *  - p.amount > :zero  → 'PAID'
     *  - 그 외(<= :zero)   → 'FREE'
     *
     * [NULL 처리]
     *  - INNER JOIN이므로 p가 NULL일 수 없음 → NULL 분기는 필요 없음.
     *
     * [GROUP BY 일관성]
     *  - SELECT에 쓰인 CASE 표현식과 동일한 CASE 표현식을 GROUP BY에도 반복해 안정성 확보.
     */
    // 티켓 유형별(FREE/PAID) 예약 집계 (RESERVED 상태)
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
