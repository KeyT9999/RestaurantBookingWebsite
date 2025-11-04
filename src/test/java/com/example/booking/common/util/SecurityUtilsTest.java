package com.example.booking.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SecurityUtils
 */
@DisplayName("SecurityUtils Tests")
public class SecurityUtilsTest {

    @Test
    @DisplayName("shouldExist_successfully")
    void shouldExist_successfully() {
        // Given
        SecurityUtils utils = new SecurityUtils();

        // When & Then
        assertNotNull(utils);
        // SecurityUtils is currently empty, but tests ensure class can be instantiated
    }
}

