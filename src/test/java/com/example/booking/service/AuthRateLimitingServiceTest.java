package com.example.booking.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRateLimitingServiceTest {

    private AuthRateLimitingService authRateLimitingService;

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        authRateLimitingService = new AuthRateLimitingService();

        ReflectionTestUtils.setField(authRateLimitingService, "monitoringService", monitoringService);

        ReflectionTestUtils.setField(authRateLimitingService, "maxForgotPasswordAttempts", 3);
        ReflectionTestUtils.setField(authRateLimitingService, "forgotPasswordWindowSeconds", 300);
        ReflectionTestUtils.setField(authRateLimitingService, "forgotPasswordAutoResetSeconds", 60);

        ReflectionTestUtils.setField(authRateLimitingService, "maxRegisterAttempts", 2);
        ReflectionTestUtils.setField(authRateLimitingService, "registerWindowSeconds", 120);
        ReflectionTestUtils.setField(authRateLimitingService, "registerAutoResetSeconds", 60);

        ReflectionTestUtils.setField(authRateLimitingService, "maxResetPasswordAttempts", 3);
        ReflectionTestUtils.setField(authRateLimitingService, "resetPasswordWindowSeconds", 300);
        ReflectionTestUtils.setField(authRateLimitingService, "resetPasswordAutoResetSeconds", 60);
    }

    @Test
    void shouldBlockForgotPasswordAfterMaxAttempts() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.10, 203.0.113.5");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));
        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));
        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));

        assertFalse(authRateLimitingService.isForgotPasswordAllowed(request, response));

        verify(response, atLeastOnce()).setHeader(eq("X-RateLimit-Limit"), eq("3"));
        verify(response, atLeastOnce()).setHeader(eq("X-RateLimit-Reset"), eq("300"));
        verify(response, atLeastOnce()).setHeader(eq("X-RateLimit-Remaining"), eq("0"));

        verify(monitoringService).logBlockedRequest(eq("198.51.100.10"), eq("/auth/forgot-password"), eq("JUnit"));
    }

    @Test
    void shouldAutoResetForgotPasswordAttemptsAfterWindow() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.99");

        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));

        Object attemptInfo = getAttemptInfo("forgotPasswordAttempts", "203.0.113.99");
        ReflectionTestUtils.setField(attemptInfo, "attemptCount", 3);
        ReflectionTestUtils.setField(attemptInfo, "firstAttemptTime", LocalDateTime.now().minusSeconds(120));

        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));

        Object refreshedAttemptInfo = getAttemptInfo("forgotPasswordAttempts", "203.0.113.99");
        int attemptCount = (int) ReflectionTestUtils.getField(refreshedAttemptInfo, "attemptCount");
        assertEquals(1, attemptCount);

        verify(monitoringService, never()).logBlockedRequest(anyString(), anyString(), anyString());
    }

    @Test
    void shouldResetForgotPasswordRateLimitManually() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.77");

        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));
        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));

        authRateLimitingService.resetForgotPasswordRateLimit("198.51.100.77");

        Object attemptInfo = getAttemptInfo("forgotPasswordAttempts", "198.51.100.77");
        int attemptCount = (int) ReflectionTestUtils.getField(attemptInfo, "attemptCount");
        assertEquals(0, attemptCount);
    }

    @Test
    void shouldRespectRealIpHeaderForRegisterRateLimiting() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("192.0.2.44");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        assertTrue(authRateLimitingService.isRegisterAllowed(request, response));
        assertTrue(authRateLimitingService.isRegisterAllowed(request, response));
        assertFalse(authRateLimitingService.isRegisterAllowed(request, response));

        verify(response, atLeastOnce()).setHeader(eq("X-RateLimit-Limit"), eq("2"));
        verify(response, atLeastOnce()).setHeader(eq("X-RateLimit-Reset"), eq("120"));

        verify(monitoringService).logBlockedRequest(eq("192.0.2.44"), eq("/auth/register"), eq("JUnit"));
    }

    private Object getAttemptInfo(String fieldName, String ip) {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Object> attempts = (ConcurrentHashMap<String, Object>)
                ReflectionTestUtils.getField(authRateLimitingService, fieldName);
        return attempts.get(ip);
    }
}

