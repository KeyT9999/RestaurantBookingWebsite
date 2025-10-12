package com.example.booking.aspect;

import com.example.booking.annotation.RateLimited;
import com.example.booking.service.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect for handling rate limiting annotations
 */
@Aspect
@Component
public class RateLimitingAspect {

    @Autowired
    private RateLimitingService rateLimitingService;

    @Around("@annotation(rateLimited)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String clientIp = getClientIpAddress(request);
        boolean isAllowed = false;

        // Check rate limit based on operation type
        switch (rateLimited.value()) {
            case LOGIN:
                isAllowed = rateLimitingService.isLoginAllowed(clientIp);
                break;
            case BOOKING:
                isAllowed = rateLimitingService.isBookingAllowed(clientIp);
                break;
            case CHAT:
                isAllowed = rateLimitingService.isChatAllowed(clientIp);
                break;
            case REVIEW:
                isAllowed = rateLimitingService.isReviewAllowed(clientIp);
                break;
            case GENERAL:
            default:
                isAllowed = rateLimitingService.isGeneralAllowed(clientIp);
                break;
        }

        if (!isAllowed) {
            // Return rate limit exceeded response
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("{\"error\":\"" + rateLimited.message() + "\"}");
        }

        // Proceed with the original method
        return joinPoint.proceed();
    }

    /**
     * Get current HTTP request
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
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

