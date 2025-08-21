package com.eventory.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "payment")
@Table(name = "payment")
@Builder
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "method", length = 255, nullable = false)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 255)
    private PaymentStatus status;

    @CreationTimestamp
    @Column(name = "paid_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime paidAt;

    // ★ PortOne 결제 식별자 (브라우저 SDK에서 받은 paymentId)
    @Column(name = "portone_payment_id", length = 64, unique = true)
    private String portonePaymentId;

    // 편의 메서드
    public void markRefunded() { this.status = PaymentStatus.REFUNDED; }
}
