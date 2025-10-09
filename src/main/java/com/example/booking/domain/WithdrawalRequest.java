package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.booking.common.enums.WithdrawalStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Yêu cầu rút tiền từ nhà hàng
 */
@Entity
@Table(name = "withdrawal_request")
public class WithdrawalRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @NotNull(message = "Nhà hàng không được để trống")
    private RestaurantProfile restaurant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    @NotNull(message = "Tài khoản ngân hàng không được để trống")
    private RestaurantBankAccount bankAccount;
    
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "100000", message = "Số tiền rút tối thiểu là 100,000 VNĐ")
    private BigDecimal amount;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WithdrawalStatus status = WithdrawalStatus.PENDING;
    
    // Admin review
    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
    
    // Financial
    @Column(name = "commission_amount", precision = 18, scale = 2)
    private BigDecimal commissionAmount = BigDecimal.ZERO;
    
    @Column(name = "net_amount", precision = 18, scale = 2)
    private BigDecimal netAmount;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Manual transfer fields (for manual payment process)
    @Column(name = "manual_transfer_ref", length = 100)
    private String manualTransferRef;
    
    @Column(name = "manual_transferred_at")
    private LocalDateTime manualTransferredAt;
    
    @Column(name = "manual_transferred_by")
    private UUID manualTransferredBy;
    
    @Column(name = "manual_note", columnDefinition = "TEXT")
    private String manualNote;
    
    @Column(name = "manual_proof_url", columnDefinition = "TEXT")
    private String manualProofUrl;
    
    // Constructors
    public WithdrawalRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Set net_amount if not already set
        if (this.netAmount == null && this.amount != null) {
            this.netAmount = this.amount.subtract(
                this.commissionAmount != null ? this.commissionAmount : BigDecimal.ZERO
            );
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public void approve(UUID adminUserId, String notes) {
        this.status = WithdrawalStatus.APPROVED;
        this.reviewedByUserId = adminUserId;
        this.reviewedAt = LocalDateTime.now();
        this.adminNotes = notes;
    }
    
    public void reject(UUID adminUserId, String reason) {
        this.status = WithdrawalStatus.REJECTED;
        this.reviewedByUserId = adminUserId;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }
    
    public void markAsProcessing() {
        this.status = WithdrawalStatus.PROCESSING;
    }
    
    public void markAsSucceeded() {
        this.status = WithdrawalStatus.SUCCEEDED;
    }
    
    public void markAsFailed(String reason) {
        this.status = WithdrawalStatus.FAILED;
        this.rejectionReason = reason;
    }
    
    public boolean canBeApproved() {
        return this.status == WithdrawalStatus.PENDING;
    }
    
    public boolean canBeRejected() {
        return this.status == WithdrawalStatus.PENDING;
    }
    
    public boolean canBeCancelled() {
        return this.status == WithdrawalStatus.PENDING;
    }
    
    // Getters and Setters
    public Integer getRequestId() {
        return requestId;
    }
    
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
    }
    
    public RestaurantBankAccount getBankAccount() {
        return bankAccount;
    }
    
    public void setBankAccount(RestaurantBankAccount bankAccount) {
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
    
    public UUID getReviewedByUserId() {
        return reviewedByUserId;
    }
    
    public void setReviewedByUserId(UUID reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
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
    
    // Manual transfer fields getters and setters
    public String getManualTransferRef() {
        return manualTransferRef;
    }
    
    public void setManualTransferRef(String manualTransferRef) {
        this.manualTransferRef = manualTransferRef;
    }
    
    public LocalDateTime getManualTransferredAt() {
        return manualTransferredAt;
    }
    
    public void setManualTransferredAt(LocalDateTime manualTransferredAt) {
        this.manualTransferredAt = manualTransferredAt;
    }
    
    public UUID getManualTransferredBy() {
        return manualTransferredBy;
    }
    
    public void setManualTransferredBy(UUID manualTransferredBy) {
        this.manualTransferredBy = manualTransferredBy;
    }
    
    public String getManualNote() {
        return manualNote;
    }
    
    public void setManualNote(String manualNote) {
        this.manualNote = manualNote;
    }
    
    public String getManualProofUrl() {
        return manualProofUrl;
    }
    
    public void setManualProofUrl(String manualProofUrl) {
        this.manualProofUrl = manualProofUrl;
    }
}

