package com.eventory.common.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class BookmarkId implements Serializable {
    private Long expoId;
    private Long userId;
}
