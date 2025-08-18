package com.eventory.common.repository;

import com.eventory.common.entity.Refund;
import com.eventory.common.entity.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    Page<Refund> findByPayment_PaymentIdIn(List<Long> paymentIds, Pageable pageable);

    Page<Refund> findByPayment_PaymentIdInAndStatus(List<Long> paymentIds, RefundStatus status, Pageable pageable);
}
