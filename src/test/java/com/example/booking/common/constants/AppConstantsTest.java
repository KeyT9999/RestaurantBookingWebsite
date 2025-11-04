package com.example.booking.common.constants;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AppConstants
 */
@DisplayName("AppConstants Tests")
public class AppConstantsTest {

    @Test
    @DisplayName("shouldExist_successfully")
    void shouldExist_successfully() {
        // Given
        AppConstants constants = new AppConstants();

        // When & Then
        assertNotNull(constants);
        // AppConstants is currently empty, but tests ensure class can be instantiated
    }
}

