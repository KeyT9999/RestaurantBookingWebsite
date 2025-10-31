package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.example.booking.domain.ChatRoom;
import com.example.booking.domain.Message;
import com.example.booking.domain.User;
import com.example.booking.service.ChatService;

/**
 * Unit tests for ChatApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatApiController Tests")
public class ChatApiControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ChatApiController controller;

    private User user;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        chatRoom = new ChatRoom();
        chatRoom.setRoomId("room-123");
    }

    // ========== createChatRoomWithRestaurant() Tests ==========

    @Test
    @DisplayName("shouldCreateChatRoomWithRestaurant_successfully")
    void shouldCreateChatRoomWithRestaurant_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(com.example.booking.domain.UserRole.ADMIN);
        
        com.example.booking.domain.RestaurantProfile restaurant = new com.example.booking.domain.RestaurantProfile();
        restaurant.setRestaurantId(1);
        chatRoom.setRestaurant(restaurant);
        
        when(chatService.createAdminRestaurantRoom(user.getId(), 1))
            .thenReturn(chatRoom);

        // When
        ResponseEntity<?> response = controller.createChatRoomWithRestaurant(1, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ========== getMessages() Tests ==========

    @Test
    @DisplayName("shouldGetMessages_successfully")
    void shouldGetMessages_successfully() {
        // Given
        List<com.example.booking.dto.ChatMessageDto> messages = new ArrayList<>();
        com.example.booking.dto.ChatMessageDto message = new com.example.booking.dto.ChatMessageDto();
        messages.add(message);

        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.canUserAccessRoom("room-123", user.getId(), user.getRole())).thenReturn(true);
        when(chatService.getMessages("room-123", 0, 200)).thenReturn(messages);

        // When
        ResponseEntity<?> response = controller.getMessages("room-123", 0, 200, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ========== getUnreadCount() Tests ==========

    @Test
    @DisplayName("shouldGetUnreadCount_successfully")
    void shouldGetUnreadCount_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.getUnreadMessageCount(user.getId())).thenReturn(5L);

        // When
        ResponseEntity<?> response = controller.getUnreadCount(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
