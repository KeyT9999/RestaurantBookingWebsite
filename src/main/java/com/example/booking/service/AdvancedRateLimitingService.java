package com.example.booking.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.domain.RequestPattern;
import com.example.booking.domain.SuspiciousActivity;
import com.example.booking.repository.RateLimitStatisticsRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Advanced Rate Limiting Service with intelligent threat detection
 */
@Service
@Transactional
public class AdvancedRateLimitingService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedRateLimitingService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    private RateLimitStatisticsRepository statisticsRepository;
    
    @Autowired
    private DatabaseRateLimitingService databaseService;
    
    @Autowired
    private RateLimitingMonitoringService monitoringService;
    
    // Advanced configuration
    @Value("${rate.limit.advanced.suspicious-detection:true}")
    private boolean suspiciousDetectionEnabled;
    
    @Value("${rate.limit.advanced.risk-score-high:80}")
    private int riskScoreHigh;
    
    @Value("${rate.limit.advanced.risk-score-medium:50}")
    private int riskScoreMedium;
    
    @Value("${rate.limit.advanced.risk-score-low:20}")
    private int riskScoreLow;
    
    @Value("${rate.limit.advanced.auto-block-enabled:true}")
    private boolean autoBlockEnabled;
    
    @Value("${rate.limit.advanced.auto-block-threshold:15}")
    private int autoBlockThreshold;
    
    @Value("${rate.limit.advanced.monitoring-enabled:true}")
    private boolean monitoringEnabled;
    
    @Value("${rate.limit.advanced.alert-threshold:5}")
    private int alertThreshold;
    
    // In-memory tracking for rapid detection
    private final ConcurrentHashMap<String, RequestPattern> requestPatterns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SuspiciousActivity> suspiciousActivities = new ConcurrentHashMap<>();
    
    /**
     * Check if request should be allowed with advanced threat detection
     */
    public boolean isRequestAllowed(HttpServletRequest request, HttpServletResponse response, String operationType) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestPath = request.getRequestURI();
        
        logger.info("🔍 ADVANCED RATE LIMIT CHECK - IP: {}, Operation: {}, Path: {}, Time: {}", 
                clientIp, operationType, requestPath, LocalDateTime.now().format(formatter));
        
        // Get or create statistics
        RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp)
                .orElse(new RateLimitStatistics(clientIp));
        
        // Update basic statistics
        stats.incrementTotalRequests();
        stats.setLastRequestAt(LocalDateTime.now());
        stats.setUserAgent(userAgent);
        
        // Check for suspicious patterns
        if (suspiciousDetectionEnabled) {
            SuspiciousActivity suspiciousActivity = analyzeSuspiciousActivity(clientIp, request, operationType);
            if (suspiciousActivity != null) {
                stats.setIsSuspicious(true);
                stats.incrementFailedRequests();
                
                logger.warn("🚨 SUSPICIOUS ACTIVITY DETECTED - IP: {}, Type: {}, Details: {}", 
                        clientIp, suspiciousActivity.getType(), suspiciousActivity.getDetails());
                
                // Log to monitoring
                if (monitoringEnabled) {
                    monitoringService.logBlockedRequest(clientIp, requestPath, userAgent);
                    databaseService.logBlockedRequest(clientIp, requestPath, userAgent, operationType);
                }
                
                // Auto-block if threshold exceeded
                if (autoBlockEnabled && stats.getBlockedCount() >= autoBlockThreshold) {
                    autoBlockIp(clientIp, "Suspicious activity detected: " + suspiciousActivity.getType());
                }
                
                statisticsRepository.save(stats);
                return false;
            }
        }
        
        // Check basic rate limiting
        boolean basicAllowed = checkBasicRateLimit(stats, operationType);
        if (!basicAllowed) {
            stats.incrementBlockedCount();
            stats.incrementFailedRequests();
            
            logger.warn("🚫 RATE LIMIT EXCEEDED - IP: {}, Operation: {}, Blocked Count: {}", 
                    clientIp, operationType, stats.getBlockedCount());
            
            // Log to monitoring
            if (monitoringEnabled) {
                monitoringService.logBlockedRequest(clientIp, requestPath, userAgent);
                databaseService.logBlockedRequest(clientIp, requestPath, userAgent, operationType);
            }
            
            // Auto-block if threshold exceeded
            if (autoBlockEnabled && stats.getBlockedCount() >= autoBlockThreshold) {
                autoBlockIp(clientIp, "Rate limit exceeded multiple times");
            }
            
            statisticsRepository.save(stats);
            return false;
        }
        
        // Request allowed
        stats.incrementSuccessfulRequests();
        stats.calculateRiskScore();
        stats.updateSuspiciousFlag();
        
        statisticsRepository.save(stats);
        
        logger.info("✅ REQUEST ALLOWED - IP: {}, Operation: {}, Success Rate: {}", 
                clientIp, operationType, stats.getFormattedSuccessRate());
        
        addAdvancedHeaders(response, stats);
        return true;
    }
    
    /**
     * Analyze suspicious activity patterns
     */
    private SuspiciousActivity analyzeSuspiciousActivity(String clientIp, HttpServletRequest request, String operationType) {
        String userAgent = request.getHeader("User-Agent");
        String requestPath = request.getRequestURI();
        
        // Track request patterns
        RequestPattern pattern = requestPatterns.computeIfAbsent(clientIp, k -> new RequestPattern());
        pattern.addRequest(requestPath, userAgent, LocalDateTime.now());
        
        // Check for rapid requests
        if (pattern.getRequestsInLastMinute() > 100) {
            return new SuspiciousActivity("RAPID_REQUESTS", 
                    "Too many requests in short time: " + pattern.getRequestsInLastMinute());
        }
        
        // Check for bot-like behavior
        if (isBotLikeUserAgent(userAgent)) {
            return new SuspiciousActivity("BOT_LIKE_BEHAVIOR", 
                    "Suspicious user agent: " + userAgent);
        }
        
        // Check for repeated failed attempts
        RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp).orElse(null);
        if (stats != null && stats.getFailureRate() > 80) {
            return new SuspiciousActivity("HIGH_FAILURE_RATE", 
                    "Failure rate too high: " + stats.getFormattedFailureRate());
        }
        
        // Check for unusual request patterns
        if (pattern.hasUnusualPattern()) {
            return new SuspiciousActivity("UNUSUAL_PATTERN", 
                    "Unusual request pattern detected");
        }
        
        return null;
    }
    
    /**
     * Check basic rate limiting based on operation type
     */
    private boolean checkBasicRateLimit(RateLimitStatistics stats, String operationType) {
        // This would integrate with existing rate limiting services
        // For now, return true - actual implementation would check against configured limits
        return true;
    }
    
    /**
     * Auto-block IP address
     */
    private void autoBlockIp(String clientIp, String reason) {
        logger.warn("🔒 AUTO-BLOCKING IP - IP: {}, Reason: {}, Time: {}", 
                clientIp, reason, LocalDateTime.now().format(formatter));
        
        databaseService.blockIpPermanently(clientIp, reason, "SYSTEM", "Auto-blocked due to suspicious activity");
        
        // Create alert
        if (monitoringEnabled) {
            monitoringService.logBlockedRequest(clientIp, "AUTO_BLOCK", "System");
        }
    }
    
    /**
     * Check if user agent looks like a bot
     */
    private boolean isBotLikeUserAgent(String userAgent) {
        if (userAgent == null) return true;
        
        String[] botPatterns = {
            "bot", "crawler", "spider", "scraper", "curl", "wget", 
            "python-requests", "java", "go-http", "okhttp"
        };
        
        String lowerUserAgent = userAgent.toLowerCase();
        for (String pattern : botPatterns) {
            if (lowerUserAgent.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Add advanced headers to response
     */
    private void addAdvancedHeaders(HttpServletResponse response, RateLimitStatistics stats) {
        response.setHeader("X-RateLimit-Risk-Score", String.valueOf(stats.getRiskScore()));
        response.setHeader("X-RateLimit-Risk-Level", stats.getRiskLevel());
        response.setHeader("X-RateLimit-Success-Rate", stats.getFormattedSuccessRate());
        response.setHeader("X-RateLimit-Suspicious", String.valueOf(stats.getIsSuspicious()));
    }
    
    /**
     * Get threat intelligence for an IP
     */
    public Map<String, Object> getThreatIntelligence(String clientIp) {
        Map<String, Object> intelligence = new HashMap<>();
        
        RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp).orElse(null);
        if (stats != null) {
            intelligence.put("riskScore", stats.getRiskScore());
            intelligence.put("riskLevel", stats.getRiskLevel());
            intelligence.put("successRate", stats.getFormattedSuccessRate());
            intelligence.put("failureRate", stats.getFormattedFailureRate());
            intelligence.put("isSuspicious", stats.getIsSuspicious());
            intelligence.put("blockedCount", stats.getBlockedCount());
            intelligence.put("isCurrentlyBlocked", stats.isCurrentlyBlocked());
            intelligence.put("timeUntilUnblock", stats.getTimeUntilUnblock());
        }
        
        RequestPattern pattern = requestPatterns.get(clientIp);
        if (pattern != null) {
            intelligence.put("requestsInLastMinute", pattern.getRequestsInLastMinute());
            intelligence.put("hasUnusualPattern", pattern.hasUnusualPattern());
        }
        
        SuspiciousActivity suspiciousActivity = suspiciousActivities.get(clientIp);
        if (suspiciousActivity != null) {
            intelligence.put("suspiciousActivity", suspiciousActivity);
        }
        
        return intelligence;
    }
    
    /**
     * Clean up old data
     */
    public void cleanupOldData() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        
        // Clean up request patterns older than 24 hours
        requestPatterns.entrySet().removeIf(entry -> 
                entry.getValue().getLastRequestTime().isBefore(cutoffTime));
        
        // Clean up suspicious activities older than 24 hours
        suspiciousActivities.entrySet().removeIf(entry -> 
                entry.getValue().getTimestamp().isBefore(cutoffTime));
        
        logger.info("🧹 CLEANUP COMPLETED - Cleaned up old rate limiting data");
    }
    
    /**
     * Check rate limit for IP and operation type
     * Returns true if allowed, false if blocked
     */
    public boolean checkRateLimit(String clientIp, String operationType) {
        if (clientIp == null) {
            logger.error("❌ NULL IP - Cannot check rate limit");
            return false;
        }
        
        logger.info("🔍 CHECK RATE LIMIT - IP: {}, Operation: {}, Time: {}", 
                clientIp, operationType, LocalDateTime.now().format(formatter));
        
        // Get or create statistics
        RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp)
                .orElse(new RateLimitStatistics(clientIp));
        
        // Increment total requests
        stats.incrementTotalRequests();
        stats.setLastRequestAt(LocalDateTime.now());
        
        // Check if IP is currently blocked
        if (stats.isCurrentlyBlocked()) {
            stats.incrementBlockedCount();
            stats.incrementFailedRequests();
            statisticsRepository.save(stats);
            
            logger.warn("🚫 IP BLOCKED - IP: {}, Until: {}", 
                    clientIp, stats.getBlockedUntil());
            return false;
        }
        
        // Check basic rate limit
        boolean allowed = checkBasicRateLimit(stats, operationType);
        
        if (!allowed) {
            stats.incrementBlockedCount();
            stats.incrementFailedRequests();
            
            // Create alert if threshold exceeded
            if (stats.getBlockedCount() >= alertThreshold) {
                if (monitoringEnabled) {
                    monitoringService.logBlockedRequest(clientIp, operationType, "Rate limit exceeded");
                }
            }
            
            logger.warn("🚫 RATE LIMIT EXCEEDED - IP: {}, Operation: {}, Blocked Count: {}", 
                    clientIp, operationType, stats.getBlockedCount());
        } else {
            stats.incrementSuccessfulRequests();
            logger.info("✅ RATE LIMIT OK - IP: {}, Operation: {}", clientIp, operationType);
        }
        
        stats.calculateRiskScore();
        stats.updateSuspiciousFlag();
        statisticsRepository.save(stats);
        
        return allowed;
    }
    
    /**
     * Reset rate limit for IP and operation type
     * Clears all buckets and statistics
     */
    public void resetRateLimit(String clientIp, String operationType) {
        if (clientIp == null) {
            logger.error("❌ NULL IP - Cannot reset rate limit");
            return;
        }
        
        logger.info("🔄 RESET RATE LIMIT - IP: {}, Operation: {}, Time: {}", 
                clientIp, operationType, LocalDateTime.now().format(formatter));
        
        // Reset statistics
        RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp).orElse(null);
        if (stats != null) {
            if (operationType == null || operationType.isEmpty()) {
                // Reset all operations
                stats.resetBlockedCount();
                stats.setBlockedUntil(null);
                stats.setLastBlockedAt(null);
                logger.info("✅ RESET ALL OPERATIONS - IP: {}", clientIp);
            } else {
                // Reset specific operation (adjust bucket only)
                logger.info("✅ RESET OPERATION - IP: {}, Operation: {}", clientIp, operationType);
            }
            
            stats.calculateRiskScore();
            stats.updateSuspiciousFlag();
            statisticsRepository.save(stats);
        }
        
        // Clear request patterns
        requestPatterns.remove(clientIp);
        
        // Resolve alerts if exists
        if (monitoringEnabled) {
            databaseService.resetRateLimitForIp(clientIp);
        }
    }
    
    /**
     * Get rate limit statistics for IP
     * Returns comprehensive stats including blocked count, success rate, risk score
     */
    public Map<String, Object> getRateLimitStats(String clientIp) {
        if (clientIp == null) {
            logger.error("❌ NULL IP - Cannot get stats");
            return new HashMap<>();
        }
        
        logger.info("📊 GET RATE LIMIT STATS - IP: {}, Time: {}", 
                clientIp, LocalDateTime.now().format(formatter));
        
        Map<String, Object> stats = new HashMap<>();
        
        // Get statistics from database
        RateLimitStatistics dbStats = statisticsRepository.findByIpAddress(clientIp).orElse(null);
        if (dbStats != null) {
            stats.put("ipAddress", dbStats.getIpAddress());
            stats.put("totalRequests", dbStats.getTotalRequests());
            stats.put("successfulRequests", dbStats.getSuccessfulRequests());
            stats.put("failedRequests", dbStats.getFailedRequests());
            stats.put("blockedCount", dbStats.getBlockedCount());
            stats.put("successRate", dbStats.getFormattedSuccessRate());
            stats.put("failureRate", dbStats.getFormattedFailureRate());
            stats.put("riskScore", dbStats.getRiskScore());
            stats.put("riskLevel", dbStats.getRiskLevel());
            stats.put("isSuspicious", dbStats.getIsSuspicious());
            stats.put("isCurrentlyBlocked", dbStats.isCurrentlyBlocked());
            stats.put("lastRequestAt", dbStats.getLastRequestAt());
            stats.put("lastBlockedAt", dbStats.getLastBlockedAt());
            stats.put("blockedUntil", dbStats.getBlockedUntil());
            stats.put("timeUntilUnblock", dbStats.getTimeUntilUnblock());
        }
        
        // Get request pattern info
        RequestPattern pattern = requestPatterns.get(clientIp);
        if (pattern != null) {
            stats.put("requestsInLastMinute", pattern.getRequestsInLastMinute());
            stats.put("hasUnusualPattern", pattern.hasUnusualPattern());
        }
        
        // Get chat bucket info (login, booking, chat operations)
        Map<String, Object> bucketInfo = new HashMap<>();
        bucketInfo.put("login", getOperationBucketInfo(clientIp, "login"));
        bucketInfo.put("booking", getOperationBucketInfo(clientIp, "booking"));
        bucketInfo.put("chat", getOperationBucketInfo(clientIp, "chat"));
        stats.put("buckets", bucketInfo);
        
        logger.info("✅ STATS RETRIEVED - IP: {}, Total Requests: {}, Blocked: {}", 
                clientIp, stats.get("totalRequests"), stats.get("blockedCount"));
        
        return stats;
    }
    
    /**
     * Helper method to get bucket info for specific operation
     */
    private Map<String, Object> getOperationBucketInfo(String clientIp, String operationType) {
        Map<String, Object> info = new HashMap<>();
        info.put("operation", operationType);
        info.put("ip", clientIp);
        return info;
    }
}
