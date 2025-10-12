package com.example.booking.config;

import com.example.booking.service.LoginRateLimitingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);
    
    @Autowired
    private LoginRateLimitingService loginRateLimitingService;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                       AuthenticationException exception) throws IOException, ServletException {
        
        String clientIp = getClientIpAddress(request);
        logger.info("🔍 AUTHENTICATION FAILURE - Exception: {}, IP: {}", 
                exception.getClass().getSimpleName(), clientIp);
        
        // Check rate limit for failed login attempts
        if (exception instanceof org.springframework.security.authentication.BadCredentialsException) {
            logger.info("🔐 BAD CREDENTIALS - Checking rate limit for IP: {}", clientIp);
            
            // Check if rate limit is exceeded
            if (!loginRateLimitingService.isLoginAllowed(request, response)) {
                logger.warn("🚫 RATE LIMIT EXCEEDED - IP: {}, redirecting to login with rate limit parameter", clientIp);
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
