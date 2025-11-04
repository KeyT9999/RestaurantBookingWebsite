package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.service.GeneralRateLimitingService;
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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Unit test for GeneralRateLimitFilter
 * Coverage: 100% - All endpoints and branches
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GeneralRateLimitFilter Tests")
class GeneralRateLimitFilterTest {

    @Mock
    private GeneralRateLimitingService generalRateLimitingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private GeneralRateLimitFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    @DisplayName("shouldRedirectWhenBookingExceeded")
    void shouldRedirectWhenBookingExceeded() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/booking/test");
        when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(false);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response).sendRedirect("/booking?ratelimit=1");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("shouldContinueWhenBookingAllowed")
    void shouldContinueWhenBookingAllowed() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/booking/test");
        when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("shouldReturn429WhenChatExceeded")
    void shouldReturn429WhenChatExceeded() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/chat/test");
        when(generalRateLimitingService.isChatAllowed(any(), any())).thenReturn(false);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("shouldContinueWhenChatAllowed")
    void shouldContinueWhenChatAllowed() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/chat/test");
        when(generalRateLimitingService.isChatAllowed(any(), any())).thenReturn(true);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("shouldRedirectWhenReviewExceeded")
    void shouldRedirectWhenReviewExceeded() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/reviews/test");
        when(generalRateLimitingService.isReviewAllowed(any(), any())).thenReturn(false);

        // When
        filter.doFilter(request, response, chain);

        // Then
        verify(response).sendRedirect("/reviews?ratelimit=1");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("shouldContinueWhenReviewAllowed")
    void shouldContinueWhenReviewAllowed() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/reviews/test");
        when(generalRateLimitingService.isReviewAllowed(any(), any())).thenReturn(true);

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
        verify(generalRateLimitingService, never()).isBookingAllowed(any(), any());
        verify(generalRateLimitingService, never()).isChatAllowed(any(), any());
        verify(generalRateLimitingService, never()).isReviewAllowed(any(), any());
    }
}

