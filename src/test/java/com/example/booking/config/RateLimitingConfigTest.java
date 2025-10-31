package com.example.booking.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.booking.config.RateLimitingConfig;
import com.example.booking.config.RateLimitingInterceptor;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Bandwidth;

class RateLimitingConfigTest {

    private RateLimitingInterceptor interceptor = Mockito.mock(RateLimitingInterceptor.class);
    private RateLimitingConfig config = new RateLimitingConfig(interceptor);

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(config, "loginRequests", 5);
        ReflectionTestUtils.setField(config, "loginWindowSeconds", 120);
        ReflectionTestUtils.setField(config, "bookingRequests", 8);
        ReflectionTestUtils.setField(config, "bookingWindowSeconds", 60);
        ReflectionTestUtils.setField(config, "chatRequests", 20);
        ReflectionTestUtils.setField(config, "chatWindowSeconds", 30);
        ReflectionTestUtils.setField(config, "reviewRequests", 3);
        ReflectionTestUtils.setField(config, "reviewWindowSeconds", 300);
        ReflectionTestUtils.setField(config, "generalRequests", 100);
        ReflectionTestUtils.setField(config, "generalWindowSeconds", 60);
    }

    @Test
    @DisplayName("cacheManager should configure caffeine cache")
    void shouldProvideCaffeineCacheManager() {
        assertThat(config.cacheManager()).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    @DisplayName("bucketStorage should return concurrent hash map")
    void shouldProvideBucketStorage() {
        assertThat(config.bucketStorage()).isInstanceOf(ConcurrentHashMap.class);
    }

    @Test
    @DisplayName("bucket configurations should honour configured capacities")
    void shouldConfigureBucketCapacities() {
        assertCapacity(config.loginBucketConfiguration(), 5);
        assertCapacity(config.bookingBucketConfiguration(), 8);
        assertCapacity(config.chatBucketConfiguration(), 20);
        assertCapacity(config.reviewBucketConfiguration(), 3);
        assertCapacity(config.generalBucketConfiguration(), 100);
    }

    private void assertCapacity(BucketConfiguration configuration, long expectedCapacity) {
        Bandwidth bandwidth = configuration.getBandwidths()[0];
        assertThat(bandwidth.getCapacity()).isEqualTo(expectedCapacity);
    }
}
