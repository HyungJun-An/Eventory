package com.eventory.common.entity;

import com.eventory.expoAdmin.dto.RefundRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "refund")
@Table(name = "refund")
@EntityListeners(AuditingEntityListener.class)
public class Refund {

    @Id
    @Column(name = "refund_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 255)
    private RefundStatus status;

    @Column(name = "reason", length = 255, nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at", columnDefinition = "TIMESTAMP", nullable = true)
    private LocalDateTime approvedAt;

    public void updateStatus(RefundRequestDto requestDto) {
        this.status = requestDto.getStatus();
        this.reason = requestDto.getReason();
        this.approvedAt = LocalDateTime.now();
    }
}
