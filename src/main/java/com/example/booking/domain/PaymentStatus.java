package com.example.booking.domain;

public enum PaymentStatus {
    PENDING("Chờ thanh toán"),
    COMPLETED("Hoàn thành"),
    FAILED("Thất bại"),
    REFUNDED("Hoàn tiền");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
