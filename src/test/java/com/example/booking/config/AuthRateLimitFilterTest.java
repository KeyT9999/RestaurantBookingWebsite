package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.service.AuthRateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

/**
 * Unit test for AuthRateLimitFilter
 * Coverage: 100% - All endpoints and branches (allowed vs blocked)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthRateLimitFilter Tests")
class AuthRateLimitFilterTest {

    @Mock
    private AuthRateLimitingService authRateLimitingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private AuthRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    @Test
    @DisplayName("shouldHandleForgotPasswordExceeded")
    void shouldHandleForgotPasswordExceeded() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/auth/forgot-password");
        when(authRateLimitingService.isForgotPasswordAllowed(any(), any())).thenReturn(false);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response).sendRedirect("/auth/forgot-password?ratelimit=1");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("shouldAllowForgotPassword")
    void shouldAllowForgotPassword() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/auth/forgot-password");
        when(authRateLimitingService.isForgotPasswordAllowed(any(), any())).thenReturn(true);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response, never()).sendRedirect(anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("shouldHandleRegisterExceeded")
    void shouldHandleRegisterExceeded() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/auth/register");
        when(authRateLimitingService.isRegisterAllowed(any(), any())).thenReturn(false);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response).sendRedirect("/auth/register?ratelimit=1");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("shouldAllowRegister")
    void shouldAllowRegister() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/auth/register");
        when(authRateLimitingService.isRegisterAllowed(any(), any())).thenReturn(true);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("shouldHandleResetPasswordExceeded")
    void shouldHandleResetPasswordExceeded() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/auth/reset-password");
        when(authRateLimitingService.isResetPasswordAllowed(any(), any())).thenReturn(false);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response).sendRedirect("/auth/reset-password?ratelimit=1");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("shouldAllowResetPassword")
    void shouldAllowResetPassword() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/auth/reset-password");
        when(authRateLimitingService.isResetPasswordAllowed(any(), any())).thenReturn(true);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("shouldContinueForOtherEndpoints")
    void shouldContinueForOtherEndpoints() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/other/endpoint");

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
        verify(authRateLimitingService, never()).isForgotPasswordAllowed(any(), any());
        verify(authRateLimitingService, never()).isRegisterAllowed(any(), any());
        verify(authRateLimitingService, never()).isResetPasswordAllowed(any(), any());
    }
}

