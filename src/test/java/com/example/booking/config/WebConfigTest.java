package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * Unit tests for WebConfig
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebConfig Tests")
public class WebConfigTest {

    @InjectMocks
    private WebConfig webConfig;

    private ResourceHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(webConfig, "uploadDir", "uploads");
    }

    // ========== addResourceHandlers() Tests ==========

    @Test
    @DisplayName("shouldAddResourceHandlers_successfully")
    void shouldAddResourceHandlers_successfully() {
        // Given
        registry = new ResourceHandlerRegistry(null, null);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            webConfig.addResourceHandlers(registry);
        });
    }

    // ========== forwardedHeaderFilter() Tests ==========

    @Test
    @DisplayName("shouldCreateForwardedHeaderFilter_successfully")
    void shouldCreateForwardedHeaderFilter_successfully() {
        // When
        org.springframework.web.filter.ForwardedHeaderFilter filter = webConfig.forwardedHeaderFilter();

        // Then
        assertNotNull(filter);
    }
}

