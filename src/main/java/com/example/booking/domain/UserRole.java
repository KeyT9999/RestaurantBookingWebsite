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

    /**
     * Kiểm tra xem role có phải là restaurant owner không
     * Hỗ trợ cả hai format: restaurant_owner và RESTAURANT_OWNER
     */
    public boolean isRestaurantOwner() {
        return this == RESTAURANT_OWNER || this == restaurant_owner;
    }

    /**
     * Kiểm tra xem role có phải là admin không
     * Hỗ trợ cả hai format: admin và ADMIN
     */
    public boolean isAdmin() {
        return this == ADMIN || this == admin;
    }

    /**
     * Kiểm tra xem role có phải là customer không
     * Hỗ trợ cả hai format: customer và CUSTOMER
     */
    public boolean isCustomer() {
        return this == CUSTOMER || this == customer;
    }
} 