package com.example.booking.domain;

/**
 * Enum định nghĩa trạng thái hợp đồng
 */
public enum ContractStatus {
    DRAFT("Bản nháp"),
    PENDING_OWNER_SIGNATURE("Chờ chủ nhà hàng ký"),
    PENDING_ADMIN_SIGNATURE("Chờ admin ký"),
    ACTIVE("Đang hiệu lực"),
    EXPIRED("Hết hạn"),
    TERMINATED("Chấm dứt"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ContractStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Kiểm tra xem hợp đồng có đang hoạt động không
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Kiểm tra xem hợp đồng có thể ký không
     */
    public boolean canBeSigned() {
        return this == DRAFT || this == PENDING_OWNER_SIGNATURE || this == PENDING_ADMIN_SIGNATURE;
    }
    
    /**
     * Kiểm tra xem hợp đồng có thể chấm dứt không
     */
    public boolean canBeTerminated() {
        return this == ACTIVE;
    }
    
    /**
     * Kiểm tra xem hợp đồng có thể hủy không
     */
    public boolean canBeCancelled() {
        return this == DRAFT || this == PENDING_OWNER_SIGNATURE || this == PENDING_ADMIN_SIGNATURE;
    }
    
    /**
     * Lấy màu hiển thị cho trạng thái
     */
    public String getColorClass() {
        return switch (this) {
            case DRAFT -> "text-muted";
            case PENDING_OWNER_SIGNATURE, PENDING_ADMIN_SIGNATURE -> "text-warning";
            case ACTIVE -> "text-success";
            case EXPIRED -> "text-info";
            case TERMINATED, CANCELLED -> "text-danger";
        };
    }
    
    /**
     * Lấy icon cho trạng thái
     */
    public String getIcon() {
        return switch (this) {
            case DRAFT -> "fas fa-edit";
            case PENDING_OWNER_SIGNATURE -> "fas fa-user-clock";
            case PENDING_ADMIN_SIGNATURE -> "fas fa-user-shield";
            case ACTIVE -> "fas fa-check-circle";
            case EXPIRED -> "fas fa-clock";
            case TERMINATED -> "fas fa-times-circle";
            case CANCELLED -> "fas fa-ban";
        };
    }
}
