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
        
        // Check if user is restaurant owner or admin
        boolean isRestaurantOwner = false;
        boolean isAdmin = false;
        try {
            if (authentication != null && authentication.getAuthorities() != null) {
                isRestaurantOwner = authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_RESTAURANT_OWNER".equals(a.getAuthority()));
                isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                
                // Set a session flag to trigger location prompt once per login session (skip for RESTAURANT_OWNER and ADMIN)
                if (!isRestaurantOwner && !isAdmin) {
                    request.getSession(true).setAttribute("SHOW_LOCATION_PROMPT", Boolean.TRUE);
                } else {
                    request.getSession(true).removeAttribute("SHOW_LOCATION_PROMPT");
                    if (isRestaurantOwner) {
                        logger.info("🏪 RESTAURANT OWNER DETECTED - Redirecting to dashboard");
                    }
                    if (isAdmin) {
                        logger.info("👑 ADMIN DETECTED - Redirecting to admin dashboard");
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("⚠️ Error checking user role: {}", e.getMessage());
        }

        // Ensure session is committed before redirect
        try {
            request.getSession().setAttribute("AUTHENTICATED_USER", username);
            logger.info("✅ SESSION CREATED - Session ID: {}, Username: {}", 
                    request.getSession().getId(), username);
        } catch (Exception e) {
            logger.error("❌ SESSION CREATION FAILED - Error: {}", e.getMessage());
        }

        // Redirect based on user role
        if (isRestaurantOwner) {
            // Redirect restaurant owner directly to dashboard
            logger.info("🔄 REDIRECTING OWNER TO DASHBOARD - Username: {}", username);
            response.sendRedirect("/restaurant-owner/dashboard");
        } else if (isAdmin) {
            // Redirect admin directly to admin dashboard
            logger.info("🔄 REDIRECTING ADMIN TO DASHBOARD - Username: {}", username);
            response.sendRedirect("/admin/dashboard");
        } else {
            // Redirect other users to home page
            response.sendRedirect("/");
        }
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

