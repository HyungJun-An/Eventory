package com.eventory.common.repository;

import com.eventory.common.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByQrCode_QrId(Long qrId);
}