package com.eventory.common.entity;

import com.eventory.expoAdmin.dto.BoothRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "booth")
@Table(name = "booth")
@EntityListeners(AuditingEntityListener.class)
public class Booth {

    @Id
    @Column(name = "booth_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boothId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "payment_id", nullable = true)
    private Payment payment;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "location", length = 255, nullable = false)
    private String location;

    @Column(name = "manager_name", length = 255, nullable = false)
    private String managerName;

    @Column(name = "department", length = 255, nullable = false)
    private String department;

    @Column(name = "phone", length = 255, nullable = false)
    private String phone;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 255, nullable = false)
    private BoothStatus status;

    @Column(name = "reason", length = 255, nullable = true)
    private String reason;

    public void updateStatus(BoothRequestDto requestDto) {
        this.status = requestDto.getStatus();
        this.reason = requestDto.getReason();
    }

    public void updateBooth(com.eventory.companyUser.dto.BoothRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.imageUrl = requestDto.getImageUrl();
        this.location = requestDto.getLocation();
        this.managerName = requestDto.getManagerName();
        this.department = requestDto.getDepartment();
        this.phone = requestDto.getPhone();
        this.email = requestDto.getEmail();
        this.updatedAt = LocalDateTime.now();
    }
}
