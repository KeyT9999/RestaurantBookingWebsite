package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for EndpointRateLimitingService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EndpointRateLimitingService Tests")
public class EndpointRateLimitingServiceTest {

    @Mock
    private AdvancedRateLimitingService advancedService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private EndpointRateLimitingService endpointRateLimitingService;

    @BeforeEach
    void setUp() {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/test");
        when(advancedService.isRequestAllowed(any(HttpServletRequest.class), 
            any(HttpServletResponse.class), anyString())).thenReturn(true);
    }

    // ========== isLoginAllowed() Tests ==========

    @Test
    @DisplayName("shouldCheckLoginRateLimit_successfully")
    void shouldCheckLoginRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "login");
    }

    // ========== isRegisterAllowed() Tests ==========

    @Test
    @DisplayName("shouldCheckRegisterRateLimit_successfully")
    void shouldCheckRegisterRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isRegisterAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "register");
    }

    // ========== isForgotPasswordAllowed() Tests ==========

    @Test
    @DisplayName("shouldCheckForgotPasswordRateLimit_successfully")
    void shouldCheckForgotPasswordRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isForgotPasswordAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "forgot-password");
    }

    // ========== isResetPasswordAllowed() Tests ==========

    @Test
    @DisplayName("shouldCheckResetPasswordRateLimit_successfully")
    void shouldCheckResetPasswordRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isResetPasswordAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "reset-password");
    }

    // ========== isBookingAllowed() Tests ==========

    @Test
    @DisplayName("shouldCheckBookingRateLimit_successfully")
    void shouldCheckBookingRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isBookingAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "booking");
    }

    // ========== isChatAllowed() Tests ==========

    @Test
    @DisplayName("shouldCheckChatRateLimit_successfully")
    void shouldCheckChatRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isChatAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "chat");
    }

    // ========== isReviewAllowed() Tests ==========

    @Test
    @DisplayName("shouldCheckReviewRateLimit_successfully")
    void shouldCheckReviewRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isReviewAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "review");
    }

    // ========== Other Endpoint Tests ==========

    @Test
    @DisplayName("shouldCheckPaymentRateLimit_successfully")
    void shouldCheckPaymentRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isPaymentAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "payment");
    }

    @Test
    @DisplayName("shouldCheckFileUploadRateLimit_successfully")
    void shouldCheckFileUploadRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isFileUploadAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "file-upload");
    }

    @Test
    @DisplayName("shouldCheckAdminRateLimit_successfully")
    void shouldCheckAdminRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isAdminAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "admin");
    }

    @Test
    @DisplayName("shouldReturnFalse_whenAdvancedServiceBlocks")
    void shouldReturnFalse_whenAdvancedServiceBlocks() {
        // Given
        when(advancedService.isRequestAllowed(any(HttpServletRequest.class), 
            any(HttpServletResponse.class), anyString())).thenReturn(false);

        // When
        boolean result = endpointRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertFalse(result);
    }

    // ========== isDashboardAllowed() Tests ==========

    @Test
    @DisplayName("shouldCheckDashboardRateLimit_successfully")
    void shouldCheckDashboardRateLimit_successfully() {
        // When
        boolean result = endpointRateLimitingService.isDashboardAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService, times(1)).isRequestAllowed(request, response, "dashboard");
    }

    // ========== getClientIpAddress() Tests ==========

    @Test
    @DisplayName("shouldGetClientIpAddress_FromXForwardedForHeader")
    void shouldGetClientIpAddress_FromXForwardedForHeader() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.200");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        boolean result = endpointRateLimitingService.isLoginAllowed(request, response);

        // Then - The method should extract IP from X-Forwarded-For (first IP)
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "login");
    }

    @Test
    @DisplayName("shouldGetClientIpAddress_FromXRealIPHeader_whenXForwardedForIsNull")
    void shouldGetClientIpAddress_FromXRealIPHeader_whenXForwardedForIsNull() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.200");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        boolean result = endpointRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "login");
    }

    @Test
    @DisplayName("shouldGetClientIpAddress_FromXRealIPHeader_whenXForwardedForIsEmpty")
    void shouldGetClientIpAddress_FromXRealIPHeader_whenXForwardedForIsEmpty() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.200");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        boolean result = endpointRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "login");
    }

    @Test
    @DisplayName("shouldGetClientIpAddress_FromRemoteAddr_whenHeadersAreNull")
    void shouldGetClientIpAddress_FromRemoteAddr_whenHeadersAreNull() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // When
        boolean result = endpointRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "login");
    }

    @Test
    @DisplayName("shouldGetClientIpAddress_FromRemoteAddr_whenHeadersAreEmpty")
    void shouldGetClientIpAddress_FromRemoteAddr_whenHeadersAreEmpty() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("X-Real-IP")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // When
        boolean result = endpointRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "login");
    }

    @Test
    @DisplayName("shouldGetClientIpAddress_ExtractFirstIPFromXForwardedFor")
    void shouldGetClientIpAddress_ExtractFirstIPFromXForwardedFor() {
        // Given - X-Forwarded-For can contain multiple IPs separated by commas
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1, 172.16.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        boolean result = endpointRateLimitingService.isLoginAllowed(request, response);

        // Then - Should extract first IP (10.0.0.1)
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "login");
    }

    // ========== Additional Endpoint Tests ==========

    @Test
    @DisplayName("shouldCheckGeneralRateLimit_successfully")
    void shouldCheckGeneralRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isGeneralAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "general");
    }

    @Test
    @DisplayName("shouldCheckProfileRateLimit_successfully")
    void shouldCheckProfileRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isProfileAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "profile");
    }

    @Test
    @DisplayName("shouldCheckApiRateLimit_successfully")
    void shouldCheckApiRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isApiAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "api");
    }

    @Test
    @DisplayName("shouldCheckSearchRateLimit_successfully")
    void shouldCheckSearchRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isSearchAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "search");
    }

    @Test
    @DisplayName("shouldCheckNotificationRateLimit_successfully")
    void shouldCheckNotificationRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isNotificationAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "notification");
    }

    @Test
    @DisplayName("shouldCheckRestaurantRateLimit_successfully")
    void shouldCheckRestaurantRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isRestaurantAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "restaurant");
    }

    @Test
    @DisplayName("shouldCheckCustomerRateLimit_successfully")
    void shouldCheckCustomerRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isCustomerAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "customer");
    }

    @Test
    @DisplayName("shouldCheckReportRateLimit_successfully")
    void shouldCheckReportRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isReportAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "report");
    }

    @Test
    @DisplayName("shouldCheckVoucherRateLimit_successfully")
    void shouldCheckVoucherRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isVoucherAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "voucher");
    }

    @Test
    @DisplayName("shouldCheckWaitlistRateLimit_successfully")
    void shouldCheckWaitlistRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isWaitlistAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "waitlist");
    }

    @Test
    @DisplayName("shouldCheckTableRateLimit_successfully")
    void shouldCheckTableRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isTableAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "table");
    }

    @Test
    @DisplayName("shouldCheckMenuRateLimit_successfully")
    void shouldCheckMenuRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isMenuAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "menu");
    }

    @Test
    @DisplayName("shouldCheckReservationRateLimit_successfully")
    void shouldCheckReservationRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isReservationAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "reservation");
    }

    @Test
    @DisplayName("shouldCheckFeedbackRateLimit_successfully")
    void shouldCheckFeedbackRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isFeedbackAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "feedback");
    }

    @Test
    @DisplayName("shouldCheckSupportRateLimit_successfully")
    void shouldCheckSupportRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isSupportAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "support");
    }

    @Test
    @DisplayName("shouldCheckAnalyticsRateLimit_successfully")
    void shouldCheckAnalyticsRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isAnalyticsAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "analytics");
    }

    @Test
    @DisplayName("shouldCheckSettingsRateLimit_successfully")
    void shouldCheckSettingsRateLimit_successfully() {
        boolean result = endpointRateLimitingService.isSettingsAllowed(request, response);
        assertTrue(result);
        verify(advancedService).isRequestAllowed(request, response, "settings");
    }
}

