package com.example.booking.dto.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ToggleFavoriteRequest.
 * Coverage Target: 100%
 * Test Cases: 3
 *
 * @author Professional Test Engineer
 */
@DisplayName("ToggleFavoriteRequest Tests")
class ToggleFavoriteRequestTest {

    @Test
    @DisplayName("Should create request with no-args constructor")
    void noArgsConstructor_SetsDefaults() {
        // When
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getRestaurantId()).isNull();
    }

    @Test
    @DisplayName("Should create request with restaurantId constructor")
    void restaurantIdConstructor_SetsId() {
        // Given
        Integer restaurantId = 123;

        // When
        ToggleFavoriteRequest request = new ToggleFavoriteRequest(restaurantId);

        // Then
        assertThat(request.getRestaurantId()).isEqualTo(restaurantId);
    }

    @Test
    @DisplayName("Should set restaurantId via setter")
    void setter_UpdatesRestaurantId() {
        // Given
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        Integer restaurantId = 456;

        // When
        request.setRestaurantId(restaurantId);

        // Then
        assertThat(request.getRestaurantId()).isEqualTo(restaurantId);
    }
}

