package com.eventory.common.entity;

public enum BoothStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String status;

    BoothStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
