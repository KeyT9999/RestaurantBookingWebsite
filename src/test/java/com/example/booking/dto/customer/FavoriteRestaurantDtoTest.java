package com.example.booking.dto.customer;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for FavoriteRestaurantDto
 */
@DisplayName("FavoriteRestaurantDto Tests")
public class FavoriteRestaurantDtoTest {

    private FavoriteRestaurantDto dto;

    @BeforeEach
    void setUp() {
        dto = new FavoriteRestaurantDto();
    }

    // ========== Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetFields_successfully")
    void shouldSetAndGetFields_successfully() {
        // Given
        Integer restaurantId = 1;
        String restaurantName = "Test Restaurant";
        String address = "123 Test St";
        BigDecimal averagePrice = new BigDecimal("300000");
        Double averageRating = 4.5;
        Integer reviewCount = 10;
        LocalDateTime favoritedAt = LocalDateTime.now();

        // When
        dto.setRestaurantId(restaurantId);
        dto.setRestaurantName(restaurantName);
        dto.setAddress(address);
        dto.setAveragePrice(averagePrice);
        dto.setAverageRating(averageRating);
        dto.setReviewCount(reviewCount);
        dto.setFavoritedAt(favoritedAt);

        // Then
        assertEquals(restaurantId, dto.getRestaurantId());
        assertEquals(restaurantName, dto.getRestaurantName());
        assertEquals(address, dto.getAddress());
        assertEquals(averagePrice, dto.getAveragePrice());
        assertEquals(averageRating, dto.getAverageRating());
        assertEquals(reviewCount, dto.getReviewCount());
        assertEquals(favoritedAt, dto.getFavoritedAt());
    }
}

