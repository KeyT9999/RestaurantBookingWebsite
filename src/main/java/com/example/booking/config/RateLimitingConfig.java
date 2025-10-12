package com.example.booking.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {
    
    @Value("${rate.limit.login.requests:5}")
    private int loginRequests;
    
    @Value("${rate.limit.login.window:300}")
    private int loginWindowSeconds;
    
    @Value("${rate.limit.booking.requests:10}")
    private int bookingRequests;
    
    @Value("${rate.limit.booking.window:60}")
    private int bookingWindowSeconds;
    
    @Value("${rate.limit.chat.requests:30}")
    private int chatRequests;
    
    @Value("${rate.limit.chat.window:60}")
    private int chatWindowSeconds;
    
    @Value("${rate.limit.review.requests:3}")
    private int reviewRequests;
    
    @Value("${rate.limit.review.window:300}")
    private int reviewWindowSeconds;
    
    @Value("${rate.limit.general.requests:100}")
    private int generalRequests;
    
    @Value("${rate.limit.general.window:60}")
    private int generalWindowSeconds;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100000)
                .expireAfterAccess(1, TimeUnit.HOURS));
        return cacheManager;
    }

    @Bean
    public ConcurrentHashMap<String, Bucket> bucketStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean("loginBucketConfiguration")
    public BucketConfiguration loginBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(loginRequests, 
                        Refill.intervally(loginRequests, 
                                Duration.ofSeconds(loginWindowSeconds))))
                .build();
    }

    @Bean("bookingBucketConfiguration")
    public BucketConfiguration bookingBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(bookingRequests, 
                        Refill.intervally(bookingRequests, 
                                Duration.ofSeconds(bookingWindowSeconds))))
                .build();
    }

    @Bean("chatBucketConfiguration")
    public BucketConfiguration chatBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(chatRequests, 
                        Refill.intervally(chatRequests, 
                                Duration.ofSeconds(chatWindowSeconds))))
                .build();
    }

    @Bean("reviewBucketConfiguration")
    public BucketConfiguration reviewBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(reviewRequests, 
                        Refill.intervally(reviewRequests, 
                                Duration.ofSeconds(reviewWindowSeconds))))
                .build();
    }

    @Bean("generalBucketConfiguration")
    public BucketConfiguration generalBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(generalRequests, 
                        Refill.intervally(generalRequests, 
                                Duration.ofSeconds(generalWindowSeconds))))
                .build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitingInterceptor())
                .addPathPatterns("/auth/**", "/booking/**", "/api/chat/**", "/reviews/**", "/api/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/uploads/**", "/actuator/**", "/login");
    }
}