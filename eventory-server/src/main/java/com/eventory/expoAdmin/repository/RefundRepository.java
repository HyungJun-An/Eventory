package com.eventory.expoAdmin.repository;

import com.eventory.expoAdmin.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    @Query("""
    SELECT COUNT(rf)
    FROM Refund rf
    WHERE rf.paymentId IN (
        SELECT r.payment.id
        FROM Reservation r
        WHERE r.expo.id = :expoId
    )
    """)
    long countRefundsByExpoId(@Param("expoId") Long expoId);
}
