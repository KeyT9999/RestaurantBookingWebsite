package com.example.booking.common.enums;

public enum TableStatus {
    AVAILABLE("available", "Có sẵn"),
    OCCUPIED("occupied", "Đang sử dụng"),
    RESERVED("reserved", "Đã đặt"),
    CLEANING("cleaning", "Đang dọn dẹp"),
    MAINTENANCE("maintenance", "Bảo trì");

    private final String value;
    private final String displayName;

    TableStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
