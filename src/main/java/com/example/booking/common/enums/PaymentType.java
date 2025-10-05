package com.example.booking.common.enums;

/**
 * Enum for payment types
 * Defines whether payment is for deposit or full payment
 */
public enum PaymentType {
    DEPOSIT("Đặt cọc"),
    FULL_PAYMENT("Thanh toán toàn phần");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
