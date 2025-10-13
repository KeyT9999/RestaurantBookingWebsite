package com.example.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to integrate rate limiting with new endpoints
 */
@Service
public class EndpointRateLimitingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EndpointRateLimitingService.class);
    
    @Autowired
    private AdvancedRateLimitingService advancedService;
    
    /**
     * Check rate limit for login operations
     */
    public boolean isLoginAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 LOGIN RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "login");
    }
    
    /**
     * Check rate limit for register operations
     */
    public boolean isRegisterAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 REGISTER RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "register");
    }
    
    /**
     * Check rate limit for forgot password operations
     */
    public boolean isForgotPasswordAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 FORGOT PASSWORD RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "forgot-password");
    }
    
    /**
     * Check rate limit for reset password operations
     */
    public boolean isResetPasswordAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 RESET PASSWORD RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "reset-password");
    }
    
    /**
     * Check rate limit for booking operations
     */
    public boolean isBookingAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 BOOKING RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "booking");
    }
    
    /**
     * Check rate limit for chat operations
     */
    public boolean isChatAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 CHAT RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "chat");
    }
    
    /**
     * Check rate limit for review operations
     */
    public boolean isReviewAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 REVIEW RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "review");
    }
    
    /**
     * Check rate limit for general operations
     */
    public boolean isGeneralAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 GENERAL RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "general");
    }
    
    /**
     * Check rate limit for file upload operations
     */
    public boolean isFileUploadAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 FILE UPLOAD RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "file-upload");
    }
    
    /**
     * Check rate limit for payment operations
     */
    public boolean isPaymentAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 PAYMENT RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "payment");
    }
    
    /**
     * Check rate limit for API operations
     */
    public boolean isApiAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 API RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "api");
    }
    
    /**
     * Check rate limit for search operations
     */
    public boolean isSearchAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 SEARCH RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "search");
    }
    
    /**
     * Check rate limit for profile operations
     */
    public boolean isProfileAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 PROFILE RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "profile");
    }
    
    /**
     * Check rate limit for notification operations
     */
    public boolean isNotificationAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 NOTIFICATION RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "notification");
    }
    
    /**
     * Check rate limit for restaurant operations
     */
    public boolean isRestaurantAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 RESTAURANT RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "restaurant");
    }
    
    /**
     * Check rate limit for customer operations
     */
    public boolean isCustomerAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 CUSTOMER RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "customer");
    }
    
    /**
     * Check rate limit for admin operations
     */
    public boolean isAdminAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 ADMIN RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "admin");
    }
    
    /**
     * Check rate limit for report operations
     */
    public boolean isReportAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 REPORT RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "report");
    }
    
    /**
     * Check rate limit for voucher operations
     */
    public boolean isVoucherAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 VOUCHER RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "voucher");
    }
    
    /**
     * Check rate limit for waitlist operations
     */
    public boolean isWaitlistAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 WAITLIST RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "waitlist");
    }
    
    /**
     * Check rate limit for table operations
     */
    public boolean isTableAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 TABLE RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "table");
    }
    
    /**
     * Check rate limit for menu operations
     */
    public boolean isMenuAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 MENU RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "menu");
    }
    
    /**
     * Check rate limit for reservation operations
     */
    public boolean isReservationAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 RESERVATION RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "reservation");
    }
    
    /**
     * Check rate limit for feedback operations
     */
    public boolean isFeedbackAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 FEEDBACK RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "feedback");
    }
    
    /**
     * Check rate limit for support operations
     */
    public boolean isSupportAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 SUPPORT RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "support");
    }
    
    /**
     * Check rate limit for analytics operations
     */
    public boolean isAnalyticsAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 ANALYTICS RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "analytics");
    }
    
    /**
     * Check rate limit for settings operations
     */
    public boolean isSettingsAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 SETTINGS RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "settings");
    }
    
    /**
     * Check rate limit for dashboard operations
     */
    public boolean isDashboardAllowed(HttpServletRequest request, HttpServletResponse response) {
        logger.info("🔍 DASHBOARD RATE LIMIT CHECK - IP: {}, Path: {}", 
                getClientIpAddress(request), request.getRequestURI());
        
        return advancedService.isRequestAllowed(request, response, "dashboard");
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
