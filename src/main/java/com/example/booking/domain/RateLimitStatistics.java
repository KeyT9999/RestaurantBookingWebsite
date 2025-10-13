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
    
    @Column(name = "blocked_reason")
    private String blockedReason;
    
    @Column(name = "blocked_by")
    private String blockedBy;
    
    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;
    
    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "total_requests", nullable = false)
    private Long totalRequests = 0L;
    
    @Column(name = "successful_requests", nullable = false)
    private Long successfulRequests = 0L;
    
    @Column(name = "failed_requests", nullable = false)
    private Long failedRequests = 0L;
    
    @Column(name = "last_request_at")
    private LocalDateTime lastRequestAt;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "country_code", length = 2)
    private String countryCode;
    
    @Column(name = "is_suspicious", nullable = false)
    private Boolean isSuspicious = false;
    
    @Column(name = "suspicious_reason")
    private String suspiciousReason;
    
    @Column(name = "suspicious_at")
    private LocalDateTime suspiciousAt;
    
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore = 0;
    
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
    
    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }
    
    public String getBlockedBy() { return blockedBy; }
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }
    
    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }
    
    public LocalDateTime getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(LocalDateTime blockedUntil) { this.blockedUntil = blockedUntil; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(Long totalRequests) { this.totalRequests = totalRequests; }
    
    public Long getSuccessfulRequests() { return successfulRequests; }
    public void setSuccessfulRequests(Long successfulRequests) { this.successfulRequests = successfulRequests; }
    
    public Long getFailedRequests() { return failedRequests; }
    public void setFailedRequests(Long failedRequests) { this.failedRequests = failedRequests; }
    
    public LocalDateTime getLastRequestAt() { return lastRequestAt; }
    public void setLastRequestAt(LocalDateTime lastRequestAt) { this.lastRequestAt = lastRequestAt; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    
    public Boolean getIsSuspicious() { return isSuspicious; }
    public void setIsSuspicious(Boolean isSuspicious) { this.isSuspicious = isSuspicious; }
    
    public String getSuspiciousReason() { return suspiciousReason; }
    public void setSuspiciousReason(String suspiciousReason) { this.suspiciousReason = suspiciousReason; }
    
    public LocalDateTime getSuspiciousAt() { return suspiciousAt; }
    public void setSuspiciousAt(LocalDateTime suspiciousAt) { this.suspiciousAt = suspiciousAt; }
    
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    
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
    
    /**
     * Increment total requests count
     */
    public void incrementTotalRequests() {
        this.totalRequests++;
        this.lastRequestAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Increment successful requests count
     */
    public void incrementSuccessfulRequests() {
        this.successfulRequests++;
        this.lastRequestAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Increment failed requests count
     */
    public void incrementFailedRequests() {
        this.failedRequests++;
        this.lastRequestAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate success rate percentage
     */
    public double getSuccessRate() {
        if (totalRequests == 0) return 0.0;
        return (double) successfulRequests / totalRequests * 100;
    }
    
    /**
     * Calculate failure rate percentage
     */
    public double getFailureRate() {
        if (totalRequests == 0) return 0.0;
        return (double) failedRequests / totalRequests * 100;
    }
    
    /**
     * Calculate risk score based on various factors
     */
    public void calculateRiskScore() {
        int score = 0;
        
        // Base score from blocked count
        score += blockedCount * 10;
        
        // High failure rate increases risk
        if (getFailureRate() > 50) score += 20;
        if (getFailureRate() > 80) score += 30;
        
        // Suspicious patterns
        if (isSuspicious) score += 25;
        
        // Recent activity
        if (lastRequestAt != null && lastRequestAt.isAfter(LocalDateTime.now().minusMinutes(5))) {
            score += 5;
        }
        
        // Cap at 100
        this.riskScore = Math.min(score, 100);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if IP should be flagged as suspicious
     */
    public void updateSuspiciousFlag() {
        boolean suspicious = false;
        
        // High blocked count
        if (blockedCount >= 5) suspicious = true;
        
        // High failure rate
        if (getFailureRate() > 70) suspicious = true;
        
        // High risk score
        if (riskScore >= 60) suspicious = true;
        
        // Rapid requests
        if (lastRequestAt != null && lastRequestAt.isAfter(LocalDateTime.now().minusMinutes(1))) {
            if (totalRequests > 50) suspicious = true;
        }
        
        this.isSuspicious = suspicious;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get formatted success rate
     */
    public String getFormattedSuccessRate() {
        return String.format("%.1f%%", getSuccessRate());
    }
    
    /**
     * Get formatted failure rate
     */
    public String getFormattedFailureRate() {
        return String.format("%.1f%%", getFailureRate());
    }
    
    /**
     * Get risk level based on risk score
     */
    public String getRiskLevel() {
        if (riskScore >= 80) return "HIGH";
        if (riskScore >= 50) return "MEDIUM";
        if (riskScore >= 20) return "LOW";
        return "MINIMAL";
    }
    
    /**
     * Check if IP is currently blocked
     */
    public boolean isCurrentlyBlocked() {
        if (isPermanentlyBlocked) return true;
        if (blockedUntil != null && blockedUntil.isAfter(LocalDateTime.now())) return true;
        return false;
    }
    
    /**
     * Get time until unblock (in seconds)
     */
    public long getTimeUntilUnblock() {
        if (!isCurrentlyBlocked()) return 0;
        if (blockedUntil == null) return -1; // Permanently blocked
        return java.time.Duration.between(LocalDateTime.now(), blockedUntil).getSeconds();
    }
}
