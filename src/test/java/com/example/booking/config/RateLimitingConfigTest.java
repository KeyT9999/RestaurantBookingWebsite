package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.booking.config.RateLimitingConfig;
import com.example.booking.config.RateLimitingInterceptor;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;

/**
 * Unit tests for RateLimitingConfig
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingConfig Tests")
public class RateLimitingConfigTest {

    @Mock
    private RateLimitingInterceptor rateLimitingInterceptor;

    @InjectMocks
    private RateLimitingConfig rateLimitingConfig;

    // ========== cacheManager() Tests ==========

    @Test
    @DisplayName("shouldCreateCacheManager_successfully")
    void shouldCreateCacheManager_successfully() {
        // When
        CacheManager cacheManager = rateLimitingConfig.cacheManager();

        // Then
        assertNotNull(cacheManager);
    }

    // ========== bucketStorage() Tests ==========

    @Test
    @DisplayName("shouldCreateBucketStorage_successfully")
    void shouldCreateBucketStorage_successfully() {
        // When
        ConcurrentHashMap<String, Bucket> bucketStorage = rateLimitingConfig.bucketStorage();

        // Then
        assertNotNull(bucketStorage);
        assertTrue(bucketStorage.isEmpty());
    }

    // ========== loginBucketConfiguration() Tests ==========

    @Test
    @DisplayName("shouldCreateLoginBucketConfiguration_successfully")
    void shouldCreateLoginBucketConfiguration_successfully() {
        // Given
        ReflectionTestUtils.setField(rateLimitingConfig, "loginRequests", 5);
        ReflectionTestUtils.setField(rateLimitingConfig, "loginWindowSeconds", 300);

        // When
        BucketConfiguration config = rateLimitingConfig.loginBucketConfiguration();

        // Then
        assertNotNull(config);
    }

    // ========== bookingBucketConfiguration() Tests ==========

    @Test
    @DisplayName("shouldCreateBookingBucketConfiguration_successfully")
    void shouldCreateBookingBucketConfiguration_successfully() {
        // Given
        ReflectionTestUtils.setField(rateLimitingConfig, "bookingRequests", 10);
        ReflectionTestUtils.setField(rateLimitingConfig, "bookingWindowSeconds", 60);

        // When
        BucketConfiguration config = rateLimitingConfig.bookingBucketConfiguration();

        // Then
        assertNotNull(config);
    }

    // ========== chatBucketConfiguration() Tests ==========

    @Test
    @DisplayName("shouldCreateChatBucketConfiguration_successfully")
    void shouldCreateChatBucketConfiguration_successfully() {
        // Given
        ReflectionTestUtils.setField(rateLimitingConfig, "chatRequests", 30);
        ReflectionTestUtils.setField(rateLimitingConfig, "chatWindowSeconds", 60);

        // When
        BucketConfiguration config = rateLimitingConfig.chatBucketConfiguration();

        // Then
        assertNotNull(config);
    }

    // ========== reviewBucketConfiguration() Tests ==========

    @Test
    @DisplayName("shouldCreateReviewBucketConfiguration_successfully")
    void shouldCreateReviewBucketConfiguration_successfully() {
        // Given
        ReflectionTestUtils.setField(rateLimitingConfig, "reviewRequests", 3);
        ReflectionTestUtils.setField(rateLimitingConfig, "reviewWindowSeconds", 300);

        // When
        BucketConfiguration config = rateLimitingConfig.reviewBucketConfiguration();

        // Then
        assertNotNull(config);
    }

    // ========== generalBucketConfiguration() Tests ==========

    @Test
    @DisplayName("shouldCreateGeneralBucketConfiguration_successfully")
    void shouldCreateGeneralBucketConfiguration_successfully() {
        // Given
        ReflectionTestUtils.setField(rateLimitingConfig, "generalRequests", 100);
        ReflectionTestUtils.setField(rateLimitingConfig, "generalWindowSeconds", 60);

        // When
        BucketConfiguration config = rateLimitingConfig.generalBucketConfiguration();

        // Then
        assertNotNull(config);
    }
}

