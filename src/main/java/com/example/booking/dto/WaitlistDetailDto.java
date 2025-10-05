package com.example.booking.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.booking.domain.WaitlistStatus;

/**
 * DTO for detailed waitlist information
 * Used for displaying waitlist details and editing
 */
public class WaitlistDetailDto {
    
    private Integer waitlistId;
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Integer restaurantId;
    private String restaurantName;
    private Integer partySize;
    private LocalDateTime joinTime;
    private WaitlistStatus status;
    private Integer estimatedWaitTime;
    private Integer queuePosition;
    private String specialRequests;
    private String notes;
    private LocalDateTime lastUpdated;
    
    // Constructors
    public WaitlistDetailDto() {}
    
    public WaitlistDetailDto(Integer waitlistId, UUID customerId, String customerName, 
                            String customerPhone, String customerEmail, Integer restaurantId, 
                            String restaurantName, Integer partySize, LocalDateTime joinTime, 
                            WaitlistStatus status, Integer estimatedWaitTime, Integer queuePosition) {
        this.waitlistId = waitlistId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.partySize = partySize;
        this.joinTime = joinTime;
        this.status = status;
        this.estimatedWaitTime = estimatedWaitTime;
        this.queuePosition = queuePosition;
    }
    
    // Getters and Setters
    public Integer getWaitlistId() {
        return waitlistId;
    }
    
    public void setWaitlistId(Integer waitlistId) {
        this.waitlistId = waitlistId;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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
    
    public Integer getPartySize() {
        return partySize;
    }
    
    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }
    
    public LocalDateTime getJoinTime() {
        return joinTime;
    }
    
    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
    
    public WaitlistStatus getStatus() {
        return status;
    }
    
    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }
    
    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }
    
    public void setEstimatedWaitTime(Integer estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }
    
    public Integer getQueuePosition() {
        return queuePosition;
    }
    
    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
    
    public String getSpecialRequests() {
        return specialRequests;
    }
    
    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    // Helper methods
    public String getStatusDisplayName() {
        if (status == null) return "Unknown";
        
        switch (status) {
            case WAITING:
                return "Đang chờ";
            case CALLED:
                return "Đã gọi";
            case SEATED:
                return "Đã xếp chỗ";
            case CANCELLED:
                return "Đã hủy";
            default:
                return "Unknown";
        }
    }
    
    public String getStatusColor() {
        if (status == null) return "secondary";
        
        switch (status) {
            case WAITING:
                return "warning";
            case CALLED:
                return "info";
            case SEATED:
                return "success";
            case CANCELLED:
                return "danger";
            default:
                return "secondary";
        }
    }
    
    public boolean isEditableByCustomer() {
        return status == WaitlistStatus.WAITING;
    }
    
    public boolean isEditableByRestaurant() {
        return status == WaitlistStatus.WAITING || status == WaitlistStatus.CALLED;
    }
}
