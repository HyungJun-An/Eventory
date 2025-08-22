package com.eventory.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "checkInLog")
@Table(name = "checkin_log")
@EntityListeners(AuditingEntityListener.class)
@Builder
public class CheckInLog {

    @Id
    @Column(name = "ticket_id")
    private Long ticketId; // 1회만 기록

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @CreationTimestamp
    @Column(name = "time", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime time; // 체크인 시각
}
