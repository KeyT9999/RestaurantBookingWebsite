package com.example.booking.common.enums;

/**
 * Status của yêu cầu rút tiền
 */
public enum WithdrawalStatus {
    /**
     * Đang chờ admin duyệt
     */
    PENDING,
    
    /**
     * Admin đã duyệt, chuẩn bị chuyển tiền
     */
    APPROVED,
    
    /**
     * Admin từ chối
     */
    REJECTED,
    
    /**
     * Đang xử lý chuyển tiền qua PayOS
     */
    PROCESSING,
    
    /**
     * Chuyển tiền thành công
     */
    SUCCEEDED,
    
    /**
     * Chuyển tiền thất bại
     */
    FAILED,
    
    /**
     * Đã hủy
     */
    CANCELLED;
    
    public boolean isTerminal() {
        return this == SUCCEEDED || this == FAILED || this == REJECTED || this == CANCELLED;
    }
    
    public boolean isPending() {
        return this == PENDING || this == APPROVED || this == PROCESSING;
    }
}

