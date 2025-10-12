package com.example.booking.config;

import com.example.booking.service.LoginRateLimitingService;
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

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    
    @Autowired
    private LoginRateLimitingService loginRateLimitingService;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        
        String clientIp = getClientIpAddress(request);
        String username = authentication.getName();
        
        logger.info("🎉 LOGIN SUCCESS - Username: {}, IP: {}", username, clientIp);
        
        // Reset rate limit for successful login
        loginRateLimitingService.resetRateLimitForSuccessfulLogin(clientIp);
        
        logger.info("🔄 RATE LIMIT RESET - IP: {} after successful login", clientIp);
        
        // Redirect to home page
        response.sendRedirect("/");
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

