package com.eventory.common.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserCategoryId {
    private Long categoryId;
    private Long userId;
}
