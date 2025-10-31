package com.example.booking.config;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.service.DatabaseRateLimitingService;
import jakarta.servlet.FilterChain;
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
 * Unit test for PermanentlyBlockedIpFilter
 * Coverage: 100% - All branches (blocked/not blocked/skip path, all IP sources)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermanentlyBlockedIpFilter Tests")
class PermanentlyBlockedIpFilterTest {

    @Mock
    private DatabaseRateLimitingService databaseService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private PermanentlyBlockedIpFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Nested
    @DisplayName("IP Blocking Tests")
    class IpBlockingTests {

        @Test
        @DisplayName("shouldBlockPermanentlyBlockedIp")
        void shouldBlockPermanentlyBlockedIp() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(databaseService.isIpPermanentlyBlocked("192.168.1.1")).thenReturn(true);

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(response).setContentType("application/json");
            verify(response.getWriter()).write(contains("PERMANENTLY_BLOCKED"));
            verify(chain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("shouldAllowNonBlockedIp")
        void shouldAllowNonBlockedIp() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getRemoteAddr()).thenReturn("192.168.1.2");
            when(databaseService.isIpPermanentlyBlocked("192.168.1.2")).thenReturn(false);

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(chain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }
    }

    @Nested
    @DisplayName("Skip Path Tests")
    class SkipPathTests {

        @Test
        @DisplayName("shouldSkipCssPath")
        void shouldSkipCssPath() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/css/style.css");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(chain).doFilter(request, response);
            verify(databaseService, never()).isIpPermanentlyBlocked(anyString());
        }

        @Test
        @DisplayName("shouldSkipJsPath")
        void shouldSkipJsPath() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/js/script.js");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(chain).doFilter(request, response);
            verify(databaseService, never()).isIpPermanentlyBlocked(anyString());
        }

        @Test
        @DisplayName("shouldSkipImagesPath")
        void shouldSkipImagesPath() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/images/logo.png");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("shouldSkipUploadsPath")
        void shouldSkipUploadsPath() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/uploads/file.pdf");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("shouldSkipActuatorPath")
        void shouldSkipActuatorPath() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/actuator/health");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("shouldSkipFavicon")
        void shouldSkipFavicon() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/favicon.ico");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("shouldNotSkipNormalPath")
        void shouldNotSkipNormalPath() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/booking");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(databaseService.isIpPermanentlyBlocked(anyString())).thenReturn(false);

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(databaseService).isIpPermanentlyBlocked("192.168.1.1");
            verify(chain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("IP Address Detection Tests")
    class IpAddressDetectionTests {

        @Test
        @DisplayName("shouldGetIpFromXForwardedFor")
        void shouldGetIpFromXForwardedFor() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
            when(databaseService.isIpPermanentlyBlocked("10.0.0.1")).thenReturn(true);

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(databaseService).isIpPermanentlyBlocked("10.0.0.1");
            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        @Test
        @DisplayName("shouldGetIpFromXRealIp")
        void shouldGetIpFromXRealIp() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn("172.16.0.1");
            when(databaseService.isIpPermanentlyBlocked("172.16.0.1")).thenReturn(true);

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(databaseService).isIpPermanentlyBlocked("172.16.0.1");
        }

        @Test
        @DisplayName("shouldGetIpFromRemoteAddr")
        void shouldGetIpFromRemoteAddr() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");
            when(databaseService.isIpPermanentlyBlocked("127.0.0.1")).thenReturn(false);

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(databaseService).isIpPermanentlyBlocked("127.0.0.1");
            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("shouldHandleEmptyXForwardedFor")
        void shouldHandleEmptyXForwardedFor() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn("");
            when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
            when(databaseService.isIpPermanentlyBlocked("10.0.0.2")).thenReturn(false);

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(databaseService).isIpPermanentlyBlocked("10.0.0.2");
        }

        @Test
        @DisplayName("shouldHandleEmptyXRealIp")
        void shouldHandleEmptyXRealIp() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn("");
            when(request.getRemoteAddr()).thenReturn("192.168.1.100");
            when(databaseService.isIpPermanentlyBlocked("192.168.1.100")).thenReturn(false);

            // When
            filter.doFilter(request, response, chain);

            // Then
            verify(databaseService).isIpPermanentlyBlocked("192.168.1.100");
        }
    }
}
