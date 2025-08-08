package com.eventory.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "bannerClickLog")
@Table(name = "banner_click_log")
public class BannerClickLog {

    @Id
    @Column(name = "banner_id")
    private Long bannerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "banner_id")
    private Banner banner;

    @Column(name = "click_count", nullable = false)
    private Long clickCount;
}
