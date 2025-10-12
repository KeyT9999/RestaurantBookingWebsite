 package com.example.booking.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate_limit_statistics")
public class RateLimitStatistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ip_address", nullable = false, unique = true, length = 45)
    private String ipAddress;
    
    @Column(name = "blocked_count", nullable = false)
    private Integer blockedCount = 0;
    
    @Column(name = "first_blocked_at")
    private LocalDateTime firstBlockedAt;
    
    @Column(name = "last_blocked_at")
    private LocalDateTime lastBlockedAt;
    
    @Column(name = "is_permanently_blocked", nullable = false)
    private Boolean isPermanentlyBlocked = false;
    
    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public RateLimitStatistics() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public RateLimitStatistics(String ipAddress) {
        this();
        this.ipAddress = ipAddress;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Integer getBlockedCount() { return blockedCount; }
    public void setBlockedCount(Integer blockedCount) { this.blockedCount = blockedCount; }
    
    public LocalDateTime getFirstBlockedAt() { return firstBlockedAt; }
    public void setFirstBlockedAt(LocalDateTime firstBlockedAt) { this.firstBlockedAt = firstBlockedAt; }
    
    public LocalDateTime getLastBlockedAt() { return lastBlockedAt; }
    public void setLastBlockedAt(LocalDateTime lastBlockedAt) { this.lastBlockedAt = lastBlockedAt; }
    
    public Boolean getIsPermanentlyBlocked() { return isPermanentlyBlocked; }
    public void setIsPermanentlyBlocked(Boolean isPermanentlyBlocked) { this.isPermanentlyBlocked = isPermanentlyBlocked; }
    
    public LocalDateTime getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(LocalDateTime blockedUntil) { this.blockedUntil = blockedUntil; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public void incrementBlockedCount() {
        this.blockedCount++;
        this.lastBlockedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (this.firstBlockedAt == null) {
            this.firstBlockedAt = LocalDateTime.now();
        }
    }
    
    public void resetBlockedCount() {
        this.blockedCount = 0;
        this.firstBlockedAt = null;
        this.lastBlockedAt = null;
        this.isPermanentlyBlocked = false;
        this.blockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }
}
