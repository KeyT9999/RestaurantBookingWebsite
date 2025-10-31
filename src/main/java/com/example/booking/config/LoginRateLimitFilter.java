package com.example.booking.config;

import com.example.booking.service.LoginRateLimitingService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * DEPRECATED: This filter is no longer used.
 * Rate limiting is now handled in CustomAuthenticationFailureHandler
 * to ensure it only applies to failed login attempts.
 */
@Component
@ConditionalOnBean(LoginRateLimitingService.class)
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
