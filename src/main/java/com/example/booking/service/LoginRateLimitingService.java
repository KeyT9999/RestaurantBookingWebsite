package com.example.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimitingService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimitingService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Custom rate limiting storage
    private final ConcurrentHashMap<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();
    
    @Value("${rate.limit.login.requests:5}")
    private int maxLoginAttempts;
    
    @Value("${rate.limit.login.window:30}")
    private int windowSeconds;
    
    @Value("${rate.limit.login.auto-reset:3600}")
    private int autoResetSeconds; // 1 hour = 3600 seconds
    
    @Autowired
    @Lazy
    private RateLimitingMonitoringService monitoringService;
    
    @Autowired
    private DatabaseRateLimitingService databaseService;
    
    /**
     * Check if login request is allowed based on rate limiting
     */
    public boolean isLoginAllowed(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = getClientIpAddress(request);
        
        logger.info("🔍 LOGIN RATE LIMIT CHECK - IP: {}, Time: {}", 
                clientIp, LocalDateTime.now().format(formatter));
        
        LoginAttemptInfo attemptInfo = loginAttempts.get(clientIp);
        
        // If no previous attempts, allow
        if (attemptInfo == null) {
            attemptInfo = new LoginAttemptInfo();
            attemptInfo.setClientIp(clientIp);
            loginAttempts.put(clientIp, attemptInfo);
        }
        
        // Check if auto-reset time has expired (1 hour)
        if (attemptInfo.isAutoResetExpired(autoResetSeconds)) {
            logger.info("🔄 AUTO RESET EXPIRED - Resetting attempts for IP: {} after {} seconds", 
                    clientIp, autoResetSeconds);
            attemptInfo.reset();
        }
        
        // Check if max attempts reached
        if (attemptInfo.getAttemptCount() >= maxLoginAttempts) {
            logger.warn("🚫 LOGIN BLOCKED - IP: {}, Attempts: {}/{}, Time: {}",
                    clientIp, attemptInfo.getAttemptCount(), maxLoginAttempts, LocalDateTime.now().format(formatter));
            
            // LOGGING để trace
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("🔍 [TRACE] LoginRateLimitingService - Rate limit exceeded");
            System.out.println("   IP: " + clientIp);
            System.out.println("   Attempts: " + attemptInfo.getAttemptCount() + "/" + maxLoginAttempts);
            System.out.println("   Thread: " + Thread.currentThread().getName());
            System.out.println("   Time: " + LocalDateTime.now());
            
            // Log to both memory and database
            // LoginRateLimitingService cần tăng blockedCount vì không tự tăng như AdvancedRateLimitingService
            monitoringService.logBlockedRequest(clientIp, "/login", request.getHeader("User-Agent"));
            
            System.out.println("✅ [TRACE] CALLING databaseService.logBlockedRequest(..., true) from LoginRateLimitingService");
            databaseService.logBlockedRequest(clientIp, "/login", request.getHeader("User-Agent"), "login", true);
            
            return false;
        }
        
        // Increment attempt count
        attemptInfo.incrementAttempt();
        
        logger.info("✅ LOGIN ALLOWED - IP: {}, Attempts: {}/{}, Time: {}",
                clientIp, attemptInfo.getAttemptCount(), maxLoginAttempts, LocalDateTime.now().format(formatter));
        
        addRateLimitHeaders(response, attemptInfo);
        return true;
    }
    
    /**
     * Reset rate limit for successful login
     */
    public void resetRateLimitForSuccessfulLogin(String clientIp) {
        LoginAttemptInfo attemptInfo = loginAttempts.get(clientIp);
        if (attemptInfo != null) {
            attemptInfo.reset();
            logger.info("🎉 LOGIN SUCCESS - Reset rate limit for IP: {}, Time: {}", 
                    clientIp, LocalDateTime.now().format(formatter));
        }
    }
    
    /**
     * Reset rate limit for successful login (overloaded method)
     */
    public void resetRateLimitForSuccessfulLogin(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        resetRateLimitForSuccessfulLogin(clientIp);
    }
    
    /**
     * Reset bucket for specific IP (for testing or manual reset)
     */
    public void resetBucketForIp(String clientIp) {
        loginAttempts.remove(clientIp);
        logger.info("🔄 RESET BUCKET - IP: {}, Time: {}", clientIp, LocalDateTime.now().format(formatter));
    }
    
    /**
     * Reset all rate limits (for testing)
     */
    public void resetAllRateLimits() {
        loginAttempts.clear();
        logger.info("🔄 RESET ALL RATE LIMITS - Time: {}", LocalDateTime.now().format(formatter));
    }
    
    /**
     * Get remaining attempts for IP
     */
    public int getRemainingAttempts(String clientIp) {
        LoginAttemptInfo attemptInfo = loginAttempts.get(clientIp);
        if (attemptInfo != null) {
            return Math.max(0, maxLoginAttempts - attemptInfo.getAttemptCount());
        }
        return maxLoginAttempts;
    }
    
    /**
     * Get auto-reset time remaining for IP
     */
    public long getAutoResetTimeRemaining(String clientIp) {
        LoginAttemptInfo attemptInfo = loginAttempts.get(clientIp);
        if (attemptInfo != null && attemptInfo.getAttemptCount() >= maxLoginAttempts) {
            LocalDateTime resetTime = attemptInfo.getFirstAttemptTime().plusSeconds(autoResetSeconds);
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(resetTime)) {
                return java.time.Duration.between(now, resetTime).getSeconds();
            }
        }
        return 0;
    }
    
    /**
     * Get auto-reset configuration
     */
    public int getAutoResetSeconds() {
        return autoResetSeconds;
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
    private void addRateLimitHeaders(HttpServletResponse response, LoginAttemptInfo attemptInfo) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxLoginAttempts));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(getRemainingAttempts(attemptInfo.getClientIp())));
        response.setHeader("X-RateLimit-Reset", String.valueOf(windowSeconds));
    }
    
    /**
     * Inner class to track login attempts
     */
    private static class LoginAttemptInfo {
        private int attemptCount;
        private LocalDateTime firstAttemptTime;
        private String clientIp;
        
        public LoginAttemptInfo() {
            this.attemptCount = 0;
            this.firstAttemptTime = LocalDateTime.now();
        }
        
        public void incrementAttempt() {
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
            return LocalDateTime.now().isAfter(firstAttemptTime.plusSeconds(windowSeconds));
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
    }
}

