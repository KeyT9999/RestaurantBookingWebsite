package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Unit tests for AuditConfig
 */
@SpringBootTest
@Import(AuditConfig.class)
@DisplayName("AuditConfig Tests")
public class AuditConfigTest {

    @Test
    @DisplayName("shouldLoadAuditConfig_successfully")
    void shouldLoadAuditConfig_successfully() {
        // Given
        AuditConfig config = new AuditConfig();

        // Then
        assertNotNull(config);
    }

    @Test
    @DisplayName("shouldBeConfigurationClass")
    void shouldBeConfigurationClass() {
        // Given
        AuditConfig config = new AuditConfig();

        // Then
        assertTrue(config.getClass().isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
    }

    @Test
    @DisplayName("shouldNotEnableAspectJAutoProxy")
    void shouldNotEnableAspectJAutoProxy() {
        // Given
        AuditConfig config = new AuditConfig();

        // Then - AspectJAutoProxy is disabled (commented out)
        assertFalse(config.getClass().isAnnotationPresent(org.springframework.context.annotation.EnableAspectJAutoProxy.class));
    }
}

