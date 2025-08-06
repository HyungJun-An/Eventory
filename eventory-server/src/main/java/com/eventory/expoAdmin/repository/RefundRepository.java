package com.eventory.expoAdmin.repository;

import com.eventory.expoAdmin.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    @Query("""
    SELECT COUNT(r)
    FROM refund r
    JOIN r.payment p
    JOIN reservation res ON p = res.payment
    WHERE res.expo.id = :expoId
    """)
    long countRefundsByExpoId(@Param("expoId") Long expoId);
}
