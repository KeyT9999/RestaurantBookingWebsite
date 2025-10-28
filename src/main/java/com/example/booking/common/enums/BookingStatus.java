package com.example.booking.common.enums;

public enum BookingStatus {
    PENDING("pending", "Chờ xác nhận"),
    CONFIRMED("confirmed", "Đã xác nhận"),
    COMPLETED("completed", "Hoàn thành"),
    PENDING_CANCEL("pending_cancel", "Chờ hủy (đang xử lý hoàn tiền)"),
    CANCELLED("cancelled", "Đã hủy"),
    NO_SHOW("no_show", "Không đến");

    private final String value;
    private final String displayName;

    BookingStatus(String value, String displayName) {
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
