package com.example.booking.dto.payout;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.booking.common.enums.WithdrawalStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho yêu cầu rút tiền
 */
public class WithdrawalRequestDto {
    
    private Integer requestId;
    private Integer restaurantId;
    private String restaurantName;
    
    @NotNull(message = "Tài khoản ngân hàng không được để trống")
    private Integer bankAccountId;
    
    private RestaurantBankAccountDto bankAccount;
    
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "100000", message = "Số tiền rút tối thiểu là 100,000 VNĐ")
    private BigDecimal amount;
    
    private String description;
    
    private WithdrawalStatus status;
    private String statusDisplay;
    
    private String reviewedByUsername;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private String adminNotes;
    
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Owner information
    private String ownerName;
    private String ownerEmail;
    
    // Bank account information
    private String bankAccountNumber;
    private String accountHolderName;
    private String bankCode;
    private String bankName;
    
    // Constructors
    public WithdrawalRequestDto() {
    }
    
    // Getters and Setters
    public Integer getRequestId() {
        return requestId;
    }
    
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }
    
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
    
    public Integer getBankAccountId() {
        return bankAccountId;
    }
    
    public void setBankAccountId(Integer bankAccountId) {
        this.bankAccountId = bankAccountId;
    }
    
    public RestaurantBankAccountDto getBankAccount() {
        return bankAccount;
    }
    
    public void setBankAccount(RestaurantBankAccountDto bankAccount) {
        this.bankAccount = bankAccount;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public WithdrawalStatus getStatus() {
        return status;
    }
    
    public void setStatus(WithdrawalStatus status) {
        this.status = status;
    }
    
    public String getStatusDisplay() {
        return statusDisplay;
    }
    
    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }
    
    public String getReviewedByUsername() {
        return reviewedByUsername;
    }
    
    public void setReviewedByUsername(String reviewedByUsername) {
        this.reviewedByUsername = reviewedByUsername;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getAdminNotes() {
        return adminNotes;
    }
    
    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
    
    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }
    
    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }
    
    public BigDecimal getNetAmount() {
        return netAmount;
    }
    
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Owner information getters and setters
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getOwnerEmail() {
        return ownerEmail;
    }
    
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
    
    // Bank account information getters and setters
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }
    
    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}

