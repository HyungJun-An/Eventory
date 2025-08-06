package com.eventory.expoAdmin.repository;

import com.eventory.expoAdmin.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
