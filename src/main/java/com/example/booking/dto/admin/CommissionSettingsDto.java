package com.example.booking.dto.admin;

import java.math.BigDecimal;

import com.example.booking.common.enums.CommissionType;

/**
 * DTO for commission settings (Admin)
 */
public class CommissionSettingsDto {
    
    private CommissionType commissionType;
    private BigDecimal commissionRate;
    private BigDecimal commissionFixedAmount;
    private BigDecimal minimumWithdrawalAmount;
    
    public CommissionSettingsDto() {}
    
    public CommissionSettingsDto(CommissionType commissionType, BigDecimal commissionRate,
                                BigDecimal commissionFixedAmount, BigDecimal minimumWithdrawalAmount) {
        this.commissionType = commissionType;
        this.commissionRate = commissionRate;
        this.commissionFixedAmount = commissionFixedAmount;
        this.minimumWithdrawalAmount = minimumWithdrawalAmount;
    }

    // Getters and Setters
    public CommissionType getCommissionType() {
        return commissionType;
    }

    public void setCommissionType(CommissionType commissionType) {
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

    public BigDecimal getMinimumWithdrawalAmount() {
        return minimumWithdrawalAmount;
    }

    public void setMinimumWithdrawalAmount(BigDecimal minimumWithdrawalAmount) {
        this.minimumWithdrawalAmount = minimumWithdrawalAmount;
    }
}

