package com.example.booking.config;

import com.example.booking.service.DatabaseRateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermanentlyBlockedIpFilter Test Suite")
class PermanentlyBlockedIpFilterTest {

    @Mock
    private DatabaseRateLimitingService databaseService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private PermanentlyBlockedIpFilter filter;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Nested
    @DisplayName("doFilter() Tests - Blocked IP")
    class BlockedIpTests {

        @Test
        @DisplayName("Should block permanently blocked IP")
        void testDoFilter_ShouldBlockPermanentlyBlockedIp() throws Exception {
            String blockedIp = "192.168.1.100";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(blockedIp);
            when(databaseService.isIpPermanentlyBlocked(blockedIp)).thenReturn(true);

            filter.doFilter(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(response).setContentType("application/json");
            verify(response).getWriter();
            assertTrue(responseWriter.toString().contains("permanently blocked"));
            assertTrue(responseWriter.toString().contains("PERMANENTLY_BLOCKED"));
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Should allow non-blocked IP")
        void testDoFilter_ShouldAllowNonBlockedIp() throws Exception {
            String allowedIp = "192.168.1.200";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(allowedIp);
            when(databaseService.isIpPermanentlyBlocked(allowedIp)).thenReturn(false);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("doFilter() Tests - Skip Paths")
    class SkipPathTests {

        @Test
        @DisplayName("Should skip /css/ paths")
        void testDoFilter_ShouldSkipCssPath() throws Exception {
            when(request.getRequestURI()).thenReturn("/css/style.css");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(databaseService, never()).isIpPermanentlyBlocked(anyString());
        }

        @Test
        @DisplayName("Should skip /js/ paths")
        void testDoFilter_ShouldSkipJsPath() throws Exception {
            when(request.getRequestURI()).thenReturn("/js/script.js");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(databaseService, never()).isIpPermanentlyBlocked(anyString());
        }

        @Test
        @DisplayName("Should skip /images/ paths")
        void testDoFilter_ShouldSkipImagesPath() throws Exception {
            when(request.getRequestURI()).thenReturn("/images/logo.png");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(databaseService, never()).isIpPermanentlyBlocked(anyString());
        }

        @Test
        @DisplayName("Should skip /uploads/ paths")
        void testDoFilter_ShouldSkipUploadsPath() throws Exception {
            when(request.getRequestURI()).thenReturn("/uploads/file.pdf");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(databaseService, never()).isIpPermanentlyBlocked(anyString());
        }

        @Test
        @DisplayName("Should skip /actuator/ paths")
        void testDoFilter_ShouldSkipActuatorPath() throws Exception {
            when(request.getRequestURI()).thenReturn("/actuator/health");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(databaseService, never()).isIpPermanentlyBlocked(anyString());
        }

        @Test
        @DisplayName("Should skip /favicon.ico")
        void testDoFilter_ShouldSkipFavicon() throws Exception {
            when(request.getRequestURI()).thenReturn("/favicon.ico");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(databaseService, never()).isIpPermanentlyBlocked(anyString());
        }
    }

    @Nested
    @DisplayName("IP Address Extraction Tests")
    class IpAddressExtractionTests {

        @Test
        @DisplayName("Should get IP from X-Forwarded-For header")
        void testDoFilter_ShouldGetIpFromXForwardedFor() throws Exception {
            String ip = "192.168.1.1";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(ip);
            when(databaseService.isIpPermanentlyBlocked(ip)).thenReturn(false);

            filter.doFilter(request, response, filterChain);

            verify(databaseService).isIpPermanentlyBlocked(ip);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should get IP from X-Real-IP header when X-Forwarded-For is not available")
        void testDoFilter_ShouldGetIpFromXRealIp() throws Exception {
            String ip = "192.168.1.2";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(ip);
            when(databaseService.isIpPermanentlyBlocked(ip)).thenReturn(false);

            filter.doFilter(request, response, filterChain);

            verify(databaseService).isIpPermanentlyBlocked(ip);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should get IP from remote address when headers are not available")
        void testDoFilter_ShouldGetIpFromRemoteAddr() throws Exception {
            String ip = "192.168.1.3";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(ip);
            when(databaseService.isIpPermanentlyBlocked(ip)).thenReturn(false);

            filter.doFilter(request, response, filterChain);

            verify(databaseService).isIpPermanentlyBlocked(ip);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle multiple IPs in X-Forwarded-For header")
        void testDoFilter_ShouldHandleMultipleXForwardedForIps() throws Exception {
            String firstIp = "192.168.1.1";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1, 172.16.0.1");
            when(databaseService.isIpPermanentlyBlocked(firstIp)).thenReturn(false);

            filter.doFilter(request, response, filterChain);

            verify(databaseService).isIpPermanentlyBlocked(firstIp);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should handle IOException from getWriter")
        void testDoFilter_ShouldHandleIOException() throws Exception {
            String blockedIp = "192.168.1.100";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(blockedIp);
            when(databaseService.isIpPermanentlyBlocked(blockedIp)).thenReturn(true);
            when(response.getWriter()).thenThrow(new IOException("Writer error"));

            assertThrows(IOException.class, () -> {
                filter.doFilter(request, response, filterChain);
            });

            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Should handle ServletException from filterChain")
        void testDoFilter_ShouldHandleServletException() throws Exception {
            String allowedIp = "192.168.1.200";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(allowedIp);
            when(databaseService.isIpPermanentlyBlocked(allowedIp)).thenReturn(false);
            doThrow(new ServletException("Filter chain error")).when(filterChain).doFilter(any(), any());

            assertThrows(ServletException.class, () -> {
                filter.doFilter(request, response, filterChain);
            });
        }
    }

    @Nested
    @DisplayName("Response Format Tests")
    class ResponseFormatTests {

        @Test
        @DisplayName("Should return JSON error response")
        void testDoFilter_ShouldReturnJsonErrorResponse() throws Exception {
            String blockedIp = "192.168.1.100";
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(blockedIp);
            when(databaseService.isIpPermanentlyBlocked(blockedIp)).thenReturn(true);

            filter.doFilter(request, response, filterChain);

            String responseBody = responseWriter.toString();
            assertTrue(responseBody.contains("\"error\""));
            assertTrue(responseBody.contains("\"code\""));
            assertTrue(responseBody.contains("PERMANENTLY_BLOCKED"));
        }
    }
}

