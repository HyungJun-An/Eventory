package com.eventory.common.repository;

import com.eventory.common.entity.QrCode;
import com.eventory.common.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    Optional<QrCode> findByReservation(Reservation reservation);
    Optional<QrCode> findByReservation_ReservationId(Long reservationId);
}