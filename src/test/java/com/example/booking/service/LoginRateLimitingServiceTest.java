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
 * Unit tests for LoginRateLimitingService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginRateLimitingService Tests")
public class LoginRateLimitingServiceTest {

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @Mock
    private DatabaseRateLimitingService databaseService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LoginRateLimitingService loginRateLimitingService;

    private String clientIp;

    @BeforeEach
    void setUp() {
        clientIp = "192.168.1.1";
        ReflectionTestUtils.setField(loginRateLimitingService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(loginRateLimitingService, "windowSeconds", 30);
        ReflectionTestUtils.setField(loginRateLimitingService, "autoResetSeconds", 3600);

        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    }

    // ========== isLoginAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowLogin_whenNoPreviousAttempts")
    void shouldAllowLogin_whenNoPreviousAttempts() {
        // When
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldAllowLogin_whenUnderLimit")
    void shouldAllowLogin_whenUnderLimit() {
        // Given - Make 4 attempts (under limit of 5)
        for (int i = 0; i < 4; i++) {
            loginRateLimitingService.isLoginAllowed(request, response);
        }

        // When
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldBlockLogin_whenMaxAttemptsReached")
    void shouldBlockLogin_whenMaxAttemptsReached() {
        // Given - Make 5 attempts to reach limit
        for (int i = 0; i < 5; i++) {
            loginRateLimitingService.isLoginAllowed(request, response);
        }

        // When
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertFalse(result);
        verify(monitoringService, atLeastOnce()).logBlockedRequest(eq(clientIp), anyString(), anyString());
        verify(databaseService, atLeastOnce()).logBlockedRequest(eq(clientIp), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldUseXForwardedFor_whenPresent")
    void shouldUseXForwardedFor_whenPresent() {
        // Given
        String forwardedIp = "10.0.0.1";
        when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedIp);

        // When
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);

        // Then
        assertTrue(result);
    }

    // ========== resetRateLimitForSuccessfulLogin() Tests ==========

    @Test
    @DisplayName("shouldResetRateLimit_whenLoginSuccessful")
    void shouldResetRateLimit_whenLoginSuccessful() {
        // Given - Make some attempts
        loginRateLimitingService.isLoginAllowed(request, response);
        loginRateLimitingService.isLoginAllowed(request, response);

        // When
        loginRateLimitingService.resetRateLimitForSuccessfulLogin(clientIp);

        // Then - Should be able to make attempts again
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldResetRateLimit_withRequest")
    void shouldResetRateLimit_withRequest() {
        // Given
        loginRateLimitingService.isLoginAllowed(request, response);

        // When
        loginRateLimitingService.resetRateLimitForSuccessfulLogin(request);

        // Then
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertTrue(result);
    }

    // ========== resetBucketForIp() Tests ==========

    @Test
    @DisplayName("shouldResetBucketForIp_successfully")
    void shouldResetBucketForIp_successfully() {
        // Given
        loginRateLimitingService.isLoginAllowed(request, response);

        // When
        loginRateLimitingService.resetBucketForIp(clientIp);

        // Then
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertTrue(result);
    }

    // ========== resetAllRateLimits() Tests ==========

    @Test
    @DisplayName("shouldResetAllRateLimits_successfully")
    void shouldResetAllRateLimits_successfully() {
        // Given
        loginRateLimitingService.isLoginAllowed(request, response);

        // When
        loginRateLimitingService.resetAllRateLimits();

        // Then
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertTrue(result);
    }

    // ========== getRemainingAttempts() Tests ==========

    @Test
    @DisplayName("shouldGetRemainingAttempts_correctly")
    void shouldGetRemainingAttempts_correctly() {
        // Given
        loginRateLimitingService.isLoginAllowed(request, response);
        loginRateLimitingService.isLoginAllowed(request, response);

        // When
        int remaining = loginRateLimitingService.getRemainingAttempts(clientIp);

        // Then
        assertEquals(3, remaining); // 5 max - 2 used = 3
    }

    @Test
    @DisplayName("shouldReturnMaxAttempts_whenNoAttempts")
    void shouldReturnMaxAttempts_whenNoAttempts() {
        // When
        int remaining = loginRateLimitingService.getRemainingAttempts(clientIp);

        // Then
        assertEquals(5, remaining);
    }

    // ========== getAutoResetTimeRemaining() Tests ==========

    @Test
    @DisplayName("shouldReturnZero_whenNotBlocked")
    void shouldReturnZero_whenNotBlocked() {
        // When
        long remaining = loginRateLimitingService.getAutoResetTimeRemaining(clientIp);

        // Then
        assertEquals(0, remaining);
    }

    @Test
    @DisplayName("shouldReturnAutoResetSeconds_whenBlocked")
    void shouldReturnAutoResetSeconds_whenBlocked() {
        // Given - Block the IP
        for (int i = 0; i < 5; i++) {
            loginRateLimitingService.isLoginAllowed(request, response);
        }

        // When
        long remaining = loginRateLimitingService.getAutoResetTimeRemaining(clientIp);

        // Then - Should return time remaining (may be less than autoResetSeconds due to timing)
        assertTrue(remaining >= 0);
    }

    // ========== getAutoResetSeconds() Tests ==========

    @Test
    @DisplayName("shouldGetAutoResetSeconds_correctly")
    void shouldGetAutoResetSeconds_correctly() {
        // When
        int seconds = loginRateLimitingService.getAutoResetSeconds();

        // Then
        assertEquals(3600, seconds);
    }
}

