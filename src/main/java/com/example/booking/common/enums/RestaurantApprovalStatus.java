package com.example.booking.common.enums;

/**
 * Status của yêu cầu đăng ký nhà hàng
 */
public enum RestaurantApprovalStatus {
    /**
     * Đang chờ admin duyệt
     */
    PENDING("Chờ duyệt"),
    
    /**
     * Admin đã duyệt
     */
    APPROVED("Đã duyệt"),
    
    /**
     * Admin từ chối
     */
    REJECTED("Bị từ chối"),
    
    /**
     * Tạm dừng hoạt động
     */
    SUSPENDED("Tạm dừng");
    
    private final String displayName;
    
    RestaurantApprovalStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Kiểm tra xem trạng thái có phải là terminal (kết thúc) không
     */
    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == SUSPENDED;
    }
    
    /**
     * Kiểm tra xem trạng thái có đang chờ xử lý không
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * Kiểm tra xem trạng thái có thể được duyệt không
     */
    public boolean canBeApproved() {
        return this == PENDING;
    }
    
    /**
     * Kiểm tra xem trạng thái có thể bị từ chối không
     */
    public boolean canBeRejected() {
        return this == PENDING || this == APPROVED;
    }
    
    /**
     * Kiểm tra xem trạng thái có thể bị tạm dừng không
     */
    public boolean canBeSuspended() {
        return this == APPROVED;
    }
}
