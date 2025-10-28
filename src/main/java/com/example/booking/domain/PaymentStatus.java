package com.example.booking.domain;

public enum PaymentStatus {
    PENDING("Chờ thanh toán"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Hoàn thành"),
    FAILED("Thất bại"),
    REFUNDED("Hoàn tiền"),
    REFUND_PENDING("Chờ hoàn tiền"),
    CANCELLED("Đã hủy");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
