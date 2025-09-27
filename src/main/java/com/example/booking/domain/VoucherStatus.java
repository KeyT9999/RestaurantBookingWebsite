package com.example.booking.domain;

public enum VoucherStatus {
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động"),
    EXPIRED("Hết hạn");

    private final String displayName;

    VoucherStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
