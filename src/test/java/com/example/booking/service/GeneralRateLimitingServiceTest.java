package com.example.booking.service;

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
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeneralRateLimitingService Test Suite")
class GeneralRateLimitingServiceTest {

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private GeneralRateLimitingService generalRateLimitingService;

    @BeforeEach
    void setUp() {
        // Set default values using reflection
        ReflectionTestUtils.setField(generalRateLimitingService, "maxBookingAttempts", 10);
        ReflectionTestUtils.setField(generalRateLimitingService, "bookingWindowSeconds", 60);
        ReflectionTestUtils.setField(generalRateLimitingService, "bookingAutoResetSeconds", 300);
        ReflectionTestUtils.setField(generalRateLimitingService, "maxChatAttempts", 30);
        ReflectionTestUtils.setField(generalRateLimitingService, "chatWindowSeconds", 60);
        ReflectionTestUtils.setField(generalRateLimitingService, "chatAutoResetSeconds", 300);
        ReflectionTestUtils.setField(generalRateLimitingService, "maxReviewAttempts", 3);
        ReflectionTestUtils.setField(generalRateLimitingService, "reviewWindowSeconds", 300);
        ReflectionTestUtils.setField(generalRateLimitingService, "reviewAutoResetSeconds", 1800);
    }

    @Nested
    @DisplayName("isBookingAllowed() Tests")
    class IsBookingAllowedTests {

        @Test
        @DisplayName("Should allow booking request within limit")
        void shouldAllowBookingRequestWithinLimit() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(request.getRequestURI()).thenReturn("/booking/new");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            boolean result = generalRateLimitingService.isBookingAllowed(request, response);

            assertTrue(result);
            verify(response, atLeastOnce()).setHeader(anyString(), anyString());
        }

        @Test
        @DisplayName("Should block booking request exceeding limit")
        void shouldBlockBookingRequestExceedingLimit() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.2");
            when(request.getRequestURI()).thenReturn("/booking/new");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            // Make requests up to the limit
            for (int i = 0; i < 10; i++) {
                generalRateLimitingService.isBookingAllowed(request, response);
            }

            // 11th request should be blocked
            boolean result = generalRateLimitingService.isBookingAllowed(request, response);

            assertFalse(result);
            verify(monitoringService).logBlockedRequest(eq("192.168.1.2"), anyString(), anyString());
        }

        @Test
        @DisplayName("Should extract IP from X-Forwarded-For header")
        void shouldExtractIpFromXForwardedFor() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");
            when(request.getRequestURI()).thenReturn("/booking/new");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            boolean result = generalRateLimitingService.isBookingAllowed(request, response);

            assertTrue(result);
            verify(request).getHeader("X-Forwarded-For");
        }

        @Test
        @DisplayName("Should extract IP from X-Real-IP header")
        void shouldExtractIpFromXRealIp() {
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.200");
            when(request.getRequestURI()).thenReturn("/booking/new");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            boolean result = generalRateLimitingService.isBookingAllowed(request, response);

            assertTrue(result);
            verify(request).getHeader("X-Real-IP");
        }
    }

    @Nested
    @DisplayName("isChatAllowed() Tests")
    class IsChatAllowedTests {

        @Test
        @DisplayName("Should allow chat request within limit")
        void shouldAllowChatRequestWithinLimit() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.3");
            when(request.getRequestURI()).thenReturn("/chat/send");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            boolean result = generalRateLimitingService.isChatAllowed(request, response);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should block chat request exceeding limit")
        void shouldBlockChatRequestExceedingLimit() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.4");
            when(request.getRequestURI()).thenReturn("/chat/send");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            // Make requests up to the limit (30)
            for (int i = 0; i < 30; i++) {
                generalRateLimitingService.isChatAllowed(request, response);
            }

            // 31st request should be blocked
            boolean result = generalRateLimitingService.isChatAllowed(request, response);

            assertFalse(result);
            verify(monitoringService).logBlockedRequest(eq("192.168.1.4"), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("isReviewAllowed() Tests")
    class IsReviewAllowedTests {

        @Test
        @DisplayName("Should allow review request within limit")
        void shouldAllowReviewRequestWithinLimit() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.5");
            when(request.getRequestURI()).thenReturn("/review/submit");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            boolean result = generalRateLimitingService.isReviewAllowed(request, response);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should block review request exceeding limit")
        void shouldBlockReviewRequestExceedingLimit() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.6");
            when(request.getRequestURI()).thenReturn("/review/submit");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            // Make requests up to the limit (3)
            for (int i = 0; i < 3; i++) {
                generalRateLimitingService.isReviewAllowed(request, response);
            }

            // 4th request should be blocked
            boolean result = generalRateLimitingService.isReviewAllowed(request, response);

            assertFalse(result);
            verify(monitoringService).logBlockedRequest(eq("192.168.1.6"), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("resetBookingRateLimit() Tests")
    class ResetBookingRateLimitTests {

        @Test
        @DisplayName("Should reset booking rate limit successfully")
        void shouldResetBookingRateLimitSuccessfully() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.7");
            when(request.getRequestURI()).thenReturn("/booking/new");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            // Make some requests
            for (int i = 0; i < 5; i++) {
                generalRateLimitingService.isBookingAllowed(request, response);
            }

            // Reset rate limit
            generalRateLimitingService.resetBookingRateLimit("192.168.1.7");

            // Should be able to make requests again
            boolean result = generalRateLimitingService.isBookingAllowed(request, response);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle reset for non-existent IP")
        void shouldHandleResetForNonExistentIp() {
            assertDoesNotThrow(() -> generalRateLimitingService.resetBookingRateLimit("192.168.1.999"));
        }
    }

    @Nested
    @DisplayName("resetChatRateLimit() Tests")
    class ResetChatRateLimitTests {

        @Test
        @DisplayName("Should reset chat rate limit successfully")
        void shouldResetChatRateLimitSuccessfully() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.8");
            when(request.getRequestURI()).thenReturn("/chat/send");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            // Make some requests
            for (int i = 0; i < 15; i++) {
                generalRateLimitingService.isChatAllowed(request, response);
            }

            // Reset rate limit
            generalRateLimitingService.resetChatRateLimit("192.168.1.8");

            // Should be able to make requests again
            boolean result = generalRateLimitingService.isChatAllowed(request, response);
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("resetReviewRateLimit() Tests")
    class ResetReviewRateLimitTests {

        @Test
        @DisplayName("Should reset review rate limit successfully")
        void shouldResetReviewRateLimitSuccessfully() {
            when(request.getRemoteAddr()).thenReturn("192.168.1.9");
            when(request.getRequestURI()).thenReturn("/review/submit");
            when(request.getHeader("User-Agent")).thenReturn("TestAgent");

            // Make requests up to limit
            for (int i = 0; i < 3; i++) {
                generalRateLimitingService.isReviewAllowed(request, response);
            }

            // Reset rate limit
            generalRateLimitingService.resetReviewRateLimit("192.168.1.9");

            // Should be able to make requests again
            boolean result = generalRateLimitingService.isReviewAllowed(request, response);
            assertTrue(result);
        }
    }
}

