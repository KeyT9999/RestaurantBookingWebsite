package com.example.booking.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.booking.annotation.RateLimited;
import com.example.booking.annotation.RateLimited.OperationType;
import com.example.booking.service.RateLimitingService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for RateLimitingAspect
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingAspect Tests")
public class RateLimitingAspectTest {

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @InjectMocks
    private RateLimitingAspect rateLimitingAspect;

    private RateLimited rateLimited;

    @BeforeEach
    void setUp() {
        rateLimited = mock(RateLimited.class);
        when(rateLimited.value()).thenReturn(OperationType.LOGIN);
        when(rateLimited.message()).thenReturn("Rate limit exceeded");
    }

    // ========== handleRateLimit() Tests ==========

    @Test
    @DisplayName("shouldAllowRequest_whenRateLimitOK")
    void shouldAllowRequest_whenRateLimitOK() throws Throwable {
        // Given
        when(rateLimitingService.isLoginAllowed("192.168.1.1")).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("shouldBlockRequest_whenRateLimitExceeded")
    void shouldBlockRequest_whenRateLimitExceeded() throws Throwable {
        // Given
        when(rateLimitingService.isLoginAllowed("192.168.1.1")).thenReturn(false);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof ResponseEntity);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(joinPoint, never()).proceed();
    }
    
    @Test
    @DisplayName("shouldHandleBookingRateLimit")
    void shouldHandleBookingRateLimit() throws Throwable {
        // Given
        when(rateLimited.value()).thenReturn(OperationType.BOOKING);
        when(rateLimitingService.isBookingAllowed("192.168.1.1")).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(rateLimitingService, times(1)).isBookingAllowed("192.168.1.1");
    }

    @Test
    @DisplayName("shouldProceed_whenRequestIsNull")
    void shouldProceed_whenRequestIsNull() throws Throwable {
        // Given
        RequestContextHolder.setRequestAttributes(null);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("shouldUseXForwardedFor_whenPresent")
    void shouldUseXForwardedFor_whenPresent() throws Throwable {
        // Given
        when(rateLimited.value()).thenReturn(OperationType.GENERAL);
        when(rateLimitingService.isGeneralAllowed("10.0.0.1")).thenReturn(true);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(rateLimitingService, times(1)).isGeneralAllowed("10.0.0.1");
    }

    @Test
    @DisplayName("shouldHandleChatRateLimit")
    void shouldHandleChatRateLimit() throws Throwable {
        // Given
        when(rateLimited.value()).thenReturn(OperationType.CHAT);
        when(rateLimitingService.isChatAllowed("192.168.1.1")).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(rateLimitingService, times(1)).isChatAllowed("192.168.1.1");
    }

    @Test
    @DisplayName("shouldHandleReviewRateLimit")
    void shouldHandleReviewRateLimit() throws Throwable {
        // Given
        when(rateLimited.value()).thenReturn(OperationType.REVIEW);
        when(rateLimitingService.isReviewAllowed("192.168.1.1")).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(rateLimitingService, times(1)).isReviewAllowed("192.168.1.1");
    }

    @Test
    @DisplayName("shouldUseXRealIp_whenXForwardedForNotPresent")
    void shouldUseXRealIp_whenXForwardedForNotPresent() throws Throwable {
        // Given
        when(rateLimited.value()).thenReturn(OperationType.GENERAL);
        when(rateLimitingService.isGeneralAllowed("192.168.1.100")).thenReturn(true);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.100");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(rateLimitingService, times(1)).isGeneralAllowed("192.168.1.100");
    }

    @Test
    @DisplayName("shouldUseFirstIp_whenXForwardedForHasMultipleIps")
    void shouldUseFirstIp_whenXForwardedForHasMultipleIps() throws Throwable {
        // Given
        when(rateLimited.value()).thenReturn(OperationType.GENERAL);
        when(rateLimitingService.isGeneralAllowed("10.0.0.1")).thenReturn(true);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1, 172.16.0.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(rateLimitingService, times(1)).isGeneralAllowed("10.0.0.1");
    }

    @Test
    @DisplayName("shouldUseRemoteAddr_whenNoHeadersPresent")
    void shouldUseRemoteAddr_whenNoHeadersPresent() throws Throwable {
        // Given
        when(rateLimited.value()).thenReturn(OperationType.GENERAL);
        when(rateLimitingService.isGeneralAllowed("127.0.0.1")).thenReturn(true);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertEquals("success", result);
        verify(rateLimitingService, times(1)).isGeneralAllowed("127.0.0.1");
    }

    @Test
    @DisplayName("shouldBlockChat_whenRateLimitExceeded")
    void shouldBlockChat_whenRateLimitExceeded() throws Throwable {
        // Given
        when(rateLimited.value()).thenReturn(OperationType.CHAT);
        when(rateLimited.message()).thenReturn("Chat rate limit exceeded");
        when(rateLimitingService.isChatAllowed("192.168.1.1")).thenReturn(false);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        RequestContextHolder.setRequestAttributes(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);

        // When
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof ResponseEntity);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(joinPoint, never()).proceed();
    }
}

