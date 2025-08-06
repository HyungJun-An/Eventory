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
@Entity(name = "expoAdmin")
@Table(name = "expoAdmin")
@EntityListeners(AuditingEntityListener.class)
public class ExpoAdmin {

    @Id
    @Column(name = "expoAdmin_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expoAdminId;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "customer_id", length = 255, nullable = false)
    private String customerId;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "phone", length = 255, nullable = false)
    private String phone;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;
}
