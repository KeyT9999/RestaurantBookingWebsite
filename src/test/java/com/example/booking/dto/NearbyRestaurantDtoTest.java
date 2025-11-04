package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NearbyRestaurantDto Test")
class NearbyRestaurantDtoTest {

    @Test
    @DisplayName("Should create NearbyRestaurantDto and set/get all fields")
    void testNearbyRestaurantDto_ShouldSetAndGetFields() {
        NearbyRestaurantDto dto = new NearbyRestaurantDto();
        LocalDateTime createdAt = LocalDateTime.now();

        dto.setRestaurantId(1);
        dto.setName("Test Restaurant");
        dto.setAddress("123 Main St");
        dto.setCuisineType("Italian");
        dto.setAveragePrice(BigDecimal.valueOf(200000));
        dto.setMainImageUrl("https://example.com/image.jpg");
        dto.setDistanceKm(2.5);
        dto.setCreatedAt(createdAt);

        assertEquals(1, dto.getRestaurantId());
        assertEquals("Test Restaurant", dto.getName());
        assertEquals("123 Main St", dto.getAddress());
        assertEquals("Italian", dto.getCuisineType());
        assertEquals(BigDecimal.valueOf(200000), dto.getAveragePrice());
        assertEquals("https://example.com/image.jpg", dto.getMainImageUrl());
        assertEquals(2.5, dto.getDistanceKm());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should create NearbyRestaurantDto with constructor")
    void testNearbyRestaurantDto_Constructor() {
        LocalDateTime createdAt = LocalDateTime.now();
        NearbyRestaurantDto dto = new NearbyRestaurantDto(
            1, "Test Restaurant", "123 Main St", "Italian",
            BigDecimal.valueOf(200000), "https://example.com/image.jpg",
            2.5, createdAt
        );

        assertEquals(1, dto.getRestaurantId());
        assertEquals("Test Restaurant", dto.getName());
        assertEquals(2.5, dto.getDistanceKm());
    }
}

