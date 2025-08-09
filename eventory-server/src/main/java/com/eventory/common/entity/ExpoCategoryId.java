package com.eventory.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExpoCategoryId implements Serializable {

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "expo_id")
    private Long expoId;

    public ExpoCategoryId() {
    }

    public ExpoCategoryId(Long categoryId, Long expoId) {
        this.categoryId = categoryId;
        this.expoId = expoId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getExpoId() {
        return expoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ExpoCategoryId that))
            return false;
        return Objects.equals(categoryId, that.categoryId)
                && Objects.equals(expoId, that.expoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, expoId);
    }
}
