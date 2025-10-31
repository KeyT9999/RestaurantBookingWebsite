package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;

/**
 * Unit tests for RateLimitingService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingService Tests")
public class RateLimitingServiceTest {

    @Mock
    private ConcurrentHashMap<String, Bucket> bucketStorage;

    @Mock
    private BucketConfiguration loginBucketConfiguration;

    @Mock
    private BucketConfiguration bookingBucketConfiguration;

    @Mock
    private BucketConfiguration chatBucketConfiguration;

    @Mock
    private BucketConfiguration reviewBucketConfiguration;

    @Mock
    private BucketConfiguration generalBucketConfiguration;

    @Mock
    private Bucket mockBucket;

    @InjectMocks
    private RateLimitingService rateLimitingService;

    private String clientIp;

    @BeforeEach
    void setUp() {
        clientIp = "192.168.1.1";
        
        // Setup bucket storage
        when(bucketStorage.computeIfAbsent(anyString(), any())).thenReturn(mockBucket);
        when(mockBucket.tryConsume(1)).thenReturn(true);
        when(mockBucket.getAvailableTokens()).thenReturn(10L);
        
        // Setup bucket configurations - removed as getBandwidths() returns Bandwidth[] and causes type issues
        // These configurations are typically tested via integration tests or by testing the actual bucket behavior
    }

    // ========== isLoginAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowLogin_whenBucketAvailable")
    void shouldAllowLogin_whenBucketAvailable() {
        // When
        boolean result = rateLimitingService.isLoginAllowed(clientIp);

        // Then
        assertTrue(result);
        verify(mockBucket, times(1)).tryConsume(1);
    }

    @Test
    @DisplayName("shouldBlockLogin_whenBucketDepleted")
    void shouldBlockLogin_whenBucketDepleted() {
        // Given
        when(mockBucket.tryConsume(1)).thenReturn(false);

        // When
        boolean result = rateLimitingService.isLoginAllowed(clientIp);

        // Then
        assertFalse(result);
    }

    // ========== isBookingAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowBooking_whenBucketAvailable")
    void shouldAllowBooking_whenBucketAvailable() {
        // When
        boolean result = rateLimitingService.isBookingAllowed(clientIp);

        // Then
        assertTrue(result);
    }

    // ========== isChatAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowChat_whenBucketAvailable")
    void shouldAllowChat_whenBucketAvailable() {
        // When
        boolean result = rateLimitingService.isChatAllowed(clientIp);

        // Then
        assertTrue(result);
    }

    // ========== isReviewAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowReview_whenBucketAvailable")
    void shouldAllowReview_whenBucketAvailable() {
        // When
        boolean result = rateLimitingService.isReviewAllowed(clientIp);

        // Then
        assertTrue(result);
    }

    // ========== isGeneralAllowed() Tests ==========

    @Test
    @DisplayName("shouldAllowGeneral_whenBucketAvailable")
    void shouldAllowGeneral_whenBucketAvailable() {
        // When
        boolean result = rateLimitingService.isGeneralAllowed(clientIp);

        // Then
        assertTrue(result);
    }

    // ========== getRemainingTokens() Tests ==========

    @Test
    @DisplayName("shouldGetRemainingTokens_successfully")
    void shouldGetRemainingTokens_successfully() {
        // When
        long tokens = rateLimitingService.getRemainingTokens(clientIp, "login");

        // Then
        assertEquals(10L, tokens);
    }

    // ========== resetRateLimit() Tests ==========

    @Test
    @DisplayName("shouldResetRateLimit_successfully")
    void shouldResetRateLimit_successfully() {
        // When
        rateLimitingService.resetRateLimit(clientIp, "login");

        // Then
        verify(bucketStorage, times(1)).remove("login:192.168.1.1");
    }
}

