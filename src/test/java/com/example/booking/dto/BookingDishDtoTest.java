package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingDishDto Test")
class BookingDishDtoTest {

    @Test
    @DisplayName("Should create BookingDishDto and set/get all fields")
    void testBookingDishDto_ShouldSetAndGetFields() {
        BookingDishDto dto = new BookingDishDto();

        dto.setDishId(1);
        dto.setDishName("Pizza Margherita");
        dto.setDescription("Classic Italian pizza");
        dto.setQuantity(2);
        dto.setPrice(BigDecimal.valueOf(150000));
        dto.setTotalPrice(BigDecimal.valueOf(300000));
        dto.setCategory("Main Course");

        assertEquals(1, dto.getDishId());
        assertEquals("Pizza Margherita", dto.getDishName());
        assertEquals("Classic Italian pizza", dto.getDescription());
        assertEquals(2, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(150000), dto.getPrice());
        assertEquals(BigDecimal.valueOf(300000), dto.getTotalPrice());
        assertEquals("Main Course", dto.getCategory());
    }

    @Test
    @DisplayName("Should create BookingDishDto with constructor")
    void testBookingDishDto_Constructor() {
        BookingDishDto dto = new BookingDishDto(
            1, "Pasta Carbonara", "Creamy pasta",
            1, BigDecimal.valueOf(120000), BigDecimal.valueOf(120000),
            "Main Course"
        );

        assertEquals(1, dto.getDishId());
        assertEquals("Pasta Carbonara", dto.getDishName());
        assertEquals(1, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(120000), dto.getTotalPrice());
    }

    @Test
    @DisplayName("Should format price correctly")
    void testBookingDishDto_FormatPrice() {
        BookingDishDto dto = new BookingDishDto();
        dto.setPrice(BigDecimal.valueOf(150000));
        dto.setTotalPrice(BigDecimal.valueOf(300000));

        String formattedPrice = dto.getFormattedPrice();
        String formattedTotal = dto.getFormattedTotalPrice();

        assertNotNull(formattedPrice);
        assertNotNull(formattedTotal);
        assertTrue(formattedPrice.contains("VND"));
        assertTrue(formattedTotal.contains("VND"));
    }
}

