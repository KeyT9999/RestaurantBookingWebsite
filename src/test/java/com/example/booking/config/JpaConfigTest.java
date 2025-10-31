package com.example.booking.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for JpaConfig
 */
@SpringBootTest
@DisplayName("JpaConfig Test Suite")
class JpaConfigTest {

    @Autowired(required = false)
    private JpaConfig jpaConfig;

    @Test
    @DisplayName("Should load JpaConfig")
    void testJpaConfigLoads() {
        // JpaConfig is a configuration class that enables JPA auditing
        // The test verifies that the configuration class is properly loaded
        assertThat(jpaConfig).isNotNull();
    }

    @Test
    @DisplayName("Should have EnableJpaAuditing annotation")
    void testEnableJpaAuditingAnnotation() {
        EnableJpaAuditing annotation = JpaConfig.class.getAnnotation(EnableJpaAuditing.class);
        assertThat(annotation).isNotNull();
    }
}

