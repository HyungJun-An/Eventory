package com.eventory.common.entity;

import com.eventory.expoAdmin.dto.BannerUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "banner")
@Table(name = "banner")
@EntityListeners(AuditingEntityListener.class)
public class Banner {

    @Id
    @Column(name = "banner_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bannerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = true)
    private Payment payment;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 255)
    private BannerStatus status;

    @Column(name = "reason", length = 255, nullable = true)
    private String reason;

    public void updateBanner(BannerUpdateRequestDto requestDto) {
        this.imageUrl = requestDto.getImageUrl();
        this.startDate = requestDto.getStartDate();
        this.endDate = requestDto.getEndDate();
    }
}
