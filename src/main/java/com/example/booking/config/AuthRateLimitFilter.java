package com.example.booking.config;

import com.example.booking.service.AuthRateLimitingService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class AuthRateLimitFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthRateLimitFilter.class);
    
    @Autowired
    private AuthRateLimitingService authRateLimitingService;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        
        // Apply rate limiting to specific auth endpoints
        if ("POST".equalsIgnoreCase(method)) {
            if ("/auth/forgot-password".equals(uri)) {
                logger.info("🔍 AUTH FILTER - Checking forgot password rate limit for IP: {}", httpRequest.getRemoteAddr());
                
                if (!authRateLimitingService.isForgotPasswordAllowed(httpRequest, httpResponse)) {
                    logger.warn("🚫 AUTH FILTER - Forgot password rate limit exceeded for IP: {}, redirecting", httpRequest.getRemoteAddr());
                    httpResponse.sendRedirect("/auth/forgot-password?ratelimit=1");
                    return;
                }
                
                logger.info("✅ AUTH FILTER - Forgot password rate limit OK for IP: {}", httpRequest.getRemoteAddr());
                
            } else if ("/auth/register".equals(uri)) {
                logger.info("🔍 AUTH FILTER - Checking register rate limit for IP: {}", httpRequest.getRemoteAddr());
                
                if (!authRateLimitingService.isRegisterAllowed(httpRequest, httpResponse)) {
                    logger.warn("🚫 AUTH FILTER - Register rate limit exceeded for IP: {}, redirecting", httpRequest.getRemoteAddr());
                    httpResponse.sendRedirect("/auth/register?ratelimit=1");
                    return;
                }
                
                logger.info("✅ AUTH FILTER - Register rate limit OK for IP: {}", httpRequest.getRemoteAddr());
                
            } else if ("/auth/reset-password".equals(uri)) {
                logger.info("🔍 AUTH FILTER - Checking reset password rate limit for IP: {}", httpRequest.getRemoteAddr());
                
                if (!authRateLimitingService.isResetPasswordAllowed(httpRequest, httpResponse)) {
                    logger.warn("🚫 AUTH FILTER - Reset password rate limit exceeded for IP: {}, redirecting", httpRequest.getRemoteAddr());
                    httpResponse.sendRedirect("/auth/reset-password?ratelimit=1");
                    return;
                }
                
                logger.info("✅ AUTH FILTER - Reset password rate limit OK for IP: {}", httpRequest.getRemoteAddr());
            }
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }
}
