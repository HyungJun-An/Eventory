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
@Entity(name = "systemAdmin")
@Table(name = "system_admin")
public class SystemAdmin {

    @Id
    @Column(name = "system_admin_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long systemAdminId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private userType type;

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
}
