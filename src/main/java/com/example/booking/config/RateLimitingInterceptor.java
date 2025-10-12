package com.example.booking.config;

import com.example.booking.service.GeneralRateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interceptor for rate limiting requests based on IP address
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingInterceptor.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private GeneralRateLimitingService generalRateLimitingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Log all requests for debugging
        logger.info("🔍 INTERCEPTING - Method: {}, Path: {}, IP: {}", method, requestPath, clientIp);
        
        // Apply rate limiting based on endpoint type
        boolean isAllowed = true;
        
        if (requestPath.startsWith("/booking/") || requestPath.startsWith("/api/booking/")) {
            isAllowed = generalRateLimitingService.isBookingAllowed(request, response);
        } else if (requestPath.startsWith("/api/chat/")) {
            isAllowed = generalRateLimitingService.isChatAllowed(request, response);
        } else if (requestPath.startsWith("/reviews/")) {
            isAllowed = generalRateLimitingService.isReviewAllowed(request, response);
        }
        
        if (!isAllowed) {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\",\"retryAfter\":60}");
            return false;
        }
        
        return true;
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