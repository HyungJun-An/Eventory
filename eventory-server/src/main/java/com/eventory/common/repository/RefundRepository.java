package com.eventory.common.repository;

import com.eventory.common.entity.Refund;
import com.eventory.common.entity.RefundStatus;
import com.eventory.expoAdmin.dto.RefundResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    @Query("""
    SELECT COUNT(r)
    FROM refund r
    JOIN r.payment p
    JOIN reservation res ON p = res.payment
    WHERE res.expo.id = :expoId
    """)
    long countRefundsByExpoId(@Param("expoId") Long expoId);

    /**
     * 박람회 환불 목록 한 페이지를 화면에 필요한 값만 담아 한 번에 조회 (status 가 null 이면 전체).
     * 기존: 박람회의 결제 id 전체를 먼저 불러와 거대한 IN 조건으로 조회한 뒤,
     * 행마다 예약을 다시 조회(+ 예약의 즉시 로딩 연관)해 10행에 쿼리 25개 이상이 나갔다.
     */
    @Query(value = """
    SELECT new com.eventory.expoAdmin.dto.RefundResponseDto(
        r.refundId, res.code, p.method, p.amount, p.paidAt, r.reason, r.status)
    FROM refund r
    JOIN r.payment p
    JOIN reservation res ON res.payment = p
    WHERE res.expo.expoId = :expoId
      AND (:status IS NULL OR r.status = :status)
    """,
    countQuery = """
    SELECT COUNT(r)
    FROM refund r
    JOIN r.payment p
    JOIN reservation res ON res.payment = p
    WHERE res.expo.expoId = :expoId
      AND (:status IS NULL OR r.status = :status)
    """)
    Page<RefundResponseDto> findRefundRowsByExpoId(@Param("expoId") Long expoId,
                                                   @Param("status") RefundStatus status,
                                                   Pageable pageable);
}
