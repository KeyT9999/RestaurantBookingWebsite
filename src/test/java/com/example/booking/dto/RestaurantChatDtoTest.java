package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestaurantChatDto Test")
class RestaurantChatDtoTest {

    @Test
    @DisplayName("Should create RestaurantChatDto and set/get all fields")
    void testRestaurantChatDto_ShouldSetAndGetFields() {
        RestaurantChatDto dto = new RestaurantChatDto();

        dto.setRestaurantId(1);
        dto.setRestaurantName("Test Restaurant");
        dto.setAddress("123 Main St");
        dto.setPhone("0123456789");
        dto.setActive(true);
        dto.setRoomId("room-123");
        dto.setUnreadCount(5L);

        assertEquals(1, dto.getRestaurantId());
        assertEquals("Test Restaurant", dto.getRestaurantName());
        assertEquals("123 Main St", dto.getAddress());
        assertEquals("0123456789", dto.getPhone());
        assertTrue(dto.isActive());
        assertEquals("room-123", dto.getRoomId());
        assertEquals(5L, dto.getUnreadCount());
    }

    @Test
    @DisplayName("Should create RestaurantChatDto with constructor (5 params)")
    void testRestaurantChatDto_Constructor5Params() {
        RestaurantChatDto dto = new RestaurantChatDto(
            1, "Test Restaurant", "123 Main St", "0123456789", true
        );

        assertEquals(1, dto.getRestaurantId());
        assertEquals("Test Restaurant", dto.getRestaurantName());
        assertEquals("123 Main St", dto.getAddress());
        assertEquals("0123456789", dto.getPhone());
        assertTrue(dto.isActive());
    }

    @Test
    @DisplayName("Should create RestaurantChatDto with constructor (6 params)")
    void testRestaurantChatDto_Constructor6Params() {
        RestaurantChatDto dto = new RestaurantChatDto(
            1, "Test Restaurant", "123 Main St", "0123456789", true, "room-456"
        );

        assertEquals(1, dto.getRestaurantId());
        assertEquals("Test Restaurant", dto.getRestaurantName());
        assertEquals("room-456", dto.getRoomId());
    }
}

