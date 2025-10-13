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
    REVIEW_REPORT_SUBMITTED("Báo cáo đánh giá mới"),
    REVIEW_REPORT_RESOLVED("Báo cáo đánh giá được chấp thuận"),
    REVIEW_REPORT_REJECTED("Báo cáo đánh giá bị từ chối"),
    RESTAURANT_REGISTRATION_SUBMITTED("Đăng ký nhà hàng mới"),
    RESTAURANT_APPROVED("Nhà hàng được duyệt"),
    RESTAURANT_REJECTED("Nhà hàng bị từ chối"),
    RESTAURANT_RESUBMIT("Nhà hàng cần chỉnh sửa"),
    RESTAURANT_SUSPENDED("Nhà hàng bị tạm dừng"),
    RESTAURANT_ACTIVATED("Nhà hàng được kích hoạt"),
    SYSTEM_ANNOUNCEMENT("Thông báo hệ thống");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
