package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ChatRoom domain entity
 */
@DisplayName("ChatRoom Domain Entity Tests")
public class ChatRoomTest {

    private ChatRoom chatRoom;
    private Customer customer;
    private RestaurantProfile restaurant;
    private User admin;

    @BeforeEach
    void setUp() {
        chatRoom = new ChatRoom();
        customer = new Customer();
        customer.setFullName("John Doe");
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setFullName("Admin User");
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateChatRoom_withDefaultConstructor")
    void shouldCreateChatRoom_withDefaultConstructor() {
        // When
        ChatRoom room = new ChatRoom();

        // Then
        assertNotNull(room);
        assertNotNull(room.getCreatedAt());
        assertTrue(room.getIsActive());
    }

    @Test
    @DisplayName("shouldCreateChatRoom_withCustomerRestaurantConstructor")
    void shouldCreateChatRoom_withCustomerRestaurantConstructor() {
        // Given
        String roomId = "room-123";

        // When
        ChatRoom room = new ChatRoom(roomId, customer, restaurant);

        // Then
        assertNotNull(room);
        assertEquals(roomId, room.getRoomId());
        assertEquals(customer, room.getCustomer());
        assertEquals(restaurant, room.getRestaurant());
        assertNull(room.getAdmin());
    }

    @Test
    @DisplayName("shouldCreateChatRoom_withAdminRestaurantConstructor")
    void shouldCreateChatRoom_withAdminRestaurantConstructor() {
        // Given
        String roomId = "room-456";

        // When
        ChatRoom room = new ChatRoom(roomId, admin, restaurant);

        // Then
        assertNotNull(room);
        assertEquals(roomId, room.getRoomId());
        assertEquals(admin, room.getAdmin());
        assertEquals(restaurant, room.getRestaurant());
        assertNull(room.getCustomer());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRoomId")
    void shouldSetAndGetRoomId() {
        // Given
        String roomId = "room-789";

        // When
        chatRoom.setRoomId(roomId);

        // Then
        assertEquals(roomId, chatRoom.getRoomId());
    }

    @Test
    @DisplayName("shouldSetAndGetCustomer")
    void shouldSetAndGetCustomer() {
        // When
        chatRoom.setCustomer(customer);

        // Then
        assertEquals(customer, chatRoom.getCustomer());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurant")
    void shouldSetAndGetRestaurant() {
        // When
        chatRoom.setRestaurant(restaurant);

        // Then
        assertEquals(restaurant, chatRoom.getRestaurant());
    }

    @Test
    @DisplayName("shouldSetAndGetAdmin")
    void shouldSetAndGetAdmin() {
        // When
        chatRoom.setAdmin(admin);

        // Then
        assertEquals(admin, chatRoom.getAdmin());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        chatRoom.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, chatRoom.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetLastMessageAt")
    void shouldSetAndGetLastMessageAt() {
        // Given
        LocalDateTime lastMessageAt = LocalDateTime.now();

        // When
        chatRoom.setLastMessageAt(lastMessageAt);

        // Then
        assertEquals(lastMessageAt, chatRoom.getLastMessageAt());
    }

    @Test
    @DisplayName("shouldSetAndGetIsActive")
    void shouldSetAndGetIsActive() {
        // When
        chatRoom.setIsActive(false);

        // Then
        assertFalse(chatRoom.getIsActive());
    }

    @Test
    @DisplayName("shouldSetAndGetMessages")
    void shouldSetAndGetMessages() {
        // Given
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        messages.add(message);

        // When
        chatRoom.setMessages(messages);

        // Then
        assertEquals(messages, chatRoom.getMessages());
        assertEquals(1, chatRoom.getMessages().size());
    }

    // ========== Helper Method Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenCustomerRestaurantChat")
    void shouldReturnTrue_whenCustomerRestaurantChat() {
        // Given
        chatRoom.setCustomer(customer);
        chatRoom.setRestaurant(restaurant);
        chatRoom.setAdmin(null);

        // When
        boolean result = chatRoom.isCustomerRestaurantChat();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNotCustomerRestaurantChat")
    void shouldReturnFalse_whenNotCustomerRestaurantChat() {
        // Given
        chatRoom.setAdmin(admin);
        chatRoom.setRestaurant(restaurant);
        chatRoom.setCustomer(null);

        // When
        boolean result = chatRoom.isCustomerRestaurantChat();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenAdminRestaurantChat")
    void shouldReturnTrue_whenAdminRestaurantChat() {
        // Given
        chatRoom.setAdmin(admin);
        chatRoom.setRestaurant(restaurant);
        chatRoom.setCustomer(null);

        // When
        boolean result = chatRoom.isAdminRestaurantChat();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNotAdminRestaurantChat")
    void shouldReturnFalse_whenNotAdminRestaurantChat() {
        // Given
        chatRoom.setCustomer(customer);
        chatRoom.setRestaurant(restaurant);
        chatRoom.setAdmin(null);

        // When
        boolean result = chatRoom.isAdminRestaurantChat();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldGetParticipantName_forCustomerRestaurantChat")
    void shouldGetParticipantName_forCustomerRestaurantChat() {
        // Given
        chatRoom.setCustomer(customer);
        chatRoom.setRestaurant(restaurant);
        chatRoom.setAdmin(null);

        // When
        String participantName = chatRoom.getParticipantName();

        // Then
        assertEquals("John Doe", participantName);
    }

    @Test
    @DisplayName("shouldGetParticipantName_forAdminRestaurantChat")
    void shouldGetParticipantName_forAdminRestaurantChat() {
        // Given
        chatRoom.setAdmin(admin);
        chatRoom.setRestaurant(restaurant);
        chatRoom.setCustomer(null);

        // When
        String participantName = chatRoom.getParticipantName();

        // Then
        assertEquals("Admin User", participantName);
    }

    @Test
    @DisplayName("shouldGetParticipantName_whenUnknown")
    void shouldGetParticipantName_whenUnknown() {
        // Given
        chatRoom.setCustomer(null);
        chatRoom.setAdmin(null);
        chatRoom.setRestaurant(restaurant);

        // When
        String participantName = chatRoom.getParticipantName();

        // Then
        assertEquals("Unknown", participantName);
    }

    @Test
    @DisplayName("shouldGetRestaurantName_whenRestaurantIsSet")
    void shouldGetRestaurantName_whenRestaurantIsSet() {
        // Given
        chatRoom.setRestaurant(restaurant);

        // When
        String restaurantName = chatRoom.getRestaurantName();

        // Then
        assertEquals("Test Restaurant", restaurantName);
    }

    @Test
    @DisplayName("shouldGetRestaurantName_whenRestaurantIsNull")
    void shouldGetRestaurantName_whenRestaurantIsNull() {
        // Given
        chatRoom.setRestaurant(null);

        // When
        String restaurantName = chatRoom.getRestaurantName();

        // Then
        assertEquals("Unknown Restaurant", restaurantName);
    }
}
