package com.example.booking.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event class for audit logging
 * Represents an audit event that needs to be logged
 */
public class AuditEvent {
    
    private Long userId;
    private String username;
    private String userRole;
    private AuditAction action;
    private String resourceType;
    private String resourceId;
    private Integer restaurantId;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private boolean success = true;
    private String errorMessage;
    private Long executionTimeMs;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    
    // Constructors
    public AuditEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AuditEvent(AuditAction action, String resourceType, String resourceId) {
        this();
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    // Builder pattern for easy construction
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private AuditEvent event = new AuditEvent();
        
        public Builder userId(Long userId) {
            event.userId = userId;
            return this;
        }
        
        public Builder username(String username) {
            event.username = username;
            return this;
        }
        
        public Builder userRole(String userRole) {
            event.userRole = userRole;
            return this;
        }
        
        public Builder action(AuditAction action) {
            event.action = action;
            return this;
        }
        
        public Builder resourceType(String resourceType) {
            event.resourceType = resourceType;
            return this;
        }
        
        public Builder resourceId(String resourceId) {
            event.resourceId = resourceId;
            return this;
        }
        
        public Builder restaurantId(Integer restaurantId) {
            event.restaurantId = restaurantId;
            return this;
        }
        
        public Builder oldValues(Map<String, Object> oldValues) {
            event.oldValues = oldValues;
            return this;
        }
        
        public Builder newValues(Map<String, Object> newValues) {
            event.newValues = newValues;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            event.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            event.userAgent = userAgent;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            event.sessionId = sessionId;
            return this;
        }
        
        public Builder success(boolean success) {
            event.success = success;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            event.errorMessage = errorMessage;
            return this;
        }
        
        public Builder executionTimeMs(Long executionTimeMs) {
            event.executionTimeMs = executionTimeMs;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            event.metadata = metadata;
            return this;
        }
        
        public AuditEvent build() {
            return event;
        }
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    public AuditAction getAction() {
        return action;
    }
    
    public void setAction(AuditAction action) {
        this.action = action;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public Map<String, Object> getOldValues() {
        return oldValues;
    }
    
    public void setOldValues(Map<String, Object> oldValues) {
        this.oldValues = oldValues;
    }
    
    public Map<String, Object> getNewValues() {
        return newValues;
    }
    
    public void setNewValues(Map<String, Object> newValues) {
        this.newValues = newValues;
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
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "AuditEvent{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", userRole='" + userRole + '\'' +
                ", action=" + action +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", restaurantId=" + restaurantId +
                ", success=" + success +
                ", timestamp=" + timestamp +
                '}';
    }
}
