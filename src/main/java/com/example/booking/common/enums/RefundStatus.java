package com.example.booking.common.enums;

/**
 * Enum for refund request status
 */
public enum RefundStatus {
    PENDING,        // Chờ admin xử lý
    COMPLETED,      // Đã chuyển tiền thành công
    REJECTED        // Admin từ chối
}
