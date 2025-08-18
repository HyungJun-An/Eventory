package com.eventory.common.entity;

public enum ExpoStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String status;

    ExpoStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
