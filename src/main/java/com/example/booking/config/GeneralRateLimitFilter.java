package com.example.booking.config;

import com.example.booking.service.GeneralRateLimitingService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class GeneralRateLimitFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(GeneralRateLimitFilter.class);
    
    @Autowired
    private GeneralRateLimitingService generalRateLimitingService;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        
        // Apply rate limiting to specific endpoints
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            
            // Booking endpoints
            if (uri.startsWith("/booking/") || uri.startsWith("/api/booking/")) {
                logger.info("🔍 GENERAL FILTER - Checking booking rate limit for IP: {}", httpRequest.getRemoteAddr());
                
                if (!generalRateLimitingService.isBookingAllowed(httpRequest, httpResponse)) {
                    logger.warn("🚫 GENERAL FILTER - Booking rate limit exceeded for IP: {}, redirecting", httpRequest.getRemoteAddr());
                    httpResponse.sendRedirect("/booking?ratelimit=1");
                    return;
                }
                
                logger.info("✅ GENERAL FILTER - Booking rate limit OK for IP: {}", httpRequest.getRemoteAddr());
                
            }
            // Chat API endpoints
            else if (uri.startsWith("/api/chat/")) {
                logger.info("🔍 GENERAL FILTER - Checking chat rate limit for IP: {}", httpRequest.getRemoteAddr());
                
                if (!generalRateLimitingService.isChatAllowed(httpRequest, httpResponse)) {
                    logger.warn("🚫 GENERAL FILTER - Chat rate limit exceeded for IP: {}, returning 429", httpRequest.getRemoteAddr());
                    httpResponse.setStatus(429);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded for chat. Please try again later.\",\"retryAfter\":60}");
                    return;
                }
                
                logger.info("✅ GENERAL FILTER - Chat rate limit OK for IP: {}", httpRequest.getRemoteAddr());
                
            }
            // Review endpoints
            else if (uri.startsWith("/reviews/")) {
                logger.info("🔍 GENERAL FILTER - Checking review rate limit for IP: {}", httpRequest.getRemoteAddr());
                
                if (!generalRateLimitingService.isReviewAllowed(httpRequest, httpResponse)) {
                    logger.warn("🚫 GENERAL FILTER - Review rate limit exceeded for IP: {}, redirecting", httpRequest.getRemoteAddr());
                    httpResponse.sendRedirect("/reviews?ratelimit=1");
                    return;
                }
                
                logger.info("✅ GENERAL FILTER - Review rate limit OK for IP: {}", httpRequest.getRemoteAddr());
            }
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }
}
