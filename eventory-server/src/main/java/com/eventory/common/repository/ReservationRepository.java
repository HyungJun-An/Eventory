package com.eventory.common.repository;

import com.eventory.common.entity.Reservation;
import com.eventory.systemAdmin.dto.ChartResponseDto;

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
    List<Reservation> findByExpoIdAndReservation(@Param("expoId") Long expoId);

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

    // 예약자 명단
    // 네이티브 쿼리(nativeQuery=true)를 사용해서 통계용 집계를 한 번에 가져옴
    // 결과는 Projection(ReservationRowProjection)으로 매핑, 정렬은 created_at DESC 고정
    @Query(
            value = """
        SELECT
          r.reservation_id   AS reservationId,
          r.code             AS code,
          u.name             AS userName,
          u.phone            AS phone,
          p.amount           AS payAmount,
          p.status           AS payStatus,
          r.created_at       AS reservedAt,
          COUNT(t.ticket_id) AS totalTickets,
          SUM(CASE WHEN t.status = TRUE THEN 1 ELSE 0 END) AS checkedCount,
          MAX(cl.time)       AS lastCheckinAt
        FROM reservation r
          JOIN `user` u           ON u.user_id = r.user_id
          JOIN payment p          ON p.payment_id = r.payment_id
          LEFT JOIN qr_code q     ON q.reservation_id = r.reservation_id
          LEFT JOIN ticket t      ON t.qr_id = q.qr_id
          LEFT JOIN checkin_log cl ON cl.ticket_id = t.ticket_id
        WHERE r.expo_id = :expoId
          AND r.status <> 'CANCELLED'
          AND p.status <> 'REFUNDED'
          AND (:search IS NULL
               OR r.code  LIKE CONCAT('%', :search, '%')
               OR u.name  LIKE CONCAT('%', :search, '%')
               OR u.phone LIKE CONCAT('%', :search, '%')
          )
        GROUP BY r.reservation_id
        HAVING
           (:status = 'ALL')
        OR (:status = 'CHECKED_IN'
            AND SUM(CASE WHEN t.status THEN 1 ELSE 0 END) = COUNT(t.ticket_id)
            AND COUNT(t.ticket_id) > 0)
        OR (:status = 'NOT_CHECKED_IN'
            AND NOT (
               SUM(CASE WHEN t.status THEN 1 ELSE 0 END) = COUNT(t.ticket_id)
               AND COUNT(t.ticket_id) > 0
            ))
        ORDER BY r.created_at DESC
      """,
            countQuery = """
        SELECT COUNT(*) FROM (
          SELECT r.reservation_id
          FROM reservation r
            JOIN `user` u           ON u.user_id = r.user_id
            JOIN payment p          ON p.payment_id = r.payment_id
            LEFT JOIN qr_code q     ON q.reservation_id = r.reservation_id
            LEFT JOIN ticket t      ON t.qr_id = q.qr_id
          WHERE r.expo_id = :expoId
            AND r.status <> 'CANCELLED'
            AND p.status <> 'REFUNDED'
            AND (:search IS NULL
                 OR r.code  LIKE CONCAT('%', :search, '%')
                 OR u.name  LIKE CONCAT('%', :search, '%')
                 OR u.phone LIKE CONCAT('%', :search, '%')
            )
          GROUP BY r.reservation_id
          HAVING
             (:status = 'ALL')
          OR (:status = 'CHECKED_IN'
              AND SUM(CASE WHEN t.status THEN 1 ELSE 0 END) = COUNT(t.ticket_id)
              AND COUNT(t.ticket_id) > 0)
          OR (:status = 'NOT_CHECKED_IN'
              AND NOT (
                 SUM(CASE WHEN t.status THEN 1 ELSE 0 END) = COUNT(t.ticket_id)
                 AND COUNT(t.ticket_id) > 0
              ))
        ) x
      """,
            nativeQuery = true
    )
    Page<ReservationRowProjection> findListDesc(@Param("expoId") Long expoId,
                                                @Param("status") String status,
                                                @Param("search") String search,
                                                Pageable pageable);


    // --- Projection (이 파일 안에 둬서 파일 수 최소화) ---
    // 네이티브 쿼리 결과를 인터페이스 기반으로 매핑
    // 엔티티를 만들지 않고도 가벼운 결과 매핑이 가능, 불필요한 컬럼 로딩 방지
    interface ReservationRowProjection {
        Long getReservationId();
        String getCode();
        String getUserName();
        String getPhone();
        BigDecimal getPayAmount();
        String getPayStatus(); // "PAID" / "REFUNDED"
        LocalDateTime getReservedAt();
        Integer getTotalTickets();
        Integer getCheckedCount();
        LocalDateTime getLastCheckinAt();
    }
    
    // 일별
    @Query(value = "SELECT DATE(r.created_at) AS date, COUNT(*) AS uv " +
            "FROM reservation r " +
            "GROUP BY DATE(r.created_at)",
            nativeQuery = true)
    List<ChartResponseDto> countDailyReservations();


	// 주별
    @Query(value = "SELECT YEARWEEK(r.created_at, 1) AS date, COUNT(*) AS uv " +
            "FROM reservation r " +
            "GROUP BY YEARWEEK(r.created_at, 1) " +
            "ORDER BY date",
            nativeQuery = true)
    List<ChartResponseDto> countWeeklyReservations();


	// 월별
	@Query(value = "SELECT DATE_FORMAT(r.created_at, '%Y-%m') AS date, COUNT(*) AS uv " +
            "FROM reservation r " +
            "GROUP BY DATE_FORMAT(r.created_at, '%Y-%m') " +
            "ORDER BY date",
            nativeQuery = true)
	List<ChartResponseDto> countMonthlyReservations();

}
