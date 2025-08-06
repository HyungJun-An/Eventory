package com.eventory.expoAdmin.repository;

import com.eventory.expoAdmin.dto.YearlySalesResponseDto;
import com.eventory.expoAdmin.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
    SELECT new com.example.dto.YearlySalesResponseDto(
        YEAR(p.paidAt), SUM(p.amount)
    )
    FROM Reservation r
    JOIN Payment p ON r.payment.id = p.id
    WHERE r.expo.id = :expoId
    GROUP BY YEAR(p.paidAt)
    ORDER BY YEAR(p.paidAt)
    """)
    List<YearlySalesResponseDto> findYearlySalesByExpoId(Long expoId);
}
