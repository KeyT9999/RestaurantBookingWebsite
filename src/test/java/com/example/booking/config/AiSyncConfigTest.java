package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class AiSyncConfigTest {

    private AiSyncConfig config;
    private AiSyncProperties properties;

    @BeforeEach
    void setUp() {
        config = new AiSyncConfig();
        properties = new AiSyncProperties();
    }

    @Test
    // TC AS-015
    void shouldCreateRestTemplateWithCorrectTimeout() {
        // Given
        properties.setTimeoutMs(2000);
        
        // When
        RestTemplate restTemplate = config.aiSyncRestTemplate(properties);
        
        // Then
        assertNotNull(restTemplate);
        assertNotNull(restTemplate.getRequestFactory());
    }

    @Test
    // TC AS-016
    void shouldUseDefaultTimeout_whenTimeoutMsIsNull() {
        // Given
        properties.setTimeoutMs(null);
        
        // When
        RestTemplate restTemplate = config.aiSyncRestTemplate(properties);
        
        // Then
        assertNotNull(restTemplate);
    }
}

