package com.eventory.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;
import java.util.ArrayList; // 필드에 new ArrayList<>()

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

    @OneToMany(mappedBy = "expo", fetch = FetchType.LAZY)
    private List<ExpoCategory> expoCategories = new ArrayList<>();

    // 호환용: 1:N 코드 getCategory() 유지
    @Transient
    public Category getCategory() {
        return expoCategories.isEmpty() ? null : expoCategories.get(0).getCategory();
    }

}
