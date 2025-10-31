package com.example.booking.dto.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for FavoriteStatisticsDto
 */
@DisplayName("FavoriteStatisticsDto Tests")
public class FavoriteStatisticsDtoTest {

    private FavoriteStatisticsDto dto;

    @BeforeEach
    void setUp() {
        dto = new FavoriteStatisticsDto();
    }

    // ========== Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetFields_successfully")
    void shouldSetAndGetFields_successfully() {
        // Given
        Integer restaurantId = 1;
        String restaurantName = "Test Restaurant";
        Long favoriteCount = 10L;
        Double averageRating = 4.5;
        Long reviewCount = 20L;

        // When
        dto.setRestaurantId(restaurantId);
        dto.setRestaurantName(restaurantName);
        dto.setFavoriteCount(favoriteCount);
        dto.setAverageRating(averageRating);
        dto.setReviewCount(reviewCount);

        // Then
        assertEquals(restaurantId, dto.getRestaurantId());
        assertEquals(restaurantName, dto.getRestaurantName());
        assertEquals(favoriteCount, dto.getFavoriteCount());
        assertEquals(averageRating, dto.getAverageRating());
        assertEquals(reviewCount, dto.getReviewCount());
    }
}

