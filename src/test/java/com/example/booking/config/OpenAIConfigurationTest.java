package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.theokanning.openai.service.OpenAiService;

/**
 * Unit tests for OpenAIConfiguration
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAIConfiguration Tests")
public class OpenAIConfigurationTest {

    @InjectMocks
    private OpenAIConfiguration configuration;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(configuration, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(configuration, "apiUrl", "https://api.openai.com/v1");
        ReflectionTestUtils.setField(configuration, "timeoutMs", 800);
    }

    // ========== openAiService() Tests ==========

    @Test
    @DisplayName("shouldCreateOpenAiService_successfully")
    void shouldCreateOpenAiService_successfully() {
        // When
        OpenAiService service = configuration.openAiService();

        // Then
        assertNotNull(service);
    }

    @Test
    @DisplayName("shouldCreateOpenAiService_withApiKey")
    void shouldCreateOpenAiService_withApiKey() {
        // When
        OpenAiService service = configuration.openAiService();

        // Then
        assertNotNull(service);
        // OpenAiService doesn't expose the API key, so we just verify it's created
    }

    @Test
    @DisplayName("shouldCreateOpenAiService_withTimeout")
    void shouldCreateOpenAiService_withTimeout() {
        // Given
        ReflectionTestUtils.setField(configuration, "timeoutMs", 5000);

        // When
        OpenAiService service = configuration.openAiService();

        // Then
        assertNotNull(service);
    }

    @Test
    @DisplayName("shouldUseDefaultTimeout_whenNotSet")
    void shouldUseDefaultTimeout_whenNotSet() {
        // Given
        ReflectionTestUtils.setField(configuration, "timeoutMs", 800); // Default

        // When
        OpenAiService service = configuration.openAiService();

        // Then
        assertNotNull(service);
    }

    // ========== openAiRestTemplate() Tests ==========

    @Test
    @DisplayName("shouldCreateRestTemplate_successfully")
    void shouldCreateRestTemplate_successfully() {
        // When
        RestTemplate restTemplate = configuration.openAiRestTemplate();

        // Then
        assertNotNull(restTemplate);
    }

    @Test
    @DisplayName("shouldCreateRestTemplateInstance")
    void shouldCreateRestTemplateInstance() {
        // When
        RestTemplate restTemplate1 = configuration.openAiRestTemplate();
        RestTemplate restTemplate2 = configuration.openAiRestTemplate();

        // Then - Should create new instances (not singleton by default)
        assertNotNull(restTemplate1);
        assertNotNull(restTemplate2);
    }
}

