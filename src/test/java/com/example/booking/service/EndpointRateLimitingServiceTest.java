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
}

