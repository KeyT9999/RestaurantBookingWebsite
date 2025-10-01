package com.example.booking.dto.notification;

import java.time.LocalDateTime;

import com.example.booking.domain.NotificationType;

public class NotificationFilter {
    
    private Boolean unreadOnly;
    private NotificationType type;
    private LocalDateTime from;
    private LocalDateTime to;
    
    // Constructors
    public NotificationFilter() {
    }
    
    // Getters and Setters
    public Boolean getUnreadOnly() {
        return unreadOnly;
    }
    
    public void setUnreadOnly(Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public LocalDateTime getFrom() {
        return from;
    }
    
    public void setFrom(LocalDateTime from) {
        this.from = from;
    }
    
    public LocalDateTime getTo() {
        return to;
    }
    
    public void setTo(LocalDateTime to) {
        this.to = to;
    }
}