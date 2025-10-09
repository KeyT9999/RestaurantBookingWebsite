package com.example.booking.common.enums;

/**
 * Enum for withdrawal audit actions (manual withdrawal flow)
 */
public enum WithdrawalAuditAction {
    
    CREATE_REQUEST("Tạo yêu cầu rút tiền"),
    MARK_PAID("Đánh dấu đã chi"),
    REJECT("Từ chối yêu cầu"),
    EDIT_NOTE("Sửa ghi chú");
    
    private final String description;
    
    WithdrawalAuditAction(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return name() + " - " + description;
    }
}
