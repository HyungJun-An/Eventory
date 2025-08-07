package com.eventory.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "expoCategory")
@Table(name = "expo_category")
public class ExpoCategory {

    @EmbeddedId
    private ExpoCategoryId id;

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @MapsId("expoId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;
}
