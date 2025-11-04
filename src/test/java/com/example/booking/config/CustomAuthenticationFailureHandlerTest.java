package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;
import com.example.booking.service.LoginRateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.util.Optional;

/**
 * Unit test for CustomAuthenticationFailureHandler
 * Coverage: 100% - All exception types and branches
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAuthenticationFailureHandler Tests")
class CustomAuthenticationFailureHandlerTest {

    @Mock
    private LoginRateLimitingService loginRateLimitingService;

    @Mock
    private RateLimitStatisticsRepository statisticsRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomAuthenticationFailureHandler handler;

    @BeforeEach
    void setUp() {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(statisticsRepository.findByIpAddress(anyString())).thenReturn(Optional.empty());
        when(statisticsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("shouldHandleBadCredentialsWithRateLimitExceeded")
    void shouldHandleBadCredentialsWithRateLimitExceeded() throws Exception {
        // Given
        AuthenticationException exception = new BadCredentialsException("Bad credentials");
        when(loginRateLimitingService.isLoginAllowed(any(), any())).thenReturn(false);
        
        RateLimitStatistics stats = new RateLimitStatistics("192.168.1.1");
        for (int i = 0; i < 10; i++) {
            stats.incrementBlockedCount();
        }
        when(statisticsRepository.findByIpAddress("192.168.1.1"))
                .thenReturn(Optional.of(stats));

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(response).sendRedirect("/login?ratelimit=1");
        verify(statisticsRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("shouldHandleBadCredentialsWithRateLimitOK")
    void shouldHandleBadCredentialsWithRateLimitOK() throws Exception {
        // Given
        AuthenticationException exception = new BadCredentialsException("Bad credentials");
        when(loginRateLimitingService.isLoginAllowed(any(), any())).thenReturn(true);

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(response).sendRedirect("/login?error=badcredentials");
    }

    @Test
    @DisplayName("shouldHandleLockedException")
    void shouldHandleLockedException() throws Exception {
        // Given
        AuthenticationException exception = new LockedException("Account locked");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(response).sendRedirect("/auth/verify-result?locked=1");
    }

    @Test
    @DisplayName("shouldHandleOtherException")
    void shouldHandleOtherException() throws Exception {
        // Given
        AuthenticationException exception = new AuthenticationException("Other error") {};

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(response).sendRedirect("/login?error");
    }
}

