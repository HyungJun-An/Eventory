package com.eventory.expoAdmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "expoStatistics")
@Table(name = "expoStatistics")
public class ExpoStatistics {

    @Id
    @Column(name = "expo_id")
    private Long expoId;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "reservation_count", nullable = false)
    private Long reservationCount;

    @Column(name = "payment_total", nullable = false)
    private BigDecimal paymentTotal;
}
