package com.example.booking.config;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;
import com.example.booking.service.LoginRateLimitingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnBean({LoginRateLimitingService.class, RateLimitStatisticsRepository.class})
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);
    
    @Autowired
    private LoginRateLimitingService loginRateLimitingService;
    
    @Autowired
    private RateLimitStatisticsRepository statisticsRepository;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                       AuthenticationException exception) throws IOException, ServletException {
        
        String clientIp = getClientIpAddress(request);
        logger.info("🔍 AUTHENTICATION FAILURE - Exception: {}, IP: {}", 
                exception.getClass().getSimpleName(), clientIp);
        
        // Update database statistics for failed login
        updateDatabaseStatisticsForFailure(clientIp, request);
        
        // Check rate limit for failed login attempts
        if (exception instanceof org.springframework.security.authentication.BadCredentialsException) {
            logger.info("🔐 BAD CREDENTIALS - Checking rate limit for IP: {}", clientIp);
            
            // Check if rate limit is exceeded
            if (!loginRateLimitingService.isLoginAllowed(request, response)) {
                logger.warn("🚫 RATE LIMIT EXCEEDED - IP: {}, redirecting to login with rate limit parameter", clientIp);
                
                // ✅ CHỈ KHI RATE LIMIT EXCEEDED MỚI TÍNH BLOCKED
                updateDatabaseStatisticsForRateLimitExceeded(clientIp, request);
                
                // ✅ KIỂM TRA XEM CÓ NÊN ĐƯA VÀO DANH SÁCH CẢNH BÁO KHÔNG
                checkAndMarkAsSuspicious(clientIp, request);
                
                response.sendRedirect("/login?ratelimit=1");
                return;
            }
            
            logger.info("✅ RATE LIMIT OK - IP: {}, redirecting to login with bad credentials error", clientIp);
            response.sendRedirect("/login?error=badcredentials");
        } else if (exception instanceof org.springframework.security.authentication.LockedException) {
            logger.info("🔒 ACCOUNT LOCKED - Redirecting to verification page");
            response.sendRedirect("/auth/verify-result?locked=1");
        } else {
            logger.info("❓ OTHER ERROR - Redirecting to login with generic error");
            response.sendRedirect("/login?error");
        }
    }
    
    /**
     * Update database statistics for failed login
     */
    private void updateDatabaseStatisticsForFailure(String clientIp, HttpServletRequest request) {
        try {
            // Get or create statistics record
            RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp)
                    .orElse(new RateLimitStatistics(clientIp));
            
            // Update statistics for failed login (NOT blocked yet)
            stats.incrementTotalRequests();
            stats.incrementFailedRequests();
            // ❌ KHÔNG incrementBlockedCount() ở đây - chỉ khi rate limit exceeded
            stats.setLastRequestAt(LocalDateTime.now());
            stats.setUserAgent(request.getHeader("User-Agent"));
            
            // Calculate risk score and suspicious flag
            stats.calculateRiskScore();
            stats.updateSuspiciousFlag();
            
            // Save to database
            statisticsRepository.save(stats);
            
            logger.info("📊 DATABASE UPDATED (FAILURE) - IP: {}, Total: {}, Failed: {}, Risk: {}", 
                    clientIp, stats.getTotalRequests(), stats.getFailedRequests(), stats.getRiskScore());
            
        } catch (Exception e) {
            logger.error("❌ DATABASE UPDATE FAILED (FAILURE) - IP: {}, Error: {}", clientIp, e.getMessage());
        }
    }
    
    /**
     * Update database statistics when rate limit is exceeded
     */
    private void updateDatabaseStatisticsForRateLimitExceeded(String clientIp, HttpServletRequest request) {
        try {
            // Get or create statistics record
            RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp)
                    .orElse(new RateLimitStatistics(clientIp));
            
            // ✅ CHỈ KHI RATE LIMIT EXCEEDED MỚI TÍNH BLOCKED
            stats.incrementBlockedCount();
            stats.setLastRequestAt(LocalDateTime.now());
            stats.setUserAgent(request.getHeader("User-Agent"));
            
            // Calculate risk score and suspicious flag
            stats.calculateRiskScore();
            stats.updateSuspiciousFlag();
            
            // Save to database
            statisticsRepository.save(stats);
            
            logger.info("🚫 DATABASE UPDATED (RATE LIMIT EXCEEDED) - IP: {}, Blocked: {}, Risk: {}", 
                    clientIp, stats.getBlockedCount(), stats.getRiskScore());
            
        } catch (Exception e) {
            logger.error("❌ DATABASE UPDATE FAILED (RATE LIMIT) - IP: {}, Error: {}", clientIp, e.getMessage());
        }
    }
    
    /**
     * Check if IP should be marked as suspicious (for admin review)
     */
    private void checkAndMarkAsSuspicious(String clientIp, HttpServletRequest request) {
        try {
            RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp)
                    .orElse(new RateLimitStatistics(clientIp));
            
            // ✅ ĐIỀU KIỆN ĐƯA VÀO DANH SÁCH CẢNH BÁO
            if (stats.getBlockedCount() >= 10) {  // 10 lần rate limit exceeded
                stats.setIsSuspicious(true);
                stats.setSuspiciousReason("Rate limit exceeded " + stats.getBlockedCount() + " times");
                stats.setSuspiciousAt(LocalDateTime.now());
                
                statisticsRepository.save(stats);
                
                logger.warn("⚠️ SUSPICIOUS IP DETECTED - IP: {}, Blocked Count: {}, Reason: {}", 
                        clientIp, stats.getBlockedCount(), stats.getSuspiciousReason());
            }
            
        } catch (Exception e) {
            logger.error("❌ SUSPICIOUS CHECK FAILED - IP: {}, Error: {}", clientIp, e.getMessage());
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
}
