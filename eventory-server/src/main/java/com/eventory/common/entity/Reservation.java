package com.eventory.common.entity;

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
@Entity(name = "reservation")
@Table(name = "reservation")
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @Column(name = "reservation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @OneToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 255, nullable = false)
    private ReservationStatus status;

    @Column(name = "code", length = 255, nullable = false)
    private String code;

    @Column(name = "people", nullable = false)
    private Integer people;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;
}
