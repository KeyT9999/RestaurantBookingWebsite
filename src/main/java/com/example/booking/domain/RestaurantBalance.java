package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.booking.common.enums.CommissionType;

import jakarta.persistence.*;

/**
 * Số dư và thống kê tài chính của nhà hàng
 */
@Entity
@Table(name = "restaurant_balance")
public class RestaurantBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Integer balanceId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", unique = true, nullable = false)
    private RestaurantProfile restaurant;
    
    // Revenue tracking
    @Column(name = "total_revenue", precision = 18, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    
    @Column(name = "total_bookings_completed")
    private Integer totalBookingsCompleted = 0;
    
    // Commission configuration
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("7.50"); // 7.5%
    
    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", length = 20)
    private CommissionType commissionType = CommissionType.PERCENTAGE;
    
    @Column(name = "commission_fixed_amount", precision = 18, scale = 2)
    private BigDecimal commissionFixedAmount = new BigDecimal("15000"); // 15k VNĐ
    
    @Column(name = "total_commission", precision = 18, scale = 2)
    private BigDecimal totalCommission = BigDecimal.ZERO;
    
    // Withdrawal tracking
    @Column(name = "total_withdrawn", precision = 18, scale = 2)
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;
    
    @Column(name = "pending_withdrawal", precision = 18, scale = 2)
    private BigDecimal pendingWithdrawal = BigDecimal.ZERO;
    
    @Column(name = "total_withdrawal_requests")
    private Integer totalWithdrawalRequests = 0;
    
    // Calculated balance
    @Column(name = "available_balance", precision = 18, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;
    
    // Metadata
    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;
    
    @Column(name = "last_withdrawal_at")
    private LocalDateTime lastWithdrawalAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public RestaurantBalance() {
        this.updatedAt = LocalDateTime.now();
        this.lastCalculatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
        this.lastCalculatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * Tính toán hoa hồng dựa trên commission_type
     */
    public BigDecimal calculateCommission() {
        if (this.commissionType == CommissionType.PERCENTAGE) {
            return this.totalRevenue.multiply(this.commissionRate.divide(new BigDecimal("100")));
        } else {
            return this.commissionFixedAmount.multiply(new BigDecimal(this.totalBookingsCompleted));
        }
    }
    
    /**
     * Tính toán và cập nhật số dư khả dụng
     */
    public void recalculateAvailableBalance() {
        this.totalCommission = calculateCommission();
        this.availableBalance = this.totalRevenue
            .subtract(this.totalCommission)
            .subtract(this.totalWithdrawn)
            .subtract(this.pendingWithdrawal);
        this.lastCalculatedAt = LocalDateTime.now();
    }
    
    /**
     * Thêm doanh thu từ booking completed
     */
    public void addRevenue(BigDecimal amount) {
        this.totalRevenue = this.totalRevenue.add(amount);
        this.totalBookingsCompleted++;
        recalculateAvailableBalance();
    }
    
    /**
     * Lock số dư khi có withdrawal pending
     */
    public void lockBalance(BigDecimal amount) {
        this.pendingWithdrawal = this.pendingWithdrawal.add(amount);
        recalculateAvailableBalance();
    }
    
    /**
     * Unlock số dư khi withdrawal bị reject/cancel
     */
    public void unlockBalance(BigDecimal amount) {
        this.pendingWithdrawal = this.pendingWithdrawal.subtract(amount);
        if (this.pendingWithdrawal.compareTo(BigDecimal.ZERO) < 0) {
            this.pendingWithdrawal = BigDecimal.ZERO;
        }
        recalculateAvailableBalance();
    }
    
    /**
     * Xác nhận withdrawal thành công
     */
    public void confirmWithdrawal(BigDecimal amount) {
        this.totalWithdrawn = this.totalWithdrawn.add(amount);
        this.pendingWithdrawal = this.pendingWithdrawal.subtract(amount);
        if (this.pendingWithdrawal.compareTo(BigDecimal.ZERO) < 0) {
            this.pendingWithdrawal = BigDecimal.ZERO;
        }
        this.totalWithdrawalRequests++;
        this.lastWithdrawalAt = LocalDateTime.now();
        recalculateAvailableBalance();
    }
    
    /**
     * Kiểm tra có đủ số dư để rút không
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return this.availableBalance.compareTo(amount) >= 0;
    }
    
    // Getters and Setters
    public Integer getBalanceId() {
        return balanceId;
    }
    
    public void setBalanceId(Integer balanceId) {
        this.balanceId = balanceId;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
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
    
    public BigDecimal getCommissionRate() {
        return commissionRate;
    }
    
    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
    
    public CommissionType getCommissionType() {
        return commissionType;
    }
    
    public void setCommissionType(CommissionType commissionType) {
        this.commissionType = commissionType;
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

