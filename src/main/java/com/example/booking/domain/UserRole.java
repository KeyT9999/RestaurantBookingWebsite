package com.example.booking.domain;

public enum UserRole {
    CUSTOMER("customer", "Khách hàng"),
    RESTAURANT_OWNER("restaurant_owner", "Chủ nhà hàng"),
    ADMIN("admin", "Quản trị viên");

    private final String value;
    private final String displayName;

    UserRole(String value, String displayName) {
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