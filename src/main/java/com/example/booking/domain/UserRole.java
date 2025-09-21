package com.example.booking.domain;

public enum UserRole {
    CUSTOMER("Khách hàng"),
    RESTAURANT("Nhà hàng"),
    ADMIN("Quản trị viên");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 