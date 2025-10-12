package com.example.booking.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity đại diện cho hợp đồng giữa nhà hàng và Book Eat
 */
@Entity
@Table(name = "restaurant_contract")
public class RestaurantContract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Integer contractId;
    
    @Column(name = "restaurant_id", nullable = false)
    private Integer restaurantId;
    
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 20)
    private ContractType contractType = ContractType.STANDARD;
    
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("5.00"); // 5% default
    
    @Column(name = "minimum_guarantee", precision = 12, scale = 2)
    private BigDecimal minimumGuarantee; // Bảo đảm tối thiểu hàng tháng
    
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms = "Hàng tuần"; // Điều khoản thanh toán
    
    @Column(name = "contract_start_date", nullable = false)
    private LocalDateTime contractStartDate;
    
    @Column(name = "contract_end_date")
    private LocalDateTime contractEndDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContractStatus status = ContractStatus.DRAFT;
    
    @Column(name = "signed_by_owner")
    private Boolean signedByOwner = false;
    
    @Column(name = "signed_by_admin")
    private Boolean signedByAdmin = false;
    
    @Column(name = "owner_signature_date")
    private LocalDateTime ownerSignatureDate;
    
    @Column(name = "admin_signature_date")
    private LocalDateTime adminSignatureDate;
    
    @Column(name = "owner_signature_ip")
    private String ownerSignatureIp;
    
    @Column(name = "admin_signature_ip")
    private String adminSignatureIp;
    
    @Column(name = "special_terms", columnDefinition = "TEXT")
    private String specialTerms; // Điều khoản đặc biệt
    
    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason; // Lý do chấm dứt hợp đồng
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    // Constructors
    public RestaurantContract() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.contractStartDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getContractId() {
        return contractId;
    }
    
    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }
    
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    
    public ContractType getContractType() {
        return contractType;
    }
    
    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }
    
    public BigDecimal getCommissionRate() {
        return commissionRate;
    }
    
    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
    
    public BigDecimal getMinimumGuarantee() {
        return minimumGuarantee;
    }
    
    public void setMinimumGuarantee(BigDecimal minimumGuarantee) {
        this.minimumGuarantee = minimumGuarantee;
    }
    
    public String getPaymentTerms() {
        return paymentTerms;
    }
    
    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
    
    public LocalDateTime getContractStartDate() {
        return contractStartDate;
    }
    
    public void setContractStartDate(LocalDateTime contractStartDate) {
        this.contractStartDate = contractStartDate;
    }
    
    public LocalDateTime getContractEndDate() {
        return contractEndDate;
    }
    
    public void setContractEndDate(LocalDateTime contractEndDate) {
        this.contractEndDate = contractEndDate;
    }
    
    public ContractStatus getStatus() {
        return status;
    }
    
    public void setStatus(ContractStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getSignedByOwner() {
        return signedByOwner;
    }
    
    public void setSignedByOwner(Boolean signedByOwner) {
        this.signedByOwner = signedByOwner;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getSignedByAdmin() {
        return signedByAdmin;
    }
    
    public void setSignedByAdmin(Boolean signedByAdmin) {
        this.signedByAdmin = signedByAdmin;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getOwnerSignatureDate() {
        return ownerSignatureDate;
    }
    
    public void setOwnerSignatureDate(LocalDateTime ownerSignatureDate) {
        this.ownerSignatureDate = ownerSignatureDate;
    }
    
    public LocalDateTime getAdminSignatureDate() {
        return adminSignatureDate;
    }
    
    public void setAdminSignatureDate(LocalDateTime adminSignatureDate) {
        this.adminSignatureDate = adminSignatureDate;
    }
    
    public String getOwnerSignatureIp() {
        return ownerSignatureIp;
    }
    
    public void setOwnerSignatureIp(String ownerSignatureIp) {
        this.ownerSignatureIp = ownerSignatureIp;
    }
    
    public String getAdminSignatureIp() {
        return adminSignatureIp;
    }
    
    public void setAdminSignatureIp(String adminSignatureIp) {
        this.adminSignatureIp = adminSignatureIp;
    }
    
    public String getSpecialTerms() {
        return specialTerms;
    }
    
    public void setSpecialTerms(String specialTerms) {
        this.specialTerms = specialTerms;
    }
    
    public String getTerminationReason() {
        return terminationReason;
    }
    
    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
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
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    // Helper methods
    public boolean isFullySigned() {
        return Boolean.TRUE.equals(signedByOwner) && Boolean.TRUE.equals(signedByAdmin);
    }
    
    public boolean isActive() {
        return status == ContractStatus.ACTIVE && isFullySigned();
    }
    
    public boolean isExpired() {
        return contractEndDate != null && LocalDateTime.now().isAfter(contractEndDate);
    }
    
    public boolean canBeSigned() {
        return status == ContractStatus.DRAFT || status == ContractStatus.PENDING_OWNER_SIGNATURE || 
               status == ContractStatus.PENDING_ADMIN_SIGNATURE;
    }
    
    public boolean needsOwnerSignature() {
        return !Boolean.TRUE.equals(signedByOwner);
    }
    
    public boolean needsAdminSignature() {
        return !Boolean.TRUE.equals(signedByAdmin);
    }
    
    public String getStatusDisplay() {
        return status.getDisplayName();
    }
    
    public long getDaysRemaining() {
        if (contractEndDate == null) {
            return -1; // Không có ngày kết thúc
        }
        return java.time.Duration.between(LocalDateTime.now(), contractEndDate).toDays();
    }
}
