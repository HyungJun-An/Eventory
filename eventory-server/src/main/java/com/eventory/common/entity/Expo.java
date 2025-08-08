package com.eventory.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "expo")
@Table(name = "expo")
@EntityListeners(AuditingEntityListener.class)
public class Expo {

    @Id
    @Column(name = "expo_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_admin_id", nullable = false)
    private ExpoAdmin expoAdmin;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "location", length = 255, nullable = false)
    private String location;

    @Column(name = "visibility", nullable = false)
    private Boolean visibility;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "display_start_date", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime displayStartDate;

    @Column(name = "display_update_date", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime displayUpdateDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 255)
    private ExpoStatus status;

    @Column(name = "reason", length = 255, nullable = true)
    private String reason;
}
