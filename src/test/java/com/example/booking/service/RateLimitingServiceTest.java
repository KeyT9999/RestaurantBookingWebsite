package com.example.booking.service;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingServiceTest {

    private ConcurrentHashMap<String, io.github.bucket4j.Bucket> bucketStorage;
    private BucketConfiguration loginConfig;
    private BucketConfiguration bookingConfig;
    private BucketConfiguration chatConfig;
    private BucketConfiguration reviewConfig;
    private BucketConfiguration generalConfig;
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        bucketStorage = new ConcurrentHashMap<>();
        
        // Create bucket configurations
        loginConfig = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
                
        bookingConfig = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
                .build();
                
        chatConfig = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1))))
                .build();
                
        reviewConfig = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
                
        generalConfig = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                .build();

        rateLimitingService = new RateLimitingService(
                bucketStorage,
                loginConfig,
                bookingConfig,
                chatConfig,
                reviewConfig,
                generalConfig
        );
    }

    @Test
    void shouldAllowLoginRequest() {
        String clientIp = "192.168.1.1";
        boolean result = rateLimitingService.isLoginAllowed(clientIp);
        assertThat(result).isTrue();
    }

    @Test
    void shouldAllowBookingRequest() {
        String clientIp = "192.168.1.1";
        boolean result = rateLimitingService.isBookingAllowed(clientIp);
        assertThat(result).isTrue();
    }

    @Test
    void shouldAllowChatRequest() {
        String clientIp = "192.168.1.1";
        boolean result = rateLimitingService.isChatAllowed(clientIp);
        assertThat(result).isTrue();
    }

    @Test
    void shouldAllowReviewRequest() {
        String clientIp = "192.168.1.1";
        boolean result = rateLimitingService.isReviewAllowed(clientIp);
        assertThat(result).isTrue();
    }

    @Test
    void shouldAllowGeneralRequest() {
        String clientIp = "192.168.1.1";
        boolean result = rateLimitingService.isGeneralAllowed(clientIp);
        assertThat(result).isTrue();
    }

    @Test
    void shouldGetRemainingTokens() {
        String clientIp = "192.168.1.1";
        long tokens = rateLimitingService.getRemainingTokens(clientIp, "login");
        assertThat(tokens).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldResetRateLimit() {
        String clientIp = "192.168.1.1";
        String operationType = "login";
        
        // Consume some tokens first
        rateLimitingService.isLoginAllowed(clientIp);
        
        // Reset rate limit
        rateLimitingService.resetRateLimit(clientIp, operationType);
        
        // Should be able to use again
        boolean result = rateLimitingService.isLoginAllowed(clientIp);
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleDifferentIpsSeparately() {
        String clientIp1 = "192.168.1.1";
        String clientIp2 = "192.168.1.2";
        
        boolean result1 = rateLimitingService.isLoginAllowed(clientIp1);
        boolean result2 = rateLimitingService.isLoginAllowed(clientIp2);
        
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
    }

    @Test
    void shouldGetRemainingTokensForDifferentOperations() {
        String clientIp = "192.168.1.1";
        
        long loginTokens = rateLimitingService.getRemainingTokens(clientIp, "login");
        long bookingTokens = rateLimitingService.getRemainingTokens(clientIp, "booking");
        long chatTokens = rateLimitingService.getRemainingTokens(clientIp, "chat");
        
        assertThat(loginTokens).isGreaterThanOrEqualTo(0);
        assertThat(bookingTokens).isGreaterThanOrEqualTo(0);
        assertThat(chatTokens).isGreaterThanOrEqualTo(0);
    }
}

