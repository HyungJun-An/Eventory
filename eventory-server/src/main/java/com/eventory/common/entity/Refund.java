package com.eventory.common.entity;

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

    /** 요청 없이 바로 완료된 환불 이력 (예: 관리자가 예약자 명단에서 직접 취소) */
    public static Refund approvedOf(Payment payment, String reason) {
        Refund refund = new Refund();
        refund.payment = payment;
        refund.status = RefundStatus.APPROVED;
        refund.reason = reason;
        refund.approvedAt = LocalDateTime.now();
        return refund;
    }

    public boolean isPending() {
        return status == RefundStatus.PENDING;
    }

    /** 승인 — 사용자가 적은 환불 사유는 그대로 유지한다 */
    public void approve() {
        this.status = RefundStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    /** 반려 — 사유를 관리자의 반려 사유로 교체한다 */
    public void reject(String rejectReason) {
        this.status = RefundStatus.REJECTED;
        this.reason = rejectReason;
        this.approvedAt = LocalDateTime.now();
    }
}
