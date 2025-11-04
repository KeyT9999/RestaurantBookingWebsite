package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit tests for JpaConfig
 */
@SpringBootTest
@Import(JpaConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=validate"
})
@DisplayName("JpaConfig Tests")
public class JpaConfigTest {

    @Test
    @DisplayName("shouldLoadJpaConfig_successfully")
    void shouldLoadJpaConfig_successfully() {
        // Given
        JpaConfig config = new JpaConfig();

        // Then
        assertNotNull(config);
    }

    @Test
    @DisplayName("shouldBeConfigurationClass")
    void shouldBeConfigurationClass() {
        // Given
        JpaConfig config = new JpaConfig();

        // Then
        assertTrue(config.getClass().isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
    }

    @Test
    @DisplayName("shouldEnableJpaAuditing")
    void shouldEnableJpaAuditing() {
        // Given
        JpaConfig config = new JpaConfig();

        // Then
        assertTrue(config.getClass().isAnnotationPresent(org.springframework.data.jpa.repository.config.EnableJpaAuditing.class));
    }
}

