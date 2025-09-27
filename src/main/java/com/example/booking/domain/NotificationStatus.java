package com.example.booking.domain;

public enum NotificationStatus {
    PENDING("Chờ xử lý"),
    SENT("Đã gửi"),
    READ("Đã đọc"),
    FAILED("Thất bại");

    private final String displayName;

    NotificationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
