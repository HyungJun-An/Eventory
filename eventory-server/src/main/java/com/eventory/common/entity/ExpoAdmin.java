package com.eventory.common.entity;

import com.eventory.expoAdmin.dto.ManagerRequestDto;
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
@Entity(name = "expoAdmin")
@Table(name = "expo_admin")
@EntityListeners(AuditingEntityListener.class)
public class ExpoAdmin {

    @Id
    @Column(name = "expo_admin_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expoAdminId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private UserType type;

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

    public void updateExpoAdmin(ManagerRequestDto requestDto) {
        this.name = requestDto.getName();
        this.email = requestDto.getEmail();
        this.phone = requestDto.getPhone();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void createAccount(String id, String password) {
    	this.customerId = id;
    	this.password = password;
    	this.updatedAt = LocalDateTime.now();
    }
}
