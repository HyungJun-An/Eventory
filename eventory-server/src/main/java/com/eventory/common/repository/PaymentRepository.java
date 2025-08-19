package com.eventory.common.repository;

import com.eventory.common.entity.Payment;
import com.eventory.common.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
