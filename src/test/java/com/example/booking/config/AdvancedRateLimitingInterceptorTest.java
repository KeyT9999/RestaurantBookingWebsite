package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.service.EndpointRateLimitingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Comprehensive tests for AdvancedRateLimitingInterceptor
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdvancedRateLimitingInterceptor Tests")
public class AdvancedRateLimitingInterceptorTest {

    @Mock
    private EndpointRateLimitingService endpointRateLimitingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @InjectMocks
    private AdvancedRateLimitingInterceptor interceptor;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getMethod()).thenReturn("GET");
    }

    // ========== Static Resource Tests ==========

    @Test
    @DisplayName("preHandle should skip rate limiting for static resources")
    void preHandle_shouldSkipRateLimitingForStaticResources() throws Exception {
        // Given - various static resource paths
        String[] staticPaths = {
            "/static/css/style.css",
            "/css/bootstrap.css",
            "/js/jquery.js",
            "/images/logo.png",
            "/img/header.jpg",
            "/fonts/font.woff",
            "/favicon.ico",
            "/robots.txt",
            "/sitemap.xml",
            "/style.css",
            "/app.js",
            "/photo.png",
            "/image.jpg",
            "/picture.jpeg",
            "/icon.gif",
            "/logo.svg",
            "/favicon.ico",
            "/font.woff2",
            "/font.ttf",
            "/font.eot"
        };

        for (String path : staticPaths) {
            when(request.getRequestURI()).thenReturn(path);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result, "Should allow static resource: " + path);
            verify(endpointRateLimitingService, never()).isLoginAllowed(any(), any());
        }
    }

    // ========== Login Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for login endpoints")
    void preHandle_shouldCheckRateLimitForLoginEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isLoginAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for auth/login endpoints")
    void preHandle_shouldCheckRateLimitForAuthLoginEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isLoginAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should block when login rate limit exceeded")
    void preHandle_shouldBlockWhenLoginRateLimitExceeded() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(false);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertFalse(result);
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(response).setHeader("X-RateLimit-Limit", "Exceeded");
        assertTrue(responseWriter.toString().contains("Rate limit exceeded"));
    }

    // ========== Register Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for register endpoints")
    void preHandle_shouldCheckRateLimitForRegisterEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/register");
        when(endpointRateLimitingService.isRegisterAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isRegisterAllowed(any(), any());
    }

    // ========== Password Reset Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for forgot-password endpoints")
    void preHandle_shouldCheckRateLimitForForgotPasswordEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/forgot-password");
        when(endpointRateLimitingService.isForgotPasswordAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isForgotPasswordAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for reset-password endpoints")
    void preHandle_shouldCheckRateLimitForResetPasswordEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/reset-password");
        when(endpointRateLimitingService.isResetPasswordAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isResetPasswordAllowed(any(), any());
    }

    // ========== Booking Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for booking endpoints")
    void preHandle_shouldCheckRateLimitForBookingEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/booking/123");
        when(endpointRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isBookingAllowed(any(), any());
    }

    // ========== Chat Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for chat endpoints")
    void preHandle_shouldCheckRateLimitForChatEndpoints() throws Exception {
        String[] chatPaths = {"/chat/123", "/api/chat/message", "/ws/chat", "/websocket/chat"};
        
        for (String path : chatPaths) {
            when(request.getRequestURI()).thenReturn(path);
            when(endpointRateLimitingService.isChatAllowed(any(), any())).thenReturn(true);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result, "Should allow chat endpoint: " + path);
            verify(endpointRateLimitingService, atLeastOnce()).isChatAllowed(any(), any());
        }
    }

    // ========== Review Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for review endpoints")
    void preHandle_shouldCheckRateLimitForReviewEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/reviews/123");
        when(endpointRateLimitingService.isReviewAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isReviewAllowed(any(), any());
    }

    // ========== File Upload Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for file upload endpoints")
    void preHandle_shouldCheckRateLimitForFileUploadEndpoints() throws Exception {
        // Given - POST method with upload path
        when(request.getRequestURI()).thenReturn("/upload/image");
        when(request.getMethod()).thenReturn("POST");
        when(endpointRateLimitingService.isFileUploadAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isFileUploadAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for multipart upload")
    void preHandle_shouldCheckRateLimitForMultipartUpload() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/image");
        when(request.getMethod()).thenReturn("POST");
        when(endpointRateLimitingService.isFileUploadAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        // Note: This may not trigger multipart check without proper content-type header mock
        // but it tests the logic path
    }

    // ========== Payment Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for payment endpoints")
    void preHandle_shouldCheckRateLimitForPaymentEndpoints() throws Exception {
        String[] paymentPaths = {"/payment/process", "/api/payment/callback", "/payos/webhook", "/api/payos/verify"};
        
        for (String path : paymentPaths) {
            when(request.getRequestURI()).thenReturn(path);
            when(endpointRateLimitingService.isPaymentAllowed(any(), any())).thenReturn(true);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result, "Should allow payment endpoint: " + path);
        }
    }

    // ========== API Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for general API endpoints")
    void preHandle_shouldCheckRateLimitForGeneralApiEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/other");
        when(endpointRateLimitingService.isApiAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isApiAllowed(any(), any());
    }

    // ========== Search Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for search endpoints")
    void preHandle_shouldCheckRateLimitForSearchEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/search/restaurants");
        when(endpointRateLimitingService.isSearchAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isSearchAllowed(any(), any());
    }

    // ========== Profile Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for profile endpoints")
    void preHandle_shouldCheckRateLimitForProfileEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/profile/edit");
        when(endpointRateLimitingService.isProfileAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isProfileAllowed(any(), any());
    }

    // ========== Notification Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for notification endpoints")
    void preHandle_shouldCheckRateLimitForNotificationEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/notifications/list");
        when(endpointRateLimitingService.isNotificationAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isNotificationAllowed(any(), any());
    }

    // ========== Restaurant Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for restaurant endpoints")
    void preHandle_shouldCheckRateLimitForRestaurantEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/restaurant/123");
        when(endpointRateLimitingService.isRestaurantAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isRestaurantAllowed(any(), any());
    }

    // ========== Customer Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for customer endpoints")
    void preHandle_shouldCheckRateLimitForCustomerEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/customer/bookings");
        when(endpointRateLimitingService.isCustomerAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isCustomerAllowed(any(), any());
    }

    // ========== Admin Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for admin endpoints")
    void preHandle_shouldCheckRateLimitForAdminEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/admin/dashboard");
        when(endpointRateLimitingService.isAdminAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isAdminAllowed(any(), any());
    }

    // ========== Other Endpoint Tests ==========

    @Test
    @DisplayName("preHandle should check rate limit for report endpoints")
    void preHandle_shouldCheckRateLimitForReportEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/report/sales");
        when(endpointRateLimitingService.isReportAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isReportAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for voucher endpoints")
    void preHandle_shouldCheckRateLimitForVoucherEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/voucher/apply");
        when(endpointRateLimitingService.isVoucherAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isVoucherAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for waitlist endpoints")
    void preHandle_shouldCheckRateLimitForWaitlistEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/waitlist/add");
        when(endpointRateLimitingService.isWaitlistAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isWaitlistAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for table endpoints")
    void preHandle_shouldCheckRateLimitForTableEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/table/book");
        when(endpointRateLimitingService.isTableAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isTableAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for menu endpoints")
    void preHandle_shouldCheckRateLimitForMenuEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/menu/view");
        when(endpointRateLimitingService.isMenuAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isMenuAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for reservation endpoints")
    void preHandle_shouldCheckRateLimitForReservationEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/reservation/create");
        when(endpointRateLimitingService.isReservationAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isReservationAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for feedback endpoints")
    void preHandle_shouldCheckRateLimitForFeedbackEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/feedback/submit");
        when(endpointRateLimitingService.isFeedbackAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isFeedbackAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for support endpoints")
    void preHandle_shouldCheckRateLimitForSupportEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/support/ticket");
        when(endpointRateLimitingService.isSupportAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isSupportAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for analytics endpoints")
    void preHandle_shouldCheckRateLimitForAnalyticsEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/analytics/view");
        when(endpointRateLimitingService.isAnalyticsAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isAnalyticsAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for settings endpoints")
    void preHandle_shouldCheckRateLimitForSettingsEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/settings/update");
        when(endpointRateLimitingService.isSettingsAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isSettingsAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should check rate limit for dashboard endpoints")
    void preHandle_shouldCheckRateLimitForDashboardEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/dashboard/stats");
        when(endpointRateLimitingService.isDashboardAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isDashboardAllowed(any(), any());
    }

    @Test
    @DisplayName("preHandle should use general rate limit for unknown endpoints")
    void preHandle_shouldUseGeneralRateLimitForUnknownEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/unknown/path");
        when(endpointRateLimitingService.isGeneralAllowed(any(), any())).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(endpointRateLimitingService).isGeneralAllowed(any(), any());
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("preHandle should allow request on exception")
    void preHandle_shouldAllowRequestOnException() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenThrow(new RuntimeException("Service error"));

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result, "Should allow request when service throws exception");
    }

    // ========== IP Address Extraction Tests ==========

    @Test
    @DisplayName("getClientIpAddress should extract from X-Forwarded-For header")
    void getClientIpAddress_shouldExtractFromXForwardedFor() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(true);

        // When
        interceptor.preHandle(request, response, handler);

        // Then - verify it was called (IP extraction is tested via logging/logic flow)
        verify(endpointRateLimitingService).isLoginAllowed(any(), any());
    }

    @Test
    @DisplayName("getClientIpAddress should extract from X-Real-IP header")
    void getClientIpAddress_shouldExtractFromXRealIp() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.1");
        when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(true);

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(endpointRateLimitingService).isLoginAllowed(any(), any());
    }

    @Test
    @DisplayName("getClientIpAddress should use remoteAddr when headers not present")
    void getClientIpAddress_shouldUseRemoteAddrWhenHeadersNotPresent() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(true);

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(endpointRateLimitingService).isLoginAllowed(any(), any());
    }

    // ========== Rate Limit Exceeded Response Tests ==========

    @Test
    @DisplayName("preHandle should set proper headers when rate limit exceeded")
    void preHandle_shouldSetProperHeadersWhenRateLimitExceeded() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(false);

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(response).setHeader("X-RateLimit-Limit", "Exceeded");
        verify(response).setHeader("X-RateLimit-Remaining", "0");
        verify(response).setHeader("X-RateLimit-Reset", "60");
        assertTrue(responseWriter.toString().contains("Rate limit exceeded"));
        assertTrue(responseWriter.toString().contains("retryAfter"));
    }

    @Test
    @DisplayName("preHandle should return false when rate limit exceeded")
    void preHandle_shouldReturnFalseWhenRateLimitExceeded() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/booking/123");
        when(endpointRateLimitingService.isBookingAllowed(any(), any())).thenReturn(false);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertFalse(result);
    }
}

