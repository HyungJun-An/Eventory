package com.eventory.expoAdmin.repository;

import com.eventory.common.entity.Refund;
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

    List<Refund> findByPayment_PaymentIdIn(List<Long> paymentIds);
}
