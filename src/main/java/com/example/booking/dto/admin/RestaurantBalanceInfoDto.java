package com.example.booking.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for restaurant balance info (Admin balance management)
 */
public class RestaurantBalanceInfoDto {
    
    private Integer restaurantId;
    private String restaurantName;
    private String ownerEmail;
    private String ownerPhone;
    
    private BigDecimal totalRevenue;
    private BigDecimal availableBalance;
    private BigDecimal pendingWithdrawal;
    private BigDecimal totalWithdrawn;
    private BigDecimal totalCommission;
    
    private Long totalBookingsCompleted;
    private Long totalWithdrawalRequests;
    
    private LocalDateTime lastWithdrawalAt;
    private LocalDateTime lastCalculatedAt;
    
    public RestaurantBalanceInfoDto() {}

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

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getPendingWithdrawal() {
        return pendingWithdrawal;
    }

    public void setPendingWithdrawal(BigDecimal pendingWithdrawal) {
        this.pendingWithdrawal = pendingWithdrawal;
    }

    public BigDecimal getTotalWithdrawn() {
        return totalWithdrawn;
    }

    public void setTotalWithdrawn(BigDecimal totalWithdrawn) {
        this.totalWithdrawn = totalWithdrawn;
    }

    public BigDecimal getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(BigDecimal totalCommission) {
        this.totalCommission = totalCommission;
    }

    public Long getTotalBookingsCompleted() {
        return totalBookingsCompleted;
    }

    public void setTotalBookingsCompleted(Long totalBookingsCompleted) {
        this.totalBookingsCompleted = totalBookingsCompleted;
    }

    public Long getTotalWithdrawalRequests() {
        return totalWithdrawalRequests;
    }

    public void setTotalWithdrawalRequests(Long totalWithdrawalRequests) {
        this.totalWithdrawalRequests = totalWithdrawalRequests;
    }

    public LocalDateTime getLastWithdrawalAt() {
        return lastWithdrawalAt;
    }

    public void setLastWithdrawalAt(LocalDateTime lastWithdrawalAt) {
        this.lastWithdrawalAt = lastWithdrawalAt;
    }

    public LocalDateTime getLastCalculatedAt() {
        return lastCalculatedAt;
    }

    public void setLastCalculatedAt(LocalDateTime lastCalculatedAt) {
        this.lastCalculatedAt = lastCalculatedAt;
    }
}

