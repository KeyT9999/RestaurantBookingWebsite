package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingServiceDto Test")
class BookingServiceDtoTest {

    @Test
    @DisplayName("Should create BookingServiceDto and set/get all fields")
    void testBookingServiceDto_ShouldSetAndGetFields() {
        BookingServiceDto dto = new BookingServiceDto();

        dto.setServiceId(1);
        dto.setServiceName("Live Music");
        dto.setDescription("Jazz performance");
        dto.setQuantity(1);
        dto.setPrice(BigDecimal.valueOf(100000));
        dto.setTotalPrice(BigDecimal.valueOf(100000));
        dto.setCategory("Entertainment");

        assertEquals(1, dto.getServiceId());
        assertEquals("Live Music", dto.getServiceName());
        assertEquals("Jazz performance", dto.getDescription());
        assertEquals(1, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(100000), dto.getPrice());
        assertEquals(BigDecimal.valueOf(100000), dto.getTotalPrice());
        assertEquals("Entertainment", dto.getCategory());
    }

    @Test
    @DisplayName("Should create BookingServiceDto with constructor")
    void testBookingServiceDto_Constructor() {
        BookingServiceDto dto = new BookingServiceDto(
            1, "VIP Service", "Premium service",
            2, BigDecimal.valueOf(200000), BigDecimal.valueOf(400000),
            "Premium"
        );

        assertEquals(1, dto.getServiceId());
        assertEquals("VIP Service", dto.getServiceName());
        assertEquals(2, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(400000), dto.getTotalPrice());
    }

    @Test
    @DisplayName("Should format price correctly")
    void testBookingServiceDto_FormatPrice() {
        BookingServiceDto dto = new BookingServiceDto();
        dto.setPrice(BigDecimal.valueOf(100000));
        dto.setTotalPrice(BigDecimal.valueOf(200000));

        String formattedPrice = dto.getFormattedPrice();
        String formattedTotal = dto.getFormattedTotalPrice();

        assertNotNull(formattedPrice);
        assertNotNull(formattedTotal);
        assertTrue(formattedPrice.contains("VND"));
        assertTrue(formattedTotal.contains("VND"));
    }
}

