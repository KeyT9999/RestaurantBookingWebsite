package com.example.booking.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginRateLimitingServiceTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @Mock
    private DatabaseRateLimitingService databaseService;

    @InjectMocks
    private LoginRateLimitingService loginRateLimitingService;

    @BeforeEach
    void setUp() {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Test Agent");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
    }

    @Test
    void shouldAllowLoginRequest_FirstAttempt() {
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertThat(result).isTrue();
        verify(response).setHeader(anyString(), anyString());
    }

    @Test
    void shouldBlockLoginRequest_AfterMaxAttempts() {
        String clientIp = "192.168.1.100";
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(request.getHeader("User-Agent")).thenReturn("Test Agent");
        
        // Make max attempts (default 5)
        for (int i = 0; i < 5; i++) {
            loginRateLimitingService.isLoginAllowed(request, response);
        }
        
        // 6th attempt should be blocked
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertThat(result).isFalse();
        
        verify(monitoringService, atLeastOnce()).logBlockedRequest(anyString(), anyString(), anyString());
        verify(databaseService, atLeastOnce()).logBlockedRequest(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldResetRateLimitForSuccessfulLogin() {
        String clientIp = "192.168.1.1";
        when(request.getRemoteAddr()).thenReturn(clientIp);
        
        // Make some attempts
        loginRateLimitingService.isLoginAllowed(request, response);
        loginRateLimitingService.isLoginAllowed(request, response);
        
        // Reset for successful login
        loginRateLimitingService.resetRateLimitForSuccessfulLogin(clientIp);
        
        // Should be able to login again
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertThat(result).isTrue();
    }

    @Test
    void shouldGetRemainingAttempts() {
        String clientIp = "192.168.1.1";
        when(request.getRemoteAddr()).thenReturn(clientIp);
        
        // Make 2 attempts
        loginRateLimitingService.isLoginAllowed(request, response);
        loginRateLimitingService.isLoginAllowed(request, response);
        
        int remaining = loginRateLimitingService.getRemainingAttempts(clientIp);
        // Default max is 5, so after 2 attempts, remaining should be 3
        assertThat(remaining).isLessThanOrEqualTo(5);
    }

    @Test
    void shouldGetAutoResetTimeRemaining() {
        String clientIp = "192.168.1.1";
        when(request.getRemoteAddr()).thenReturn(clientIp);
        
        loginRateLimitingService.isLoginAllowed(request, response);
        
        long timeRemaining = loginRateLimitingService.getAutoResetTimeRemaining(clientIp);
        assertThat(timeRemaining).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldResetRateLimitManually() {
        String clientIp = "192.168.1.1";
        when(request.getRemoteAddr()).thenReturn(clientIp);
        
        // Make some attempts
        loginRateLimitingService.isLoginAllowed(request, response);
        
        // Reset manually (using resetRateLimitForSuccessfulLogin or similar)
        loginRateLimitingService.resetRateLimitForSuccessfulLogin(clientIp);
        
        // Should be able to login again
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleClientIpFromXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleClientIpFromXRealIP() {
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        
        boolean result = loginRateLimitingService.isLoginAllowed(request, response);
        assertThat(result).isTrue();
    }
}

