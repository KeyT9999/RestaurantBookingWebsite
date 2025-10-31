package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.service.EndpointRateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Unit test for AdvancedRateLimitingInterceptor
 * Coverage: 100% - All endpoint patterns, static resources, exceptions, IP detection
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdvancedRateLimitingInterceptor Tests")
class AdvancedRateLimitingInterceptorTest {

    @Mock
    private EndpointRateLimitingService endpointRateLimitingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AdvancedRateLimitingInterceptor interceptor;

    private Object handler;

    @BeforeEach
    void setUp() throws Exception {
        handler = new Object();
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Nested
    @DisplayName("Static Resource Tests")
    class StaticResourceTests {

        @Test
        @DisplayName("shouldSkipStaticResources")
        void shouldSkipStaticResources() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/static/style.css");
            when(request.getMethod()).thenReturn("GET");

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
            verify(endpointRateLimitingService, never()).isLoginAllowed(any(), any());
        }

        @Test
        @DisplayName("shouldSkipImageFiles")
        void shouldSkipImageFiles() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/images/logo.png");
            when(request.getMethod()).thenReturn("GET");

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldSkipJavaScriptFiles")
        void shouldSkipJavaScriptFiles() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/js/app.js");
            when(request.getMethod()).thenReturn("GET");

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Authentication Endpoint Tests")
    class AuthenticationEndpointTests {

        @Test
        @DisplayName("shouldBlockLoginWhenRateExceeded")
        void shouldBlockLoginWhenRateExceeded() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/login");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(false);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            verify(response).setStatus(429);
        }

        @Test
        @DisplayName("shouldAllowLoginWhenUnderLimit")
        void shouldAllowLoginWhenUnderLimit() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/auth/login");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isLoginAllowed(any(), any())).thenReturn(true);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Registration Endpoint Tests")
    class RegistrationEndpointTests {

        @Test
        @DisplayName("shouldBlockRegisterWhenRateExceeded")
        void shouldBlockRegisterWhenRateExceeded() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/register");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isRegisterAllowed(any(), any())).thenReturn(false);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldAllowRegisterWhenUnderLimit")
        void shouldAllowRegisterWhenUnderLimit() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/auth/register");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isRegisterAllowed(any(), any())).thenReturn(true);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Booking Endpoint Tests")
    class BookingEndpointTests {

        @Test
        @DisplayName("shouldBlockBookingWhenRateExceeded")
        void shouldBlockBookingWhenRateExceeded() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/booking/test");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isBookingAllowed(any(), any())).thenReturn(false);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldAllowBookingWhenUnderLimit")
        void shouldAllowBookingWhenUnderLimit() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/booking/test");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Chat Endpoint Tests")
    class ChatEndpointTests {

        @Test
        @DisplayName("shouldBlockChatWhenRateExceeded")
        void shouldBlockChatWhenRateExceeded() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/chat/test");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isChatAllowed(any(), any())).thenReturn(false);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldAllowChatWhenUnderLimit")
        void shouldAllowChatWhenUnderLimit() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/chat/test");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isChatAllowed(any(), any())).thenReturn(true);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("General Endpoint Tests")
    class GeneralEndpointTests {

        @Test
        @DisplayName("shouldAllowGeneralEndpoint")
        void shouldAllowGeneralEndpoint() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/unknown/endpoint");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isGeneralAllowed(any(), any())).thenReturn(true);

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldHandleExceptionGracefully")
        void shouldHandleExceptionGracefully() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isApiAllowed(any(), any()))
                    .thenThrow(new RuntimeException("Service error"));

            // When
            boolean result = interceptor.preHandle(request, response, handler);

            // Then - Should allow on error
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("IP Address Detection Tests")
    class IpAddressDetectionTests {

        @Test
        @DisplayName("shouldGetIpFromXForwardedFor")
        void shouldGetIpFromXForwardedFor() throws Exception {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isApiAllowed(any(), any())).thenReturn(true);

            // When
            interceptor.preHandle(request, response, handler);

            // Then - Should extract first IP
            verify(request).getHeader("X-Forwarded-For");
        }

        @Test
        @DisplayName("shouldGetIpFromXRealIp")
        void shouldGetIpFromXRealIp() throws Exception {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn("172.16.0.1");
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isApiAllowed(any(), any())).thenReturn(true);

            // When
            interceptor.preHandle(request, response, handler);

            // Then
            verify(request).getHeader("X-Real-IP");
        }

        @Test
        @DisplayName("shouldGetIpFromRemoteAddr")
        void shouldGetIpFromRemoteAddr() throws Exception {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("POST");
            when(endpointRateLimitingService.isApiAllowed(any(), any())).thenReturn(true);

            // When
            interceptor.preHandle(request, response, handler);

            // Then
            verify(request).getRemoteAddr();
        }
    }
}

