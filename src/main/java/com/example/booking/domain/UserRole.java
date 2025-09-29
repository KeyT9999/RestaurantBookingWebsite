package com.example.booking.domain;

public enum UserRole {
    // Enum constants với tên khớp database values (lowercase)
    admin("admin", "Quản trị viên"),
    customer("customer", "Khách hàng"), 
    restaurant_owner("restaurant_owner", "Chủ nhà hàng"),
    
    // Giữ constants cũ để backward compatibility (uppercase)
    ADMIN("admin", "Quản trị viên"),
    CUSTOMER("customer", "Khách hàng"),
    RESTAURANT_OWNER("restaurant_owner", "Chủ nhà hàng");

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