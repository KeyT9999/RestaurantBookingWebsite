package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ChatRoomDto
 */
@DisplayName("ChatRoomDto Tests")
public class ChatRoomDtoTest {

    private ChatRoomDto chatRoomDto;

    @BeforeEach
    void setUp() {
        chatRoomDto = new ChatRoomDto();
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRoomId_successfully")
    void shouldSetAndGetRoomId_successfully() {
        // Given
        String roomId = "room-123";

        // When
        chatRoomDto.setRoomId(roomId);

        // Then
        assertEquals(roomId, chatRoomDto.getRoomId());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurantName_successfully")
    void shouldSetAndGetRestaurantName_successfully() {
        // Given
        String restaurantName = "Test Restaurant";

        // When
        chatRoomDto.setRestaurantName(restaurantName);

        // Then
        assertEquals(restaurantName, chatRoomDto.getRestaurantName());
    }

    @Test
    @DisplayName("shouldSetAndGetUnreadCount_successfully")
    void shouldSetAndGetUnreadCount_successfully() {
        // Given
        Long unreadCount = 5L;

        // When
        chatRoomDto.setUnreadCount(unreadCount);

        // Then
        assertEquals(unreadCount, chatRoomDto.getUnreadCount());
    }
}

