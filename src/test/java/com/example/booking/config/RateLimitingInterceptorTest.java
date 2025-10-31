package com.example.booking.config;

import com.example.booking.service.GeneralRateLimitingService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingInterceptor Test Suite")
class RateLimitingInterceptorTest {

    @Mock
    private GeneralRateLimitingService generalRateLimitingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @InjectMocks
    private RateLimitingInterceptor interceptor;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Nested
    @DisplayName("preHandle() Tests - HTTP Methods")
    class HttpMethodTests {

        @Test
        @DisplayName("Should allow GET requests without rate limiting")
        void testPreHandle_ShouldAllowGet() throws Exception {
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/booking/new");

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService, never()).isBookingAllowed(any(), any());
            verify(generalRateLimitingService, never()).isChatAllowed(any(), any());
            verify(generalRateLimitingService, never()).isReviewAllowed(any(), any());
        }

        @Test
        @DisplayName("Should allow HEAD requests without rate limiting")
        void testPreHandle_ShouldAllowHead() throws Exception {
            when(request.getMethod()).thenReturn("HEAD");
            when(request.getRequestURI()).thenReturn("/booking/new");

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService, never()).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should apply rate limiting to POST requests")
        void testPreHandle_ShouldRateLimitPost() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/booking");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should apply rate limiting to PUT requests")
        void testPreHandle_ShouldRateLimitPut() throws Exception {
            when(request.getMethod()).thenReturn("PUT");
            when(request.getRequestURI()).thenReturn("/api/booking/1");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should apply rate limiting to PATCH requests")
        void testPreHandle_ShouldRateLimitPatch() throws Exception {
            when(request.getMethod()).thenReturn("PATCH");
            when(request.getRequestURI()).thenReturn("/api/booking/1");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should apply rate limiting to DELETE requests")
        void testPreHandle_ShouldRateLimitDelete() throws Exception {
            when(request.getMethod()).thenReturn("DELETE");
            when(request.getRequestURI()).thenReturn("/api/booking/1");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }
    }

    @Nested
    @DisplayName("preHandle() Tests - Request Paths")
    class RequestPathTests {

        @Test
        @DisplayName("Should apply booking rate limiting for /booking/ paths")
        void testPreHandle_ShouldRateLimitBookingPath() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/booking/new");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
            verify(generalRateLimitingService, never()).isChatAllowed(any(), any());
            verify(generalRateLimitingService, never()).isReviewAllowed(any(), any());
        }

        @Test
        @DisplayName("Should apply booking rate limiting for /api/booking/ paths")
        void testPreHandle_ShouldRateLimitApiBookingPath() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/booking/create");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should apply chat rate limiting for /api/chat/ paths")
        void testPreHandle_ShouldRateLimitChatPath() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/chat/send");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isChatAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isChatAllowed(any(), any());
            verify(generalRateLimitingService, never()).isBookingAllowed(any(), any());
            verify(generalRateLimitingService, never()).isReviewAllowed(any(), any());
        }

        @Test
        @DisplayName("Should apply review rate limiting for /reviews/ paths")
        void testPreHandle_ShouldRateLimitReviewPath() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/reviews/create");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isReviewAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isReviewAllowed(any(), any());
            verify(generalRateLimitingService, never()).isBookingAllowed(any(), any());
            verify(generalRateLimitingService, never()).isChatAllowed(any(), any());
        }

        @Test
        @DisplayName("Should allow requests for other paths")
        void testPreHandle_ShouldAllowOtherPaths() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/other/path");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService, never()).isBookingAllowed(any(), any());
            verify(generalRateLimitingService, never()).isChatAllowed(any(), any());
            verify(generalRateLimitingService, never()).isReviewAllowed(any(), any());
        }
    }

    @Nested
    @DisplayName("preHandle() Tests - Rate Limit Exceeded")
    class RateLimitExceededTests {

        @Test
        @DisplayName("Should return 429 when booking rate limit exceeded")
        void testPreHandle_ShouldReturn429ForBookingExceeded() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/booking/create");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(false);

            boolean result = interceptor.preHandle(request, response, handler);

            assertFalse(result);
            verify(response).setStatus(429);
            verify(response).setContentType("application/json");
            verify(response).getWriter();
            assertTrue(responseWriter.toString().contains("Rate limit exceeded"));
        }

        @Test
        @DisplayName("Should return 429 when chat rate limit exceeded")
        void testPreHandle_ShouldReturn429ForChatExceeded() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/chat/send");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isChatAllowed(any(), any())).thenReturn(false);

            boolean result = interceptor.preHandle(request, response, handler);

            assertFalse(result);
            verify(response).setStatus(429);
            verify(response).setContentType("application/json");
        }

        @Test
        @DisplayName("Should return 429 when review rate limit exceeded")
        void testPreHandle_ShouldReturn429ForReviewExceeded() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/reviews/create");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isReviewAllowed(any(), any())).thenReturn(false);

            boolean result = interceptor.preHandle(request, response, handler);

            assertFalse(result);
            verify(response).setStatus(429);
            verify(response).setContentType("application/json");
        }
    }

    @Nested
    @DisplayName("IP Address Extraction Tests")
    class IpAddressExtractionTests {

        @Test
        @DisplayName("Should get IP from X-Forwarded-For header")
        void testPreHandle_ShouldGetIpFromXForwardedFor() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/booking");
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            interceptor.preHandle(request, response, handler);

            verify(request).getHeader("X-Forwarded-For");
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should get IP from X-Real-IP header when X-Forwarded-For is not available")
        void testPreHandle_ShouldGetIpFromXRealIp() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/booking");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.2");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            interceptor.preHandle(request, response, handler);

            verify(request).getHeader("X-Forwarded-For");
            verify(request).getHeader("X-Real-IP");
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should get IP from remote address when headers are not available")
        void testPreHandle_ShouldGetIpFromRemoteAddr() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/booking");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.3");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            interceptor.preHandle(request, response, handler);

            verify(request).getRemoteAddr();
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should handle multiple IPs in X-Forwarded-For header")
        void testPreHandle_ShouldHandleMultipleXForwardedForIps() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/booking");
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1, 172.16.0.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            interceptor.preHandle(request, response, handler);

            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle case-insensitive HTTP methods")
        void testPreHandle_ShouldHandleCaseInsensitiveMethods() throws Exception {
            when(request.getMethod()).thenReturn("post");
            when(request.getRequestURI()).thenReturn("/api/booking");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            verify(generalRateLimitingService).isBookingAllowed(any(), any());
        }

        @Test
        @DisplayName("Should log request information")
        void testPreHandle_ShouldLogRequestInfo() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/booking");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(generalRateLimitingService.isBookingAllowed(any(), any())).thenReturn(true);

            interceptor.preHandle(request, response, handler);

            verify(request, atLeastOnce()).getMethod();
            verify(request, atLeastOnce()).getRequestURI();
        }
    }
}

