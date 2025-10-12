package com.example.booking.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate_limit_alerts")
public class RateLimitAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;
    
    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;
    
    @Column(name = "alert_message", nullable = false, columnDefinition = "TEXT")
    private String alertMessage;
    
    @Column(name = "severity", nullable = false, length = 20)
    private String severity = "warning";
    
    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    // Constructors
    public RateLimitAlert() {
        this.createdAt = LocalDateTime.now();
    }
    
    public RateLimitAlert(String ipAddress, String alertType, String alertMessage, String severity) {
        this();
        this.ipAddress = ipAddress;
        this.alertType = alertType;
        this.alertMessage = alertMessage;
        this.severity = severity;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    
    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    // Helper methods
    public void resolve() {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
    }
}
