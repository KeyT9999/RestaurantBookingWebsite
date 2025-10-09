package com.example.booking.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.booking.common.enums.WithdrawalAuditAction;

/**
 * DTO for audit log entries
 */
public class AuditLogDto {
    
    private Integer logId;
    private Integer withdrawalRequestId;
    private UUID performedByUserId;
    private String performedByUsername;
    private WithdrawalAuditAction action;
    private String notes;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime performedAt;
    
    // Additional info
    private String restaurantName;
    private String withdrawalStatus;
    
    public AuditLogDto() {}

    // Getters and Setters
    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getWithdrawalRequestId() {
        return withdrawalRequestId;
    }

    public void setWithdrawalRequestId(Integer withdrawalRequestId) {
        this.withdrawalRequestId = withdrawalRequestId;
    }

    public UUID getPerformedByUserId() {
        return performedByUserId;
    }

    public void setPerformedByUserId(UUID performedByUserId) {
        this.performedByUserId = performedByUserId;
    }

    public String getPerformedByUsername() {
        return performedByUsername;
    }

    public void setPerformedByUsername(String performedByUsername) {
        this.performedByUsername = performedByUsername;
    }

    public WithdrawalAuditAction getAction() {
        return action;
    }

    public void setAction(WithdrawalAuditAction action) {
        this.action = action;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getWithdrawalStatus() {
        return withdrawalStatus;
    }

    public void setWithdrawalStatus(String withdrawalStatus) {
        this.withdrawalStatus = withdrawalStatus;
    }
}

