package com.example.booking.domain;

public enum WaitlistStatus {
    WAITING("Đang chờ"),
    CALLED("Đã gọi"),
    SEATED("Đã ngồi"),
    CANCELLED("Đã hủy");

    private final String displayName;

    WaitlistStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
