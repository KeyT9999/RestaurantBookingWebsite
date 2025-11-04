package com.example.booking.dto.customer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ToggleFavoriteRequest
 */
@DisplayName("ToggleFavoriteRequest Tests")
public class ToggleFavoriteRequestTest {

    private ToggleFavoriteRequest request;

    @BeforeEach
    void setUp() {
        request = new ToggleFavoriteRequest();
    }

    // ========== Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRestaurantId_successfully")
    void shouldSetAndGetRestaurantId_successfully() {
        // Given
        Integer restaurantId = 1;

        // When
        request.setRestaurantId(restaurantId);

        // Then
        assertEquals(restaurantId, request.getRestaurantId());
    }
}

