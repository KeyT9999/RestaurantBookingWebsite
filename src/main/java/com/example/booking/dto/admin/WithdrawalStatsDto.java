package com.example.booking.dto.admin;

import java.math.BigDecimal;

/**
 * DTO for withdrawal statistics (Admin dashboard)
 */
public class WithdrawalStatsDto {
    
    private Long pendingCount;
    private Long processingCount;
    private Long succeededCount;
    private Long failedCount;
    private Long rejectedCount;
    
    private BigDecimal pendingAmount;
    private BigDecimal processingAmount;
    private BigDecimal succeededAmount;
    private BigDecimal totalCommission;
    
    private Double averageProcessingTimeHours;
    private Double successRate;
    
    public WithdrawalStatsDto() {}
    
    public WithdrawalStatsDto(Long pendingCount, Long processingCount, Long succeededCount, 
                             Long failedCount, Long rejectedCount, BigDecimal pendingAmount,
                             BigDecimal processingAmount, BigDecimal succeededAmount, 
                             BigDecimal totalCommission, Double averageProcessingTimeHours, 
                             Double successRate) {
        this.pendingCount = pendingCount;
        this.processingCount = processingCount;
        this.succeededCount = succeededCount;
        this.failedCount = failedCount;
        this.rejectedCount = rejectedCount;
        this.pendingAmount = pendingAmount;
        this.processingAmount = processingAmount;
        this.succeededAmount = succeededAmount;
        this.totalCommission = totalCommission;
        this.averageProcessingTimeHours = averageProcessingTimeHours;
        this.successRate = successRate;
    }

    // Getters and Setters
    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getProcessingCount() {
        return processingCount;
    }

    public void setProcessingCount(Long processingCount) {
        this.processingCount = processingCount;
    }

    public Long getSucceededCount() {
        return succeededCount;
    }

    public void setSucceededCount(Long succeededCount) {
        this.succeededCount = succeededCount;
    }

    public Long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }

    public Long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(Long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public BigDecimal getProcessingAmount() {
        return processingAmount;
    }

    public void setProcessingAmount(BigDecimal processingAmount) {
        this.processingAmount = processingAmount;
    }

    public BigDecimal getSucceededAmount() {
        return succeededAmount;
    }

    public void setSucceededAmount(BigDecimal succeededAmount) {
        this.succeededAmount = succeededAmount;
    }

    public BigDecimal getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(BigDecimal totalCommission) {
        this.totalCommission = totalCommission;
    }

    public Double getAverageProcessingTimeHours() {
        return averageProcessingTimeHours;
    }

    public void setAverageProcessingTimeHours(Double averageProcessingTimeHours) {
        this.averageProcessingTimeHours = averageProcessingTimeHours;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
}

