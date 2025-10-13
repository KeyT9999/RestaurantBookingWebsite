package com.example.booking.config;

import com.example.booking.service.LoginRateLimitingService;
import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    
    @Autowired
    private LoginRateLimitingService loginRateLimitingService;
    
    @Autowired
    private RateLimitStatisticsRepository statisticsRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        
        String clientIp = getClientIpAddress(request);
        String username = authentication.getName();
        
        logger.info("🎉 LOGIN SUCCESS - Username: {}, IP: {}", username, clientIp);
        
        // Reset rate limit for successful login (memory)
        loginRateLimitingService.resetRateLimitForSuccessfulLogin(clientIp);
        
        // Update database statistics for successful login
        updateDatabaseStatistics(clientIp, request);
        
        logger.info("🔄 RATE LIMIT RESET - IP: {} after successful login", clientIp);
        
        // Redirect to home page
        response.sendRedirect("/");
    }
    
    /**
     * Update database statistics for successful login
     */
    private void updateDatabaseStatistics(String clientIp, HttpServletRequest request) {
        try {
            // Get or create statistics record
            RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp)
                    .orElse(new RateLimitStatistics(clientIp));
            
            // Update statistics
            stats.incrementTotalRequests();
            stats.incrementSuccessfulRequests();
            stats.setLastRequestAt(LocalDateTime.now());
            stats.setUserAgent(request.getHeader("User-Agent"));
            
            // Calculate risk score and suspicious flag
            stats.calculateRiskScore();
            stats.updateSuspiciousFlag();
            
            // Save to database
            statisticsRepository.save(stats);
            
            logger.info("📊 DATABASE UPDATED - IP: {}, Total: {}, Success: {}, Risk: {}", 
                    clientIp, stats.getTotalRequests(), stats.getSuccessfulRequests(), stats.getRiskScore());
            
        } catch (Exception e) {
            logger.error("❌ DATABASE UPDATE FAILED - IP: {}, Error: {}", clientIp, e.getMessage());
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

