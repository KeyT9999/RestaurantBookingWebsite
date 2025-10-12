package com.example.booking.config;

import com.example.booking.service.LoginRateLimitingService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * DEPRECATED: This filter is no longer used.
 * Rate limiting is now handled in CustomAuthenticationFailureHandler
 * to ensure it only applies to failed login attempts.
 */
@Component
public class LoginRateLimitFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    
    @Autowired
    private LoginRateLimitingService loginRateLimitingService;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // This filter is now disabled - rate limiting is handled in CustomAuthenticationFailureHandler
        // to ensure it only applies to failed login attempts, not successful ones
        chain.doFilter(request, response);
    }
}