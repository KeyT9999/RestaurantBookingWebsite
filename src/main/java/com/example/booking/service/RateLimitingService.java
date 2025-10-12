package com.example.booking.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing rate limiting operations
 */
@Service
public class RateLimitingService {

    private final ConcurrentHashMap<String, Bucket> bucketStorage;
    private final BucketConfiguration loginBucketConfiguration;
    private final BucketConfiguration bookingBucketConfiguration;
    private final BucketConfiguration chatBucketConfiguration;
    private final BucketConfiguration reviewBucketConfiguration;
    private final BucketConfiguration generalBucketConfiguration;

    @Autowired
    public RateLimitingService(
            ConcurrentHashMap<String, Bucket> bucketStorage,
            @Qualifier("loginBucketConfiguration") BucketConfiguration loginBucketConfiguration,
            @Qualifier("bookingBucketConfiguration") BucketConfiguration bookingBucketConfiguration,
            @Qualifier("chatBucketConfiguration") BucketConfiguration chatBucketConfiguration,
            @Qualifier("reviewBucketConfiguration") BucketConfiguration reviewBucketConfiguration,
            @Qualifier("generalBucketConfiguration") BucketConfiguration generalBucketConfiguration) {
        this.bucketStorage = bucketStorage;
        this.loginBucketConfiguration = loginBucketConfiguration;
        this.bookingBucketConfiguration = bookingBucketConfiguration;
        this.chatBucketConfiguration = chatBucketConfiguration;
        this.reviewBucketConfiguration = reviewBucketConfiguration;
        this.generalBucketConfiguration = generalBucketConfiguration;
    }

    /**
     * Check if request is allowed for login operations
     */
    public boolean isLoginAllowed(String clientIp) {
        return isRequestAllowed(clientIp, "login", loginBucketConfiguration);
    }

    /**
     * Check if request is allowed for booking operations
     */
    public boolean isBookingAllowed(String clientIp) {
        return isRequestAllowed(clientIp, "booking", bookingBucketConfiguration);
    }

    /**
     * Check if request is allowed for chat operations
     */
    public boolean isChatAllowed(String clientIp) {
        return isRequestAllowed(clientIp, "chat", chatBucketConfiguration);
    }

    /**
     * Check if request is allowed for review operations
     */
    public boolean isReviewAllowed(String clientIp) {
        return isRequestAllowed(clientIp, "review", reviewBucketConfiguration);
    }

    /**
     * Check if request is allowed for general operations
     */
    public boolean isGeneralAllowed(String clientIp) {
        return isRequestAllowed(clientIp, "general", generalBucketConfiguration);
    }

    /**
     * Get remaining tokens for a specific operation
     */
    public long getRemainingTokens(String clientIp, String operationType) {
        BucketConfiguration config = getBucketConfiguration(operationType);
        String bucketKey = operationType + ":" + clientIp;
        
        Bucket bucket = bucketStorage.computeIfAbsent(bucketKey, key -> Bucket.builder()
                .addLimit(config.getBandwidths()[0])
                .build());
        
        return bucket.getAvailableTokens();
    }

    /**
     * Reset rate limit for a specific IP and operation
     */
    public void resetRateLimit(String clientIp, String operationType) {
        String bucketKey = operationType + ":" + clientIp;
        bucketStorage.remove(bucketKey);
    }

    /**
     * Check if request is allowed based on IP and configuration
     */
    private boolean isRequestAllowed(String clientIp, String operationType, BucketConfiguration config) {
        String bucketKey = operationType + ":" + clientIp;
        
        Bucket bucket = bucketStorage.computeIfAbsent(bucketKey, key -> Bucket.builder()
                .addLimit(config.getBandwidths()[0])
                .build());
        
        return bucket.tryConsume(1);
    }

    /**
     * Get bucket configuration for operation type
     */
    private BucketConfiguration getBucketConfiguration(String operationType) {
        switch (operationType.toLowerCase()) {
            case "login":
                return loginBucketConfiguration;
            case "booking":
                return bookingBucketConfiguration;
            case "chat":
                return chatBucketConfiguration;
            case "review":
                return reviewBucketConfiguration;
            default:
                return generalBucketConfiguration;
        }
    }
}