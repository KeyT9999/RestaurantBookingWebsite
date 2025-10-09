package com.example.booking.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration cho Payout system
 */
@Configuration
@ConfigurationProperties(prefix = "payout")
public class PayoutConfiguration {
    
    /**
     * Số tiền rút tối thiểu (VNĐ)
     */
    private BigDecimal minimumWithdrawalAmount = new BigDecimal("100000"); // 100k VNĐ
    
    /**
     * Số lần rút tối đa mỗi ngày
     */
    private int maxWithdrawalsPerDay = 3;
    
    /**
     * Số lượng tài khoản ngân hàng tối đa mỗi restaurant
     */
    private int maxBankAccountsPerRestaurant = 5;
    
    /**
     * Loại hoa hồng: PERCENTAGE hoặc FIXED
     */
    private String commissionType = "PERCENTAGE";
    
    /**
     * Tỷ lệ hoa hồng (%) - nếu commissionType = PERCENTAGE
     */
    private BigDecimal commissionRate = new BigDecimal("7.50"); // 7.5%
    
    /**
     * Phí cố định (VNĐ) - nếu commissionType = FIXED
     */
    private BigDecimal commissionFixedAmount = new BigDecimal("15000"); // 15k VNĐ
    
    /**
     * Enable/disable auto approve (chỉ dùng cho testing)
     */
    private boolean autoApprove = false;
    
    /**
     * Retry payout sau bao nhiêu phút nếu stuck ở PROCESSING
     */
    private int retryAfterMinutes = 30;
    
    // Getters and Setters
    public BigDecimal getMinimumWithdrawalAmount() {
        return minimumWithdrawalAmount;
    }
    
    public void setMinimumWithdrawalAmount(BigDecimal minimumWithdrawalAmount) {
        this.minimumWithdrawalAmount = minimumWithdrawalAmount;
    }
    
    public int getMaxWithdrawalsPerDay() {
        return maxWithdrawalsPerDay;
    }
    
    public void setMaxWithdrawalsPerDay(int maxWithdrawalsPerDay) {
        this.maxWithdrawalsPerDay = maxWithdrawalsPerDay;
    }
    
    public int getMaxBankAccountsPerRestaurant() {
        return maxBankAccountsPerRestaurant;
    }
    
    public void setMaxBankAccountsPerRestaurant(int maxBankAccountsPerRestaurant) {
        this.maxBankAccountsPerRestaurant = maxBankAccountsPerRestaurant;
    }
    
    public String getCommissionType() {
        return commissionType;
    }
    
    public void setCommissionType(String commissionType) {
        this.commissionType = commissionType;
    }
    
    public BigDecimal getCommissionRate() {
        return commissionRate;
    }
    
    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
    
    public BigDecimal getCommissionFixedAmount() {
        return commissionFixedAmount;
    }
    
    public void setCommissionFixedAmount(BigDecimal commissionFixedAmount) {
        this.commissionFixedAmount = commissionFixedAmount;
    }
    
    public boolean isAutoApprove() {
        return autoApprove;
    }
    
    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }
    
    public int getRetryAfterMinutes() {
        return retryAfterMinutes;
    }
    
    public void setRetryAfterMinutes(int retryAfterMinutes) {
        this.retryAfterMinutes = retryAfterMinutes;
    }
}

