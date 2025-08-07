package com.eventory.common.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class ExpoCategoryId {
    private Long categoryId;
    private Long expoId;
}
