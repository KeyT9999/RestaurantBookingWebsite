package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DishDto
 */
@DisplayName("DishDto Tests")
public class DishDtoTest {

    private DishDto dishDto;

    @BeforeEach
    void setUp() {
        dishDto = new DishDto();
    }

    @Test
    @DisplayName("shouldSetAndGetDishId_successfully")
    void shouldSetAndGetDishId_successfully() {
        // Given
        Integer dishId = 1;

        // When
        dishDto.setDishId(dishId);

        // Then
        assertEquals(dishId, dishDto.getDishId());
    }

    @Test
    @DisplayName("shouldSetAndGetName_successfully")
    void shouldSetAndGetName_successfully() {
        // Given
        String name = "Pizza Margherita";

        // When
        dishDto.setName(name);

        // Then
        assertEquals(name, dishDto.getName());
    }

    @Test
    @DisplayName("shouldSetAndGetPrice_successfully")
    void shouldSetAndGetPrice_successfully() {
        // Given
        BigDecimal price = new BigDecimal("150000");

        // When
        dishDto.setPrice(price);

        // Then
        assertEquals(price, dishDto.getPrice());
    }
}

