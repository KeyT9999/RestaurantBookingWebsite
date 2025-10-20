package com.example.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeneralRateLimitingService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeneralRateLimitingService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Custom rate limiting storage for different operations
    private final ConcurrentHashMap<String, GeneralAttemptInfo> bookingAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, GeneralAttemptInfo> chatAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, GeneralAttemptInfo> reviewAttempts = new ConcurrentHashMap<>();
    
    // Booking Rate Limiting
    @Value("${rate.limit.booking.requests:10}")
    private int maxBookingAttempts;
    
    @Value("${rate.limit.booking.window:60}")
    private int bookingWindowSeconds;
    
    @Value("${rate.limit.booking.auto-reset:300}")
    private int bookingAutoResetSeconds;
    
    // Chat Rate Limiting
    @Value("${rate.limit.chat.requests:30}")
    private int maxChatAttempts;
    
    @Value("${rate.limit.chat.window:60}")
    private int chatWindowSeconds;
    
    @Value("${rate.limit.chat.auto-reset:300}")
    private int chatAutoResetSeconds;
    
    // Review Rate Limiting
    @Value("${rate.limit.review.requests:3}")
    private int maxReviewAttempts;
    
    @Value("${rate.limit.review.window:300}")
    private int reviewWindowSeconds;
    
    @Value("${rate.limit.review.auto-reset:1800}")
    private int reviewAutoResetSeconds;
    
    @Autowired
    @Lazy
    private RateLimitingMonitoringService monitoringService;
    
    /**
     * Check if booking request is allowed
     */
    public boolean isBookingAllowed(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = getClientIpAddress(request);
        
        logger.info("🔍 BOOKING RATE LIMIT CHECK - IP: {}, Time: {}", 
                clientIp, LocalDateTime.now().format(formatter));
        
        GeneralAttemptInfo attemptInfo = bookingAttempts.get(clientIp);
        
        if (attemptInfo == null) {
            attemptInfo = new GeneralAttemptInfo();
            attemptInfo.setClientIp(clientIp);
            bookingAttempts.put(clientIp, attemptInfo);
        }
        
        // Check if window has expired
        if (attemptInfo.isWindowExpired(bookingWindowSeconds)) {
            logger.info("⏱️ BOOKING WINDOW RESET - IP: {}, window: {}s", clientIp, bookingWindowSeconds);
            attemptInfo.reset();
        }

        // Check if auto-reset time has expired
        if (attemptInfo.isAutoResetExpired(bookingAutoResetSeconds)) {
            logger.info("🔄 AUTO RESET EXPIRED - Resetting booking attempts for IP: {} after {} seconds", 
                    clientIp, bookingAutoResetSeconds);
            attemptInfo.reset();
        }
        
        // Check if max attempts reached
        if (attemptInfo.getAttemptCount() >= maxBookingAttempts) {
            logger.warn("🚫 BOOKING BLOCKED - IP: {}, Attempts: {}/{}, Time: {}",
                    clientIp, attemptInfo.getAttemptCount(), maxBookingAttempts, LocalDateTime.now().format(formatter));
            monitoringService.logBlockedRequest(clientIp, request.getRequestURI(), request.getHeader("User-Agent"));
            return false;
        }
        
        // Increment attempt count
        attemptInfo.incrementAttempt();
        
        logger.info("✅ BOOKING ALLOWED - IP: {}, Attempts: {}/{}, Time: {}",
                clientIp, attemptInfo.getAttemptCount(), maxBookingAttempts, LocalDateTime.now().format(formatter));
        
        addRateLimitHeaders(response, attemptInfo, maxBookingAttempts, bookingWindowSeconds);
        return true;
    }
    
    /**
     * Check if chat request is allowed
     */
    public boolean isChatAllowed(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = getClientIpAddress(request);
        
        logger.info("🔍 CHAT RATE LIMIT CHECK - IP: {}, Time: {}", 
                clientIp, LocalDateTime.now().format(formatter));
        
        GeneralAttemptInfo attemptInfo = chatAttempts.get(clientIp);
        
        if (attemptInfo == null) {
            attemptInfo = new GeneralAttemptInfo();
            attemptInfo.setClientIp(clientIp);
            chatAttempts.put(clientIp, attemptInfo);
        }
        
        // Check if window has expired
        if (attemptInfo.isWindowExpired(chatWindowSeconds)) {
            logger.info("⏱️ CHAT WINDOW RESET - IP: {}, window: {}s", clientIp, chatWindowSeconds);
            attemptInfo.reset();
        }

        // Check if auto-reset time has expired
        if (attemptInfo.isAutoResetExpired(chatAutoResetSeconds)) {
            logger.info("🔄 AUTO RESET EXPIRED - Resetting chat attempts for IP: {} after {} seconds", 
                    clientIp, chatAutoResetSeconds);
            attemptInfo.reset();
        }
        
        // Check if max attempts reached
        if (attemptInfo.getAttemptCount() >= maxChatAttempts) {
            logger.warn("🚫 CHAT BLOCKED - IP: {}, Attempts: {}/{}, Time: {}",
                    clientIp, attemptInfo.getAttemptCount(), maxChatAttempts, LocalDateTime.now().format(formatter));
            monitoringService.logBlockedRequest(clientIp, request.getRequestURI(), request.getHeader("User-Agent"));
            return false;
        }
        
        // Increment attempt count
        attemptInfo.incrementAttempt();
        
        logger.info("✅ CHAT ALLOWED - IP: {}, Attempts: {}/{}, Time: {}",
                clientIp, attemptInfo.getAttemptCount(), maxChatAttempts, LocalDateTime.now().format(formatter));
        
        addRateLimitHeaders(response, attemptInfo, maxChatAttempts, chatWindowSeconds);
        return true;
    }
    
    /**
     * Check if review request is allowed
     */
    public boolean isReviewAllowed(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = getClientIpAddress(request);
        
        logger.info("🔍 REVIEW RATE LIMIT CHECK - IP: {}, Time: {}", 
                clientIp, LocalDateTime.now().format(formatter));
        
        GeneralAttemptInfo attemptInfo = reviewAttempts.get(clientIp);
        
        if (attemptInfo == null) {
            attemptInfo = new GeneralAttemptInfo();
            attemptInfo.setClientIp(clientIp);
            reviewAttempts.put(clientIp, attemptInfo);
        }
        
        // Check if window has expired
        if (attemptInfo.isWindowExpired(reviewWindowSeconds)) {
            logger.info("⏱️ REVIEW WINDOW RESET - IP: {}, window: {}s", clientIp, reviewWindowSeconds);
            attemptInfo.reset();
        }

        // Check if auto-reset time has expired
        if (attemptInfo.isAutoResetExpired(reviewAutoResetSeconds)) {
            logger.info("🔄 AUTO RESET EXPIRED - Resetting review attempts for IP: {} after {} seconds", 
                    clientIp, reviewAutoResetSeconds);
            attemptInfo.reset();
        }
        
        // Check if max attempts reached
        if (attemptInfo.getAttemptCount() >= maxReviewAttempts) {
            logger.warn("🚫 REVIEW BLOCKED - IP: {}, Attempts: {}/{}, Time: {}",
                    clientIp, attemptInfo.getAttemptCount(), maxReviewAttempts, LocalDateTime.now().format(formatter));
            monitoringService.logBlockedRequest(clientIp, request.getRequestURI(), request.getHeader("User-Agent"));
            return false;
        }
        
        // Increment attempt count
        attemptInfo.incrementAttempt();
        
        logger.info("✅ REVIEW ALLOWED - IP: {}, Attempts: {}/{}, Time: {}",
                clientIp, attemptInfo.getAttemptCount(), maxReviewAttempts, LocalDateTime.now().format(formatter));
        
        addRateLimitHeaders(response, attemptInfo, maxReviewAttempts, reviewWindowSeconds);
        return true;
    }
    
    /**
     * Reset rate limit for successful operations
     */
    public void resetBookingRateLimit(String clientIp) {
        GeneralAttemptInfo attemptInfo = bookingAttempts.get(clientIp);
        if (attemptInfo != null) {
            attemptInfo.reset();
            logger.info("🎉 BOOKING SUCCESS - Reset rate limit for IP: {}, Time: {}", 
                    clientIp, LocalDateTime.now().format(formatter));
        }
    }
    
    public void resetChatRateLimit(String clientIp) {
        GeneralAttemptInfo attemptInfo = chatAttempts.get(clientIp);
        if (attemptInfo != null) {
            attemptInfo.reset();
            logger.info("🎉 CHAT SUCCESS - Reset rate limit for IP: {}, Time: {}", 
                    clientIp, LocalDateTime.now().format(formatter));
        }
    }
    
    public void resetReviewRateLimit(String clientIp) {
        GeneralAttemptInfo attemptInfo = reviewAttempts.get(clientIp);
        if (attemptInfo != null) {
            attemptInfo.reset();
            logger.info("🎉 REVIEW SUCCESS - Reset rate limit for IP: {}, Time: {}", 
                    clientIp, LocalDateTime.now().format(formatter));
        }
    }
    
    /**
     * Get client IP address from request
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
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, GeneralAttemptInfo attemptInfo, int maxAttempts, int windowSeconds) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxAttempts));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxAttempts - attemptInfo.getAttemptCount())));
        long secondsSinceFirstAttempt = attemptInfo.secondsSinceFirstAttempt();
        long secondsUntilReset = Math.max(0, windowSeconds - secondsSinceFirstAttempt);

        response.setHeader("X-RateLimit-Reset", String.valueOf(secondsUntilReset));
    }
    
    /**
     * Inner class to track general attempts
     */
    private static class GeneralAttemptInfo {
        private int attemptCount;
        private LocalDateTime firstAttemptTime;
        private String clientIp;
        
        public GeneralAttemptInfo() {
            reset();
        }
        
        public void incrementAttempt() {
            if (attemptCount == 0) {
                firstAttemptTime = LocalDateTime.now();
            }
            this.attemptCount++;
        }
        
        public void reset() {
            this.attemptCount = 0;
            this.firstAttemptTime = LocalDateTime.now();
        }
        
        public int getAttemptCount() {
            return attemptCount;
        }
        
        public boolean isWindowExpired(int windowSeconds) {
            return firstAttemptTime != null && LocalDateTime.now().isAfter(firstAttemptTime.plusSeconds(windowSeconds));
        }
        
        public boolean isAutoResetExpired(int autoResetSeconds) {
            return LocalDateTime.now().isAfter(firstAttemptTime.plusSeconds(autoResetSeconds));
        }
        
        public String getClientIp() {
            return clientIp;
        }
        
        public void setClientIp(String clientIp) {
            this.clientIp = clientIp;
        }
        
        public LocalDateTime getFirstAttemptTime() {
            return firstAttemptTime;
        }

        public long secondsSinceFirstAttempt() {
            if (firstAttemptTime == null) {
                return 0;
            }
            return Math.max(0, Duration.between(firstAttemptTime, LocalDateTime.now()).getSeconds());
        }
    }
}
