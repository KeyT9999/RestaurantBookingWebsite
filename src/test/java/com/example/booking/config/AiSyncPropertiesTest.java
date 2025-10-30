package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.booking.config.AiSyncProperties;
import com.example.booking.config.AiSyncProperties.RetryProperties;

class AiSyncPropertiesTest {

    private AiSyncProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AiSyncProperties();
    }

    @Test
    // TC AS-017
    void shouldGetAndSetProperties() {
        // Given & When
        properties.setEnabled(true);
        properties.setUrl("http://localhost:8080/sync");
        properties.setSecret("test-secret");
        properties.setApiKey("test-api-key");
        properties.setTimeoutMs(2000);
        
        // Then
        assertEquals(true, properties.isEnabled());
        assertEquals("http://localhost:8080/sync", properties.getUrl());
        assertEquals("test-secret", properties.getSecret());
        assertEquals("test-api-key", properties.getApiKey());
        assertEquals(Integer.valueOf(2000), properties.getTimeoutMs());
    }

    @Test
    // TC AS-018
    void shouldGetAndSetRetryProperties() {
        // Given & When
        RetryProperties retryProps = new RetryProperties();
        retryProps.setMaxAttempts(5);
        retryProps.setBackoffMs(10000);
        properties.setRetry(retryProps);
        
        // Then
        assertNotNull(properties.getRetry());
        assertEquals(5, properties.getRetry().getMaxAttempts());
        assertEquals(10000, properties.getRetry().getBackoffMs());
    }
}

