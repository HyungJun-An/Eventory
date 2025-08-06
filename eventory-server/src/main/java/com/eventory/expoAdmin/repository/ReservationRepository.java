package com.eventory.expoAdmin.repository;

import com.eventory.expoAdmin.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
