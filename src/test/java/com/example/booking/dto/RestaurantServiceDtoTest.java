package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestaurantServiceDto Test")
class RestaurantServiceDtoTest {

    @Test
    @DisplayName("Should create RestaurantServiceDto and set/get all fields")
    void testRestaurantServiceDto_ShouldSetAndGetFields() {
        RestaurantServiceDto dto = new RestaurantServiceDto();

        dto.setServiceId(1);
        dto.setName("WiFi");
        dto.setCategory("Amenity");
        dto.setDescription("Free WiFi access");
        dto.setPrice(BigDecimal.ZERO);
        dto.setStatus("ACTIVE");
        dto.setRestaurantId(1);

        assertEquals(1, dto.getServiceId());
        assertEquals("WiFi", dto.getName());
        assertEquals("Amenity", dto.getCategory());
        assertEquals("Free WiFi access", dto.getDescription());
        assertEquals(BigDecimal.ZERO, dto.getPrice());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(1, dto.getRestaurantId());
    }

    @Test
    @DisplayName("Should create RestaurantServiceDto with constructor")
    void testRestaurantServiceDto_Constructor() {
        RestaurantServiceDto dto = new RestaurantServiceDto(
            1, "Parking", "Amenity",
            "Free parking", BigDecimal.ZERO, "ACTIVE", 1
        );

        assertEquals(1, dto.getServiceId());
        assertEquals("Parking", dto.getName());
        assertEquals("Amenity", dto.getCategory());
        assertEquals(1, dto.getRestaurantId());
    }
}

