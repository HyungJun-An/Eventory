package com.eventory.expoAdmin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "expo_statistics")
@Table(name = "expo_statistics")
public class ExpoStatistics {

    @Id
    @Column(name = "expo_id")
    private Long expoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "expo_id")
    private Expo expo;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "reservation_count", nullable = false)
    private Long reservationCount;

    @Column(name = "payment_total", nullable = false)
    private BigDecimal paymentTotal;
}
