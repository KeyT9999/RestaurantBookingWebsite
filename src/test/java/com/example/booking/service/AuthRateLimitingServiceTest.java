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
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for AuthRateLimitingService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthRateLimitingService Tests")
public class AuthRateLimitingServiceTest {

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthRateLimitingService authRateLimitingService;

    private String clientIp;

    @BeforeEach
    void setUp() {
        clientIp = "192.168.1.1";
        ReflectionTestUtils.setField(authRateLimitingService, "maxForgotPasswordAttempts", 3);
        ReflectionTestUtils.setField(authRateLimitingService, "maxRegisterAttempts", 2);
        ReflectionTestUtils.setField(authRateLimitingService, "maxResetPasswordAttempts", 3);

        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getRequestURI()).thenReturn("/auth/test");
    }

    // ========== isForgotPasswordAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowForgotPassword_whenNoPreviousAttempts")
    void shouldAllowForgotPassword_whenNoPreviousAttempts() {
        // When
        boolean result = authRateLimitingService.isForgotPasswordAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldBlockForgotPassword_whenMaxAttemptsReached")
    void shouldBlockForgotPassword_whenMaxAttemptsReached() {
        // Given - Make 3 attempts to reach limit
        for (int i = 0; i < 3; i++) {
            authRateLimitingService.isForgotPasswordAllowed(request, response);
        }

        // When
        boolean result = authRateLimitingService.isForgotPasswordAllowed(request, response);

        // Then
        assertFalse(result);
        verify(monitoringService, atLeastOnce()).logBlockedRequest(eq(clientIp), anyString(), anyString());
    }

    // ========== isRegisterAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowRegister_whenNoPreviousAttempts")
    void shouldAllowRegister_whenNoPreviousAttempts() {
        // When
        boolean result = authRateLimitingService.isRegisterAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldBlockRegister_whenMaxAttemptsReached")
    void shouldBlockRegister_whenMaxAttemptsReached() {
        // Given - Make 2 attempts to reach limit
        for (int i = 0; i < 2; i++) {
            authRateLimitingService.isRegisterAllowed(request, response);
        }

        // When
        boolean result = authRateLimitingService.isRegisterAllowed(request, response);

        // Then
        assertFalse(result);
        verify(monitoringService, atLeastOnce()).logBlockedRequest(eq(clientIp), anyString(), anyString());
    }

    // ========== isResetPasswordAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowResetPassword_whenNoPreviousAttempts")
    void shouldAllowResetPassword_whenNoPreviousAttempts() {
        // When
        boolean result = authRateLimitingService.isResetPasswordAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldBlockResetPassword_whenMaxAttemptsReached")
    void shouldBlockResetPassword_whenMaxAttemptsReached() {
        // Given - Make 3 attempts to reach limit
        for (int i = 0; i < 3; i++) {
            authRateLimitingService.isResetPasswordAllowed(request, response);
        }

        // When
        boolean result = authRateLimitingService.isResetPasswordAllowed(request, response);

        // Then
        assertFalse(result);
        verify(monitoringService, atLeastOnce()).logBlockedRequest(eq(clientIp), anyString(), anyString());
    }

    // ========== Reset Methods Tests ==========

    @Test
    @DisplayName("shouldResetForgotPasswordRateLimit_successfully")
    void shouldResetForgotPasswordRateLimit_successfully() {
        // Given
        authRateLimitingService.isForgotPasswordAllowed(request, response);

        // When
        authRateLimitingService.resetForgotPasswordRateLimit(clientIp);

        // Then
        boolean result = authRateLimitingService.isForgotPasswordAllowed(request, response);
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldResetRegisterRateLimit_successfully")
    void shouldResetRegisterRateLimit_successfully() {
        // Given
        authRateLimitingService.isRegisterAllowed(request, response);

        // When
        authRateLimitingService.resetRegisterRateLimit(clientIp);

        // Then
        boolean result = authRateLimitingService.isRegisterAllowed(request, response);
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldResetResetPasswordRateLimit_successfully")
    void shouldResetResetPasswordRateLimit_successfully() {
        // Given
        authRateLimitingService.isResetPasswordAllowed(request, response);

        // When
        authRateLimitingService.resetResetPasswordRateLimit(clientIp);

        // Then
        boolean result = authRateLimitingService.isResetPasswordAllowed(request, response);
        assertTrue(result);
    }

    // ========== IP Address Tests ==========

    @Test
    @DisplayName("shouldUseXForwardedFor_whenPresent")
    void shouldUseXForwardedFor_whenPresent() {
        // Given
        String forwardedIp = "10.0.0.1";
        when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedIp);

        // When
        boolean result = authRateLimitingService.isForgotPasswordAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldUseXRealIP_whenPresent")
    void shouldUseXRealIP_whenPresent() {
        // Given
        String realIp = "10.0.0.2";
        when(request.getHeader("X-Real-IP")).thenReturn(realIp);

        // When
        boolean result = authRateLimitingService.isForgotPasswordAllowed(request, response);

        // Then
        assertTrue(result);
    }
}

