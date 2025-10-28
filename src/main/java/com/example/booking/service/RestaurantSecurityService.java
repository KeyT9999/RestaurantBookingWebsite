package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.repository.RateLimitStatisticsRepository;

/**
 * Service để kiểm tra quyền truy cập cho Restaurant Owner
 */
@Service
public class RestaurantSecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantSecurityService.class);
    
    @Autowired
    private SimpleUserService userService;
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    @Autowired(required = false)
    private RateLimitStatisticsRepository rateLimitStatisticsRepository;
    
    @Value("${security.threat-intelligence.enabled:true}")
    private boolean threatIntelligenceEnabled;
    
    @Value("${security.suspicious-detection.enabled:true}")
    private boolean suspiciousDetectionEnabled;
    
    /**
     * Kiểm tra user có active và có restaurant được approve không
     */
    public boolean isUserActiveAndApproved(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("Authentication is null or not authenticated");
            return false;
        }
        
        try {
            // Lấy user từ authentication
            User user = getUserFromAuthentication(authentication);
            if (user == null) {
                logger.debug("User not found from authentication: {}", authentication.getName());
                return false;
            }
            
            // Kiểm tra user có active không
            if (!Boolean.TRUE.equals(user.getActive())) {
                logger.warn("User {} is not active", user.getUsername());
                return false;
            }
            
            // Kiểm tra user có role RESTAURANT_OWNER không
            if (!user.getRole().isRestaurantOwner()) {
                logger.debug("User {} does not have RESTAURANT_OWNER role", user.getUsername());
                return false;
            }
            
            // Kiểm tra có restaurant được approve không
            Optional<RestaurantOwner> restaurantOwnerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());
            if (restaurantOwnerOpt.isEmpty()) {
                logger.debug("User {} does not have restaurant owner record", user.getUsername());
                return false;
            }
            
            // Kiểm tra có ít nhất 1 restaurant được approve không
            var restaurants = restaurantOwnerService.getRestaurantsByOwnerId(restaurantOwnerOpt.get().getOwnerId());
            boolean hasApprovedRestaurant = restaurants.stream()
                .anyMatch(restaurant -> restaurant.getApprovalStatus() == RestaurantApprovalStatus.APPROVED);
            
            if (!hasApprovedRestaurant) {
                logger.debug("User {} does not have any approved restaurant", user.getUsername());
                return false;
            }
            
            logger.debug("User {} is active and has approved restaurant", user.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Error checking user active and approved status for: {}", authentication.getName(), e);
            return false;
        }
    }
    
    /**
     * Helper method để lấy User từ Authentication
     */
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        
        try {
            // Try to parse as UUID first (if using UUID-based authentication)
            return userService.findById(java.util.UUID.fromString(authentication.getName()));
        } catch (IllegalArgumentException e) {
            // If not a UUID, this is username-based authentication
            return userService.findByUsername(authentication.getName()).orElse(null);
        }
    }
    
    /**
     * Kiểm tra security status của user (sử dụng cho REST API)
     * Returns a map with status information
     */
    public Map<String, Object> checkSecurityStatus(Authentication authentication, String ipAddress) {
        Map<String, Object> result = new HashMap<>();
        
        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            result.put("status", false);
            result.put("message", "Authentication is null or not authenticated");
            result.put("riskLevel", "LOW");
            return result;
        }
        
        try {
            // Get user
            User user = getUserFromAuthentication(authentication);
            if (user == null) {
                result.put("status", false);
                result.put("message", "User not found");
                result.put("riskLevel", "LOW");
                return result;
            }
            
            // Check if user is active
            if (!Boolean.TRUE.equals(user.getActive())) {
                result.put("status", false);
                result.put("message", "User is not active");
                result.put("riskLevel", "LOW");
                logger.warn("Inactive user {} tried to access", user.getUsername());
                return result;
            }
            
            // Check if user has restaurant owner role
            if (!user.getRole().isRestaurantOwner()) {
                result.put("status", false);
                result.put("message", "User does not have RESTAURANT_OWNER role");
                result.put("riskLevel", "LOW");
                return result;
            }
            
            // Check if user has approved restaurant
            Optional<RestaurantOwner> restaurantOwnerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());
            if (restaurantOwnerOpt.isEmpty()) {
                result.put("status", false);
                result.put("message", "No restaurant owner record found");
                result.put("redirect", "/registration");
                result.put("riskLevel", "LOW");
                return result;
            }
            
            // Check for approved restaurants
            var restaurants = restaurantOwnerService.getRestaurantsByOwnerId(restaurantOwnerOpt.get().getOwnerId());
            boolean hasApprovedRestaurant = restaurants.stream()
                .anyMatch(restaurant -> restaurant.getApprovalStatus() == RestaurantApprovalStatus.APPROVED);
            
            if (!hasApprovedRestaurant) {
                result.put("status", false);
                result.put("message", "No approved restaurant found");
                result.put("redirect", "/registration");
                result.put("riskLevel", "LOW");
                return result;
            }
            
            // Check threat intelligence if enabled
            if (threatIntelligenceEnabled && ipAddress != null) {
                Map<String, Object> threatInfo = getThreatIntelligence(ipAddress);
                result.putAll(threatInfo);
            }
            
            // All checks passed
            result.put("status", true);
            result.put("message", "User can access");
            result.put("user", user.getUsername());
            result.put("restaurantOwner", restaurantOwnerOpt.get().getOwnerId());
            
            logger.debug("Security check passed for user: {}", user.getUsername());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error checking security status", e);
            result.put("status", false);
            result.put("message", "Error checking security status");
            result.put("riskLevel", "LOW");
            return result;
        }
    }
    
    /**
     * Get threat intelligence for an IP address
     */
    private Map<String, Object> getThreatIntelligence(String ipAddress) {
        Map<String, Object> threatInfo = new HashMap<>();
        
        if (rateLimitStatisticsRepository == null) {
            threatInfo.put("riskScore", 0);
            threatInfo.put("riskLevel", "LOW");
            return threatInfo;
        }
        
        Optional<RateLimitStatistics> statsOpt = rateLimitStatisticsRepository.findByIpAddress(ipAddress);
        if (statsOpt.isPresent()) {
            RateLimitStatistics stats = statsOpt.get();
            threatInfo.put("riskScore", stats.getRiskScore());
            threatInfo.put("riskLevel", stats.getRiskLevel());
            threatInfo.put("isSuspicious", stats.getIsSuspicious());
        } else {
            threatInfo.put("riskScore", 0);
            threatInfo.put("riskLevel", "LOW");
            threatInfo.put("isSuspicious", false);
        }
        
        return threatInfo;
    }
    
    /**
     * Report suspicious activity
     * Analyzes and logs suspicious activity, auto-blocks if needed
     */
    public Map<String, Object> reportSuspiciousActivity(String ipAddress, String userAgent, boolean autoLog) {
        Map<String, Object> result = new HashMap<>();
        
        if (ipAddress == null) {
            result.put("detected", false);
            result.put("message", "IP address is null");
            return result;
        }
        
        try {
            // Get or create statistics
            RateLimitStatistics stats;
            if (rateLimitStatisticsRepository != null) {
                stats = rateLimitStatisticsRepository.findByIpAddress(ipAddress)
                    .orElse(new RateLimitStatistics(ipAddress));
            } else {
                // No repository available
                result.put("detected", false);
                result.put("message", "Monitoring disabled");
                return result;
            }
            
            // Analyze suspicious patterns
            boolean isSuspicious = analyzeSuspiciousPatterns(ipAddress, userAgent, stats);
            
            if (isSuspicious) {
                stats.setIsSuspicious(true);
                stats.setSuspiciousReason("Suspicious activity detected");
                stats.setSuspiciousAt(LocalDateTime.now());
                stats.calculateRiskScore();
                
                // Auto-log if enabled
                if (autoLog) {
                    logger.warn("🚨 SUSPICIOUS ACTIVITY - IP: {}, UserAgent: {}, RiskScore: {}", 
                               ipAddress, userAgent, stats.getRiskScore());
                }
                
                // Auto-block if risk score is high
                if (stats.getRiskScore() >= 80) {
                    stats.setBlockedCount(stats.getBlockedCount() + 1);
                    stats.incrementBlockedCount();
                    rateLimitStatisticsRepository.save(stats);
                    
                    result.put("detected", true);
                    result.put("isSuspicious", true);
                    result.put("blocked", true);
                    result.put("riskScore", stats.getRiskScore());
                    result.put("riskLevel", "HIGH");
                    result.put("message", "Alert created with severity: danger, message logged");
                    
                    logger.error("🚫 AUTO-BLOCKED IP: {} due to high risk score: {}", 
                               ipAddress, stats.getRiskScore());
                } else {
                    rateLimitStatisticsRepository.save(stats);
                    
                    result.put("detected", true);
                    result.put("isSuspicious", true);
                    result.put("blocked", false);
                    result.put("riskScore", stats.getRiskScore());
                    result.put("riskLevel", stats.getRiskLevel());
                    result.put("message", "Statistics updated: blockedCount++, failedRequests++");
                }
            } else {
                result.put("detected", false);
                result.put("isSuspicious", false);
                result.put("message", "No suspicious activity detection, allows request");
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error analyzing suspicious activity for IP: {}", ipAddress, e);
            result.put("detected", false);
            result.put("message", "Error analyzing suspicious activity");
            return result;
        }
    }
    
    /**
     * Analyze suspicious patterns
     */
    private boolean analyzeSuspiciousPatterns(String ipAddress, String userAgent, RateLimitStatistics stats) {
        if (!suspiciousDetectionEnabled) {
            return false;
        }
        
        // Check for rapid requests (>150 in short time)
        if (stats.getTotalRequests() > 150) {
            logger.debug("Rapid requests detected for IP: {}", ipAddress);
            return true;
        }
        
        // Check for bot-like user agent
        if (userAgent != null && isBotLikeUserAgent(userAgent)) {
            logger.debug("Bot-like user agent detected: {}", userAgent);
            return true;
        }
        
        // Check for high failure rate (>80%)
        if (stats.getFailureRate() > 80) {
            logger.debug("High failure rate detected: {}%", stats.getFailureRate());
            return true;
        }
        
        // Check for unusual patterns (blocked count >= 15)
        if (stats.getBlockedCount() >= 15) {
            logger.debug("Unusual pattern detected, blocked count: {}", stats.getBlockedCount());
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if user agent is bot-like
     */
    private boolean isBotLikeUserAgent(String userAgent) {
        if (userAgent == null) return false;
        
        String ua = userAgent.toLowerCase();
        return ua.contains("curl") || 
               ua.contains("wget") || 
               ua.contains("bot") ||
               ua.contains("python") ||
               ua.contains("scanner") ||
               ua.startsWith("User-Agent=curl");
    }
}
