package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthRateLimitingServiceTest {

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @InjectMocks
    private AuthRateLimitingService authRateLimitingService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(authRateLimitingService, "maxForgotPasswordAttempts", 2);
        ReflectionTestUtils.setField(authRateLimitingService, "forgotPasswordWindowSeconds", 60);
        ReflectionTestUtils.setField(authRateLimitingService, "forgotPasswordAutoResetSeconds", 120);

        ReflectionTestUtils.setField(authRateLimitingService, "maxRegisterAttempts", 2);
        ReflectionTestUtils.setField(authRateLimitingService, "registerWindowSeconds", 60);
        ReflectionTestUtils.setField(authRateLimitingService, "registerAutoResetSeconds", 120);

        ReflectionTestUtils.setField(authRateLimitingService, "maxResetPasswordAttempts", 2);
        ReflectionTestUtils.setField(authRateLimitingService, "resetPasswordWindowSeconds", 60);
        ReflectionTestUtils.setField(authRateLimitingService, "resetPasswordAutoResetSeconds", 120);
    }

    @Test
    void isForgotPasswordAllowed_whenWithinLimit_shouldReturnTrueAndSetHeaders() {
        MockHttpServletRequest request = buildRequest("1.1.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authRateLimitingService.isForgotPasswordAllowed(request, response);

        assertTrue(allowed);
        assertEquals("2", response.getHeader("X-RateLimit-Limit"));
        assertEquals("1", response.getHeader("X-RateLimit-Remaining"));
        assertEquals("60", response.getHeader("X-RateLimit-Reset"));
    }

    @Test
    void isForgotPasswordAllowed_whenLimitExceeded_shouldBlockAndLog() {
        ReflectionTestUtils.setField(authRateLimitingService, "maxForgotPasswordAttempts", 1);

        MockHttpServletRequest request = buildRequest("2.2.2.2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));

        boolean allowed = authRateLimitingService.isForgotPasswordAllowed(request, response);

        assertFalse(allowed);
        verify(monitoringService).logBlockedRequest("2.2.2.2", "/auth/forgot-password", "JUnit");
    }

    @Test
    void resetForgotPasswordRateLimit_shouldAllowRequestsAgain() {
        ReflectionTestUtils.setField(authRateLimitingService, "maxForgotPasswordAttempts", 1);

        MockHttpServletRequest request = buildRequest("3.3.3.3");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, response));
        assertFalse(authRateLimitingService.isForgotPasswordAllowed(request, response));

        authRateLimitingService.resetForgotPasswordRateLimit("3.3.3.3");

        MockHttpServletResponse afterReset = new MockHttpServletResponse();
        assertTrue(authRateLimitingService.isForgotPasswordAllowed(request, afterReset));
    }

    @Test
    void isRegisterAllowed_shouldDelegateToRegisterRateLimiter() {
        MockHttpServletRequest request = buildRequest("4.4.4.4");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(authRateLimitingService.isRegisterAllowed(request, response));
        assertTrue(authRateLimitingService.isRegisterAllowed(request, response));
        assertFalse(authRateLimitingService.isRegisterAllowed(request, response));
        verify(monitoringService).logBlockedRequest("4.4.4.4", "/auth/register", "JUnit");
    }

    private MockHttpServletRequest buildRequest(String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddress);
        request.addHeader("User-Agent", "JUnit");
        return request;
    }
}
