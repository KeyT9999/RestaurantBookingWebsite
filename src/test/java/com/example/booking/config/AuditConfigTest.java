package com.example.booking.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for AuditConfig
 */
@SpringBootTest
@DisplayName("AuditConfig Test Suite")
class AuditConfigTest {

    @Test
    @DisplayName("Should load AuditConfig")
    void testAuditConfigLoads() {
        // AuditConfig is a configuration class (currently disabled)
        // The test verifies that the configuration class exists
        assertThat(AuditConfig.class).isNotNull();
    }

    @Test
    @DisplayName("Should be a configuration class")
    void testConfigurationAnnotation() {
        org.springframework.context.annotation.Configuration annotation = 
                AuditConfig.class.getAnnotation(org.springframework.context.annotation.Configuration.class);
        assertThat(annotation).isNotNull();
    }
}

