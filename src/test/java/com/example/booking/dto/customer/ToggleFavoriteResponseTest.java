package com.example.booking.dto.customer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ToggleFavoriteResponse
 */
@DisplayName("ToggleFavoriteResponse Tests")
public class ToggleFavoriteResponseTest {

    // ========== success() Tests ==========

    @Test
    @DisplayName("shouldCreateSuccessResponse_successfully")
    void shouldCreateSuccessResponse_successfully() {
        // When
        ToggleFavoriteResponse response = ToggleFavoriteResponse.success(true, 5, 1);

        // Then
        assertTrue(response.isSuccess());
        assertTrue(response.isFavorited());
        assertEquals(5, response.getFavoriteCount());
    }

    // ========== error() Tests ==========

    @Test
    @DisplayName("shouldCreateErrorResponse_successfully")
    void shouldCreateErrorResponse_successfully() {
        // When
        ToggleFavoriteResponse response = ToggleFavoriteResponse.error("Error occurred");

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Error occurred", response.getMessage());
    }
}

