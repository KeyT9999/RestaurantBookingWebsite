package com.example.booking.config;

import com.example.booking.service.EndpointRateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advanced interceptor for comprehensive rate limiting across all endpoints
 */
@Component
public class AdvancedRateLimitingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedRateLimitingInterceptor.class);
    
    @Autowired
    private EndpointRateLimitingService endpointRateLimitingService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        logger.info("🔍 ADVANCED INTERCEPTING - Method: {}, Path: {}, IP: {}", 
                method, requestPath, getClientIpAddress(request));
        
        // Skip rate limiting for static resources
        if (isStaticResource(requestPath)) {
            return true;
        }
        
        // Apply rate limiting based on endpoint patterns
        boolean isAllowed = true;
        
        try {
            // Authentication endpoints
            if (requestPath.startsWith("/login") || requestPath.startsWith("/auth/login")) {
                isAllowed = endpointRateLimitingService.isLoginAllowed(request, response);
            }
            // Registration endpoints
            else if (requestPath.startsWith("/register") || requestPath.startsWith("/auth/register")) {
                isAllowed = endpointRateLimitingService.isRegisterAllowed(request, response);
            }
            // Password reset endpoints
            else if (requestPath.startsWith("/forgot-password") || requestPath.startsWith("/auth/forgot-password")) {
                isAllowed = endpointRateLimitingService.isForgotPasswordAllowed(request, response);
            }
            else if (requestPath.startsWith("/reset-password") || requestPath.startsWith("/auth/reset-password")) {
                isAllowed = endpointRateLimitingService.isResetPasswordAllowed(request, response);
            }
            // Booking endpoints
            else if (requestPath.startsWith("/booking/") || requestPath.startsWith("/api/booking/")) {
                isAllowed = endpointRateLimitingService.isBookingAllowed(request, response);
            }
            // Chat endpoints
            else if (requestPath.startsWith("/chat/") || requestPath.startsWith("/api/chat/") || 
                     requestPath.startsWith("/ws/") || requestPath.startsWith("/websocket/")) {
                isAllowed = endpointRateLimitingService.isChatAllowed(request, response);
            }
            // Review endpoints
            else if (requestPath.startsWith("/reviews/") || requestPath.startsWith("/api/reviews/")) {
                isAllowed = endpointRateLimitingService.isReviewAllowed(request, response);
            }
            // File upload endpoints
            else if (requestPath.startsWith("/upload/") || requestPath.startsWith("/api/upload/") ||
                     requestPath.contains("/upload") || method.equals("POST") && requestPath.contains("multipart")) {
                isAllowed = endpointRateLimitingService.isFileUploadAllowed(request, response);
            }
            // Payment endpoints
            else if (requestPath.startsWith("/payment/") || requestPath.startsWith("/api/payment/") ||
                     requestPath.startsWith("/payos/") || requestPath.startsWith("/api/payos/")) {
                isAllowed = endpointRateLimitingService.isPaymentAllowed(request, response);
            }
            // API endpoints
            else if (requestPath.startsWith("/api/")) {
                isAllowed = endpointRateLimitingService.isApiAllowed(request, response);
            }
            // Search endpoints
            else if (requestPath.startsWith("/search/") || requestPath.startsWith("/api/search/")) {
                isAllowed = endpointRateLimitingService.isSearchAllowed(request, response);
            }
            // Profile endpoints
            else if (requestPath.startsWith("/profile/") || requestPath.startsWith("/api/profile/")) {
                isAllowed = endpointRateLimitingService.isProfileAllowed(request, response);
            }
            // Notification endpoints
            else if (requestPath.startsWith("/notifications/") || requestPath.startsWith("/api/notifications/")) {
                isAllowed = endpointRateLimitingService.isNotificationAllowed(request, response);
            }
            // Restaurant endpoints
            else if (requestPath.startsWith("/restaurant/") || requestPath.startsWith("/api/restaurant/")) {
                isAllowed = endpointRateLimitingService.isRestaurantAllowed(request, response);
            }
            // Customer endpoints
            else if (requestPath.startsWith("/customer/") || requestPath.startsWith("/api/customer/")) {
                isAllowed = endpointRateLimitingService.isCustomerAllowed(request, response);
            }
            // Admin endpoints
            else if (requestPath.startsWith("/admin/") || requestPath.startsWith("/api/admin/")) {
                isAllowed = endpointRateLimitingService.isAdminAllowed(request, response);
            }
            // Report endpoints
            else if (requestPath.startsWith("/report/") || requestPath.startsWith("/api/report/")) {
                isAllowed = endpointRateLimitingService.isReportAllowed(request, response);
            }
            // Voucher endpoints
            else if (requestPath.startsWith("/voucher/") || requestPath.startsWith("/api/voucher/")) {
                isAllowed = endpointRateLimitingService.isVoucherAllowed(request, response);
            }
            // Waitlist endpoints
            else if (requestPath.startsWith("/waitlist/") || requestPath.startsWith("/api/waitlist/")) {
                isAllowed = endpointRateLimitingService.isWaitlistAllowed(request, response);
            }
            // Table endpoints
            else if (requestPath.startsWith("/table/") || requestPath.startsWith("/api/table/")) {
                isAllowed = endpointRateLimitingService.isTableAllowed(request, response);
            }
            // Menu endpoints
            else if (requestPath.startsWith("/menu/") || requestPath.startsWith("/api/menu/")) {
                isAllowed = endpointRateLimitingService.isMenuAllowed(request, response);
            }
            // Reservation endpoints
            else if (requestPath.startsWith("/reservation/") || requestPath.startsWith("/api/reservation/")) {
                isAllowed = endpointRateLimitingService.isReservationAllowed(request, response);
            }
            // Feedback endpoints
            else if (requestPath.startsWith("/feedback/") || requestPath.startsWith("/api/feedback/")) {
                isAllowed = endpointRateLimitingService.isFeedbackAllowed(request, response);
            }
            // Support endpoints
            else if (requestPath.startsWith("/support/") || requestPath.startsWith("/api/support/")) {
                isAllowed = endpointRateLimitingService.isSupportAllowed(request, response);
            }
            // Analytics endpoints
            else if (requestPath.startsWith("/analytics/") || requestPath.startsWith("/api/analytics/")) {
                isAllowed = endpointRateLimitingService.isAnalyticsAllowed(request, response);
            }
            // Settings endpoints
            else if (requestPath.startsWith("/settings/") || requestPath.startsWith("/api/settings/")) {
                isAllowed = endpointRateLimitingService.isSettingsAllowed(request, response);
            }
            // Dashboard endpoints
            else if (requestPath.startsWith("/dashboard/") || requestPath.startsWith("/api/dashboard/")) {
                isAllowed = endpointRateLimitingService.isDashboardAllowed(request, response);
            }
            // General endpoints (fallback)
            else {
                isAllowed = endpointRateLimitingService.isGeneralAllowed(request, response);
            }
            
        } catch (Exception e) {
            logger.error("❌ ERROR in rate limiting check for path: {}, Error: {}", requestPath, e.getMessage());
            // In case of error, allow the request but log it
            isAllowed = true;
        }
        
        if (!isAllowed) {
            // Rate limit exceeded
            logger.warn("🚫 RATE LIMIT EXCEEDED - IP: {}, Path: {}, Method: {}", 
                    getClientIpAddress(request), requestPath, method);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("X-RateLimit-Limit", "Exceeded");
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", "60");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\",\"retryAfter\":60}");
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if the request is for static resources
     */
    private boolean isStaticResource(String requestPath) {
        return requestPath.startsWith("/static/") ||
               requestPath.startsWith("/css/") ||
               requestPath.startsWith("/js/") ||
               requestPath.startsWith("/images/") ||
               requestPath.startsWith("/img/") ||
               requestPath.startsWith("/fonts/") ||
               requestPath.startsWith("/favicon.ico") ||
               requestPath.startsWith("/robots.txt") ||
               requestPath.startsWith("/sitemap.xml") ||
               requestPath.endsWith(".css") ||
               requestPath.endsWith(".js") ||
               requestPath.endsWith(".png") ||
               requestPath.endsWith(".jpg") ||
               requestPath.endsWith(".jpeg") ||
               requestPath.endsWith(".gif") ||
               requestPath.endsWith(".svg") ||
               requestPath.endsWith(".ico") ||
               requestPath.endsWith(".woff") ||
               requestPath.endsWith(".woff2") ||
               requestPath.endsWith(".ttf") ||
               requestPath.endsWith(".eot");
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
