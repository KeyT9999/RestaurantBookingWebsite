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
public class AuthRateLimitingService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthRateLimitingService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Custom rate limiting storage for different auth operations
    private final ConcurrentHashMap<String, AuthAttemptInfo> forgotPasswordAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AuthAttemptInfo> registerAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AuthAttemptInfo> resetPasswordAttempts = new ConcurrentHashMap<>();
    
    // Forgot Password Rate Limiting
    @Value("${rate.limit.forgot-password.requests:3}")
    private int maxForgotPasswordAttempts;
    
    @Value("${rate.limit.forgot-password.window:300}")
    private int forgotPasswordWindowSeconds;
    
    @Value("${rate.limit.forgot-password.auto-reset:1800}")
    private int forgotPasswordAutoResetSeconds;
    
    // Register Rate Limiting
    @Value("${rate.limit.register.requests:2}")
    private int maxRegisterAttempts;
    
    @Value("${rate.limit.register.window:300}")
    private int registerWindowSeconds;
    
    @Value("${rate.limit.register.auto-reset:1800}")
    private int registerAutoResetSeconds;
    
    // Reset Password Rate Limiting
    @Value("${rate.limit.reset-password.requests:3}")
    private int maxResetPasswordAttempts;
    
    @Value("${rate.limit.reset-password.window:300}")
    private int resetPasswordWindowSeconds;
    
    @Value("${rate.limit.reset-password.auto-reset:1800}")
    private int resetPasswordAutoResetSeconds;
    
    @Autowired
    @Lazy
    private RateLimitingMonitoringService monitoringService;
    
    /**
     * Check if forgot password request is allowed
     */
    public boolean isForgotPasswordAllowed(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = getClientIpAddress(request);
        String operation = "forgot-password";
        
        logger.info("🔍 FORGOT PASSWORD RATE LIMIT CHECK - IP: {}, Time: {}", 
                clientIp, LocalDateTime.now().format(formatter));
        
        AuthAttemptInfo attemptInfo = forgotPasswordAttempts.get(clientIp);
        
        if (attemptInfo == null) {
            attemptInfo = new AuthAttemptInfo();
            attemptInfo.setClientIp(clientIp);
            forgotPasswordAttempts.put(clientIp, attemptInfo);
        }
        
        // Check if auto-reset time has expired
        if (attemptInfo.isAutoResetExpired(forgotPasswordAutoResetSeconds)) {
            logger.info("🔄 AUTO RESET EXPIRED - Resetting forgot password attempts for IP: {} after {} seconds", 
                    clientIp, forgotPasswordAutoResetSeconds);
            attemptInfo.reset();
        }
        
        // Check if max attempts reached
        if (attemptInfo.getAttemptCount() >= maxForgotPasswordAttempts) {
            logger.warn("🚫 FORGOT PASSWORD BLOCKED - IP: {}, Attempts: {}/{}, Time: {}",
                    clientIp, attemptInfo.getAttemptCount(), maxForgotPasswordAttempts, LocalDateTime.now().format(formatter));
            monitoringService.logBlockedRequest(clientIp, "/auth/forgot-password", request.getHeader("User-Agent"));
            return false;
        }
        
        // Increment attempt count
        attemptInfo.incrementAttempt();
        
        logger.info("✅ FORGOT PASSWORD ALLOWED - IP: {}, Attempts: {}/{}, Time: {}",
                clientIp, attemptInfo.getAttemptCount(), maxForgotPasswordAttempts, LocalDateTime.now().format(formatter));
        
        addRateLimitHeaders(response, attemptInfo, maxForgotPasswordAttempts, forgotPasswordWindowSeconds);
        return true;
    }
    
    /**
     * Check if register request is allowed
     */
    public boolean isRegisterAllowed(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = getClientIpAddress(request);
        String operation = "register";
        
        logger.info("🔍 REGISTER RATE LIMIT CHECK - IP: {}, Time: {}", 
                clientIp, LocalDateTime.now().format(formatter));
        
        AuthAttemptInfo attemptInfo = registerAttempts.get(clientIp);
        
        if (attemptInfo == null) {
            attemptInfo = new AuthAttemptInfo();
            attemptInfo.setClientIp(clientIp);
            registerAttempts.put(clientIp, attemptInfo);
        }
        
        // Check if auto-reset time has expired
        if (attemptInfo.isAutoResetExpired(registerAutoResetSeconds)) {
            logger.info("🔄 AUTO RESET EXPIRED - Resetting register attempts for IP: {} after {} seconds", 
                    clientIp, registerAutoResetSeconds);
            attemptInfo.reset();
        }
        
        // Check if max attempts reached
        if (attemptInfo.getAttemptCount() >= maxRegisterAttempts) {
            logger.warn("🚫 REGISTER BLOCKED - IP: {}, Attempts: {}/{}, Time: {}",
                    clientIp, attemptInfo.getAttemptCount(), maxRegisterAttempts, LocalDateTime.now().format(formatter));
            monitoringService.logBlockedRequest(clientIp, "/auth/register", request.getHeader("User-Agent"));
            return false;
        }
        
        // Increment attempt count
        attemptInfo.incrementAttempt();
        
        logger.info("✅ REGISTER ALLOWED - IP: {}, Attempts: {}/{}, Time: {}",
                clientIp, attemptInfo.getAttemptCount(), maxRegisterAttempts, LocalDateTime.now().format(formatter));
        
        addRateLimitHeaders(response, attemptInfo, maxRegisterAttempts, registerWindowSeconds);
        return true;
    }
    
    /**
     * Check if reset password request is allowed
     */
    public boolean isResetPasswordAllowed(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = getClientIpAddress(request);
        String operation = "reset-password";
        
        logger.info("🔍 RESET PASSWORD RATE LIMIT CHECK - IP: {}, Time: {}", 
                clientIp, LocalDateTime.now().format(formatter));
        
        AuthAttemptInfo attemptInfo = resetPasswordAttempts.get(clientIp);
        
        if (attemptInfo == null) {
            attemptInfo = new AuthAttemptInfo();
            attemptInfo.setClientIp(clientIp);
            resetPasswordAttempts.put(clientIp, attemptInfo);
        }
        
        // Check if auto-reset time has expired
        if (attemptInfo.isAutoResetExpired(resetPasswordAutoResetSeconds)) {
            logger.info("🔄 AUTO RESET EXPIRED - Resetting reset password attempts for IP: {} after {} seconds", 
                    clientIp, resetPasswordAutoResetSeconds);
            attemptInfo.reset();
        }
        
        // Check if max attempts reached
        if (attemptInfo.getAttemptCount() >= maxResetPasswordAttempts) {
            logger.warn("🚫 RESET PASSWORD BLOCKED - IP: {}, Attempts: {}/{}, Time: {}",
                    clientIp, attemptInfo.getAttemptCount(), maxResetPasswordAttempts, LocalDateTime.now().format(formatter));
            monitoringService.logBlockedRequest(clientIp, "/auth/reset-password", request.getHeader("User-Agent"));
            return false;
        }
        
        // Increment attempt count
        attemptInfo.incrementAttempt();
        
        logger.info("✅ RESET PASSWORD ALLOWED - IP: {}, Attempts: {}/{}, Time: {}",
                clientIp, attemptInfo.getAttemptCount(), maxResetPasswordAttempts, LocalDateTime.now().format(formatter));
        
        addRateLimitHeaders(response, attemptInfo, maxResetPasswordAttempts, resetPasswordWindowSeconds);
        return true;
    }
    
    /**
     * Reset rate limit for successful operations
     */
    public void resetForgotPasswordRateLimit(String clientIp) {
        AuthAttemptInfo attemptInfo = forgotPasswordAttempts.get(clientIp);
        if (attemptInfo != null) {
            attemptInfo.reset();
            logger.info("🎉 FORGOT PASSWORD SUCCESS - Reset rate limit for IP: {}, Time: {}", 
                    clientIp, LocalDateTime.now().format(formatter));
        }
    }
    
    public void resetRegisterRateLimit(String clientIp) {
        AuthAttemptInfo attemptInfo = registerAttempts.get(clientIp);
        if (attemptInfo != null) {
            attemptInfo.reset();
            logger.info("🎉 REGISTER SUCCESS - Reset rate limit for IP: {}, Time: {}", 
                    clientIp, LocalDateTime.now().format(formatter));
        }
    }
    
    public void resetResetPasswordRateLimit(String clientIp) {
        AuthAttemptInfo attemptInfo = resetPasswordAttempts.get(clientIp);
        if (attemptInfo != null) {
            attemptInfo.reset();
            logger.info("🎉 RESET PASSWORD SUCCESS - Reset rate limit for IP: {}, Time: {}", 
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
    private void addRateLimitHeaders(HttpServletResponse response, AuthAttemptInfo attemptInfo, int maxAttempts, int windowSeconds) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxAttempts));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxAttempts - attemptInfo.getAttemptCount())));
        response.setHeader("X-RateLimit-Reset", String.valueOf(windowSeconds));
    }
    
    /**
     * Inner class to track auth attempts
     */
    private static class AuthAttemptInfo {
        private int attemptCount;
        private LocalDateTime firstAttemptTime;
        private String clientIp;
        
        public AuthAttemptInfo() {
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
