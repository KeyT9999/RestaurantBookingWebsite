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
 * Unit tests for GeneralRateLimitingService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GeneralRateLimitingService Tests")
public class GeneralRateLimitingServiceTest {

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private GeneralRateLimitingService generalRateLimitingService;

    private String clientIp;

    @BeforeEach
    void setUp() {
        clientIp = "192.168.1.1";
        ReflectionTestUtils.setField(generalRateLimitingService, "maxBookingAttempts", 10);
        ReflectionTestUtils.setField(generalRateLimitingService, "maxChatAttempts", 30);
        ReflectionTestUtils.setField(generalRateLimitingService, "maxReviewAttempts", 3);

        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getRequestURI()).thenReturn("/test");
    }

    // ========== isBookingAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowBooking_whenNoPreviousAttempts")
    void shouldAllowBooking_whenNoPreviousAttempts() {
        // When
        boolean result = generalRateLimitingService.isBookingAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldBlockBooking_whenMaxAttemptsReached")
    void shouldBlockBooking_whenMaxAttemptsReached() {
        // Given - Make 10 attempts to reach limit
        for (int i = 0; i < 10; i++) {
            generalRateLimitingService.isBookingAllowed(request, response);
        }

        // When
        boolean result = generalRateLimitingService.isBookingAllowed(request, response);

        // Then
        assertFalse(result);
        verify(monitoringService, atLeastOnce()).logBlockedRequest(eq(clientIp), anyString(), anyString());
    }

    // ========== isChatAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowChat_whenNoPreviousAttempts")
    void shouldAllowChat_whenNoPreviousAttempts() {
        // When
        boolean result = generalRateLimitingService.isChatAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldBlockChat_whenMaxAttemptsReached")
    void shouldBlockChat_whenMaxAttemptsReached() {
        // Given - Make 30 attempts to reach limit
        for (int i = 0; i < 30; i++) {
            generalRateLimitingService.isChatAllowed(request, response);
        }

        // When
        boolean result = generalRateLimitingService.isChatAllowed(request, response);

        // Then
        assertFalse(result);
        verify(monitoringService, atLeastOnce()).logBlockedRequest(eq(clientIp), anyString(), anyString());
    }

    // ========== isReviewAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowReview_whenNoPreviousAttempts")
    void shouldAllowReview_whenNoPreviousAttempts() {
        // When
        boolean result = generalRateLimitingService.isReviewAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldBlockReview_whenMaxAttemptsReached")
    void shouldBlockReview_whenMaxAttemptsReached() {
        // Given - Make 3 attempts to reach limit
        for (int i = 0; i < 3; i++) {
            generalRateLimitingService.isReviewAllowed(request, response);
        }

        // When
        boolean result = generalRateLimitingService.isReviewAllowed(request, response);

        // Then
        assertFalse(result);
        verify(monitoringService, atLeastOnce()).logBlockedRequest(eq(clientIp), anyString(), anyString());
    }

    // ========== Reset Methods Tests ==========

    @Test
    @DisplayName("shouldResetBookingRateLimit_successfully")
    void shouldResetBookingRateLimit_successfully() {
        // Given
        generalRateLimitingService.isBookingAllowed(request, response);

        // When
        generalRateLimitingService.resetBookingRateLimit(clientIp);

        // Then
        boolean result = generalRateLimitingService.isBookingAllowed(request, response);
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldResetChatRateLimit_successfully")
    void shouldResetChatRateLimit_successfully() {
        // Given
        generalRateLimitingService.isChatAllowed(request, response);

        // When
        generalRateLimitingService.resetChatRateLimit(clientIp);

        // Then
        boolean result = generalRateLimitingService.isChatAllowed(request, response);
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldResetReviewRateLimit_successfully")
    void shouldResetReviewRateLimit_successfully() {
        // Given
        generalRateLimitingService.isReviewAllowed(request, response);

        // When
        generalRateLimitingService.resetReviewRateLimit(clientIp);

        // Then
        boolean result = generalRateLimitingService.isReviewAllowed(request, response);
        assertTrue(result);
    }
}

