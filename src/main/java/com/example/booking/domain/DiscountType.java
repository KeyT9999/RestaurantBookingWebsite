package com.example.booking.domain;

public enum DiscountType {
    PERCENT("Phần trăm"),
    FIXED("Cố định");

    private final String displayName;

    DiscountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
