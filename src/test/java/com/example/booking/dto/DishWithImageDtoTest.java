package com.example.booking.dto;

import com.example.booking.domain.Dish;
import com.example.booking.domain.DishStatus;
import com.example.booking.domain.RestaurantProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("DishWithImageDto Test")
class DishWithImageDtoTest {

    @Test
    @DisplayName("Should create DishWithImageDto and set/get all fields")
    void testDishWithImageDto_ShouldSetAndGetFields() {
        DishWithImageDto dto = new DishWithImageDto();

        dto.setDishId(1);
        dto.setName("Pizza Margherita");
        dto.setDescription("Classic Italian pizza");
        dto.setPrice(BigDecimal.valueOf(150000));
        dto.setCategory("Main Course");
        dto.setStatus(DishStatus.AVAILABLE);
        dto.setRestaurantId(1);
        dto.setImageUrl("https://example.com/pizza.jpg");

        assertEquals(1, dto.getDishId());
        assertEquals("Pizza Margherita", dto.getName());
        assertEquals("Classic Italian pizza", dto.getDescription());
        assertEquals(BigDecimal.valueOf(150000), dto.getPrice());
        assertEquals("Main Course", dto.getCategory());
        assertEquals(DishStatus.AVAILABLE, dto.getStatus());
        assertEquals(1, dto.getRestaurantId());
        assertEquals("https://example.com/pizza.jpg", dto.getImageUrl());
    }

    @Test
    @DisplayName("Should create DishWithImageDto from Dish")
    void testDishWithImageDto_FromDish() {
        Dish dish = mock(Dish.class);
        RestaurantProfile restaurant = mock(RestaurantProfile.class);

        when(dish.getDishId()).thenReturn(1);
        when(dish.getName()).thenReturn("Pasta");
        when(dish.getDescription()).thenReturn("Italian pasta");
        when(dish.getPrice()).thenReturn(BigDecimal.valueOf(120000));
        when(dish.getCategory()).thenReturn("Main Course");
        when(dish.getStatus()).thenReturn(DishStatus.AVAILABLE);
        when(dish.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getRestaurantId()).thenReturn(1);

        DishWithImageDto dto = new DishWithImageDto(dish, "https://example.com/pasta.jpg");

        assertEquals(1, dto.getDishId());
        assertEquals("Pasta", dto.getName());
        assertEquals("Italian pasta", dto.getDescription());
        assertEquals(BigDecimal.valueOf(120000), dto.getPrice());
        assertEquals("https://example.com/pasta.jpg", dto.getImageUrl());
    }
}

