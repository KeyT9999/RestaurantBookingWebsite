package com.example.booking.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.booking.domain.VoucherStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VoucherEditForm {
    
    @NotNull
    private Integer voucherId;
    
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Discount type is required")
    private String discountType;
    
    @DecimalMin(value = "0.0", message = "Discount value must be positive")
    private BigDecimal discountValue;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer globalUsageLimit;
    
    private Integer perCustomerLimit;
    
    @DecimalMin(value = "0.0", message = "Minimum order amount must be positive")
    private BigDecimal minOrderAmount;
    
    @DecimalMin(value = "0.0", message = "Maximum discount amount must be positive")
    private BigDecimal maxDiscountAmount;
    
    @NotNull(message = "Status is required")
    private String status;
    
    // Constructors
    public VoucherEditForm() {}
    
    // Getters and Setters
    public Integer getVoucherId() {
        return voucherId;
    }
    
    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDiscountType() {
        return discountType;
    }
    
    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public Integer getGlobalUsageLimit() {
        return globalUsageLimit;
    }
    
    public void setGlobalUsageLimit(Integer globalUsageLimit) {
        this.globalUsageLimit = globalUsageLimit;
    }
    
    public Integer getPerCustomerLimit() {
        return perCustomerLimit;
    }
    
    public void setPerCustomerLimit(Integer perCustomerLimit) {
        this.perCustomerLimit = perCustomerLimit;
    }
    
    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }
    
    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }
    
    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }
    
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
