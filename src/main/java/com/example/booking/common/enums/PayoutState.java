package com.example.booking.common.enums;

/**
 * Trạng thái transaction từ PayOS
 */
public enum PayoutState {
    /**
     * Giao dịch thành công
     */
    SUCCEEDED,
    
    /**
     * Giao dịch thất bại
     */
    FAILED,
    
    /**
     * Đang xử lý
     */
    PROCESSING,
    
    /**
     * Đã hủy
     */
    CANCELLED
}

