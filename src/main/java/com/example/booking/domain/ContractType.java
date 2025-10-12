package com.example.booking.domain;

/**
 * Enum định nghĩa các loại hợp đồng
 */
public enum ContractType {
    STANDARD("Hợp đồng tiêu chuẩn"),
    PREMIUM("Hợp đồng cao cấp"),
    ENTERPRISE("Hợp đồng doanh nghiệp"),
    TRIAL("Hợp đồng thử nghiệm");

    private final String displayName;

    ContractType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Lấy hoa hồng mặc định theo loại hợp đồng
     */
    public java.math.BigDecimal getDefaultCommissionRate() {
        return switch (this) {
            case STANDARD -> new java.math.BigDecimal("5.00"); // 5%
            case PREMIUM -> new java.math.BigDecimal("4.50");  // 4.5%
            case ENTERPRISE -> new java.math.BigDecimal("4.00"); // 4%
            case TRIAL -> new java.math.BigDecimal("0.00");    // 0% cho thử nghiệm
        };
    }
    
    /**
     * Lấy bảo đảm tối thiểu theo loại hợp đồng
     */
    public java.math.BigDecimal getDefaultMinimumGuarantee() {
        return switch (this) {
            case STANDARD -> new java.math.BigDecimal("1000000.00"); // 1M VNĐ
            case PREMIUM -> new java.math.BigDecimal("2000000.00");  // 2M VNĐ
            case ENTERPRISE -> new java.math.BigDecimal("5000000.00"); // 5M VNĐ
            case TRIAL -> new java.math.BigDecimal("0.00");         // Không bảo đảm
        };
    }
    
    /**
     * Lấy thời hạn hợp đồng mặc định (tháng)
     */
    public int getDefaultDurationMonths() {
        return switch (this) {
            case STANDARD -> 12; // 1 năm
            case PREMIUM -> 24;  // 2 năm
            case ENTERPRISE -> 36; // 3 năm
            case TRIAL -> 3;     // 3 tháng
        };
    }
}
