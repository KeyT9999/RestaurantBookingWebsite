package com.example.booking.domain;

public enum TableStatus {
    AVAILABLE("Có sẵn"),
    OCCUPIED("Đang sử dụng"),
    RESERVED("Đã đặt"),
    MAINTENANCE("Bảo trì");

    private final String displayName;

    TableStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
