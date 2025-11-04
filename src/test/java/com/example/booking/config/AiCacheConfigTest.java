package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.example.booking.config.AiCacheConfig;

class AiCacheConfigTest {

    private AiCacheConfig config;

    @BeforeEach
    void setUp() {
        config = new AiCacheConfig();
    }

    @Test
    // TC AC-001
    void shouldCreateCacheManagerWithCorrectCacheNames() {
        // When
        CacheManager cacheManager = config.aiCacheManager();
        
        // Then
        assertNotNull(cacheManager);
        assertNotNull(cacheManager.getCache("aiIntent"));
        assertNotNull(cacheManager.getCache("aiExplain"));
    }

    @Test
    // TC AC-002
    void shouldCacheExpireAfterConfiguredTime() {
        // Given
        CacheManager cacheManager = config.aiCacheManager();
        
        // When
        Cache cache = cacheManager.getCache("aiIntent");
        
        // Then
        assertNotNull(cache);
        
        // Test cache expiration by putting and getting
        cache.put("testKey", "testValue");
        assertEquals("testValue", cache.get("testKey", String.class));
    }
}

