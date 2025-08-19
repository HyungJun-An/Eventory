package com.eventory.common.entity;

import com.eventory.companyUser.dto.UserRequestDto;
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
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user")
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
@Builder
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private UserType userType;

    @Column(name = "customer_id", length = 255, nullable = false)
    private String customerId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "birth", length = 255, nullable = true)
    private LocalDate birth;

    @Column(name = "gender", length = 10, nullable = true)
    private String gender;

    @Column(name = "phone", length = 255, nullable = false)
    private String phone;

    @Column(name = "company_name_kr", length = 255, nullable = true)
    private String companyNameKr;

    @Column(name = "company_name_eng", length = 255, nullable = true)
    private String companyNameEng;

    @Column(name = "ceo_name_kr", length = 255, nullable = true)
    private String ceoNameKr;

    @Column(name = "ceo_name_eng", length = 255, nullable = true)
    private String ceoNameEng;

    @Column(name = "company_address", length = 255, nullable = true)
    private String companyAddress;

    @Column(name = "registration_num", length = 255, nullable = true)
    private String registrationNum;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    public void updateUser(UserRequestDto requestDto) {
        this.name = requestDto.getName();
        this.updatedAt = LocalDateTime.now();
    }
}
