package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RestaurantDto
 */
@DisplayName("RestaurantDto Tests")
public class RestaurantDtoTest {

    private RestaurantDto restaurantDto;

    @BeforeEach
    void setUp() {
        restaurantDto = new RestaurantDto();
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRestaurantId_successfully")
    void shouldSetAndGetRestaurantId_successfully() {
        // Given
        Integer restaurantId = 1;

        // When
        restaurantDto.setRestaurantId(restaurantId);

        // Then
        assertEquals(restaurantId, restaurantDto.getRestaurantId());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurantName_successfully")
    void shouldSetAndGetRestaurantName_successfully() {
        // Given
        String name = "Test Restaurant";

        // When
        restaurantDto.setRestaurantName(name);

        // Then
        assertEquals(name, restaurantDto.getRestaurantName());
    }

    @Test
    @DisplayName("shouldSetAndGetAveragePrice_successfully")
    void shouldSetAndGetAveragePrice_successfully() {
        // Given
        BigDecimal averagePrice = new BigDecimal("300000");

        // When
        restaurantDto.setAveragePrice(averagePrice);

        // Then
        assertEquals(averagePrice, restaurantDto.getAveragePrice());
    }
}

