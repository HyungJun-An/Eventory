package com.eventory.expoAdmin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "vipBanner")
@Table(name = "vip_banner")
@EntityListeners(AuditingEntityListener.class)
public class VipBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Long bannerId;

    @Column(name = "expo_id", nullable = false)
    private Long expoId; // 어떤 박람회에 속하는 배너인지

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BannerStatus status; // 대기, 승인, 거절

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum BannerStatus {
        WAITING, APPROVED, REJECTED
    }

    public void approve() {
        this.status = BannerStatus.APPROVED;
        this.rejectReason = null;
    }

    public void reject(String reason) {
        this.status = BannerStatus.REJECTED;
        this.rejectReason = reason;
    }

    public void updatePeriod(LocalDateTime start, LocalDateTime end) {
        this.startDate = start;
        this.endDate = end;
    }
}

