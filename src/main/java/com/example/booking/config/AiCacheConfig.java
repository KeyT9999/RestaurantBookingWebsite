package com.example.booking.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Cache configuration for AI services (English + Vietnamese)
 * Cấu hình bộ nhớ đệm cho các dịch vụ AI (ý định & giải thích).
 */
@Configuration
public class AiCacheConfig {

    @Bean("aiCacheManager")
    public CacheManager aiCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .maximumSize(10_000));
        cacheManager.setCacheNames(List.of("aiIntent", "aiExplain"));
        return cacheManager;
    }
}
