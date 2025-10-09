package com.example.booking.domain;

public enum NotificationType {
    BOOKING_CONFIRMED("Đặt bàn được xác nhận"),
    BOOKING_CANCELLED("Đặt bàn bị hủy"),
    BOOKING_REMINDER("Nhắc nhở đặt bàn"),
    PAYMENT_SUCCESS("Thanh toán thành công"),
    PAYMENT_STATUS("Trạng thái thanh toán"),
    WITHDRAWAL_STATUS("Trạng thái rút tiền"),
    VOUCHER_ASSIGNED("Nhận voucher"),
    REVIEW_REQUEST("Yêu cầu đánh giá"),
    SYSTEM_ANNOUNCEMENT("Thông báo hệ thống");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
