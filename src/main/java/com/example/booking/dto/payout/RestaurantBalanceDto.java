package com.example.booking.dto.payout;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.booking.common.enums.CommissionType;

/**
 * DTO cho số dư nhà hàng
 */
public class RestaurantBalanceDto {
    
    private Integer restaurantId;
    private String restaurantName;
    
    // Revenue
    private BigDecimal totalRevenue;
    private Integer totalBookingsCompleted;
    
    // Commission
    private CommissionType commissionType;
    private BigDecimal commissionRate;
    private BigDecimal commissionFixedAmount;
    private BigDecimal totalCommission;
    private String commissionDisplay;
    
    // Withdrawal
    private BigDecimal totalWithdrawn;
    private BigDecimal pendingWithdrawal;
    private Integer totalWithdrawalRequests;
    
    // Balance
    private BigDecimal availableBalance;
    private boolean canWithdraw;
    private BigDecimal minimumWithdrawal = new BigDecimal("100000");
    
    // Metadata
    private LocalDateTime lastCalculatedAt;
    private LocalDateTime lastWithdrawalAt;
    
    // Constructors
    public RestaurantBalanceDto() {
    }
    
    // Helper methods
    public String getCommissionDisplay() {
        if (commissionType == CommissionType.PERCENTAGE) {
            return commissionRate + "%";
        } else {
            return String.format("%,d VNĐ/booking", commissionFixedAmount.intValue());
        }
    }
    
    public boolean isCanWithdraw() {
        return availableBalance != null && 
               availableBalance.compareTo(minimumWithdrawal) >= 0;
    }
    
    // Getters and Setters
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public Integer getTotalBookingsCompleted() {
        return totalBookingsCompleted;
    }
    
    public void setTotalBookingsCompleted(Integer totalBookingsCompleted) {
        this.totalBookingsCompleted = totalBookingsCompleted;
    }
    
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
    
    public BigDecimal getTotalCommission() {
        return totalCommission;
    }
    
    public void setTotalCommission(BigDecimal totalCommission) {
        this.totalCommission = totalCommission;
    }
    
    public BigDecimal getTotalWithdrawn() {
        return totalWithdrawn;
    }
    
    public void setTotalWithdrawn(BigDecimal totalWithdrawn) {
        this.totalWithdrawn = totalWithdrawn;
    }
    
    public BigDecimal getPendingWithdrawal() {
        return pendingWithdrawal;
    }
    
    public void setPendingWithdrawal(BigDecimal pendingWithdrawal) {
        this.pendingWithdrawal = pendingWithdrawal;
    }
    
    public Integer getTotalWithdrawalRequests() {
        return totalWithdrawalRequests;
    }
    
    public void setTotalWithdrawalRequests(Integer totalWithdrawalRequests) {
        this.totalWithdrawalRequests = totalWithdrawalRequests;
    }
    
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    
    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }
    
    public BigDecimal getMinimumWithdrawal() {
        return minimumWithdrawal;
    }
    
    public void setMinimumWithdrawal(BigDecimal minimumWithdrawal) {
        this.minimumWithdrawal = minimumWithdrawal;
    }
    
    public LocalDateTime getLastCalculatedAt() {
        return lastCalculatedAt;
    }
    
    public void setLastCalculatedAt(LocalDateTime lastCalculatedAt) {
        this.lastCalculatedAt = lastCalculatedAt;
    }
    
    public LocalDateTime getLastWithdrawalAt() {
        return lastWithdrawalAt;
    }
    
    public void setLastWithdrawalAt(LocalDateTime lastWithdrawalAt) {
        this.lastWithdrawalAt = lastWithdrawalAt;
    }
    
    public void setCanWithdraw(boolean canWithdraw) {
        this.canWithdraw = canWithdraw;
    }
    
    public void setCommissionDisplay(String commissionDisplay) {
        this.commissionDisplay = commissionDisplay;
    }
}

