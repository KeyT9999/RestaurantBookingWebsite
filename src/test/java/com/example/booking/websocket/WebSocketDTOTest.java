package com.example.booking.websocket;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for WebSocket DTOs to improve coverage
 */
@DisplayName("WebSocket DTO Coverage Tests")
class WebSocketDTOTest {

    private final String testRoomId = "customer_" + UUID.randomUUID() + "_restaurant_1";
    private final String testUserId = UUID.randomUUID().toString();

    @Test
    @DisplayName("ChatMessageRequest - Constructor and Getters")
    void testChatMessageRequestConstructorAndGetters() {
        // When
        ChatMessageController.ChatMessageRequest request = 
            new ChatMessageController.ChatMessageRequest(testRoomId, "Test content");
        
        // Then
        assertNotNull(request);
        assertEquals(testRoomId, request.getRoomId());
        assertEquals("Test content", request.getContent());
    }

    @Test
    @DisplayName("ChatMessageRequest - Default Constructor and Setters")
    void testChatMessageRequestDefaultConstructorAndSetters() {
        // Given
        ChatMessageController.ChatMessageRequest request = 
            new ChatMessageController.ChatMessageRequest();
        
        // When
        request.setRoomId(testRoomId);
        request.setContent("Updated content");
        
        // Then
        assertNotNull(request);
        assertEquals(testRoomId, request.getRoomId());
        assertEquals("Updated content", request.getContent());
    }

    @Test
    @DisplayName("ChatMessageResponse - Getters coverage")
    void testChatMessageResponseGetters() throws Exception {
        // Given
        com.example.booking.domain.ChatRoom room = new com.example.booking.domain.ChatRoom();
        com.example.booking.domain.User sender = new com.example.booking.domain.User();
        sender.setId(UUID.fromString(testUserId));
        sender.setRole(com.example.booking.domain.UserRole.CUSTOMER);
        sender.setFullName("Test User");
        sender.setEmail("test@example.com");
        
        com.example.booking.domain.Message message = new com.example.booking.domain.Message();
        message.setMessageId(1);
        message.setRoom(room);
        message.setSender(sender);
        message.setContent("Test message");
        message.setMessageType(com.example.booking.domain.MessageType.TEXT);
        message.setSentAt(java.time.LocalDateTime.now());
        room.setRoomId(testRoomId);
        
        // When
        ChatMessageController.ChatMessageResponse response = 
            new ChatMessageController.ChatMessageResponse(message);
        
        // Then - Verify all getters are called
        assertNotNull(response.getMessageId());
        assertNotNull(response.getRoomId());
        assertNotNull(response.getSenderId());
        assertNotNull(response.getSenderName());
        assertNotNull(response.getSenderRole());
        assertNotNull(response.getContent());
        assertNotNull(response.getMessageType());
        assertNotNull(response.getSentAt());
        
        // Verify values
        assertEquals(1, response.getMessageId());
        assertEquals(testRoomId, response.getRoomId());
        assertEquals(testUserId, response.getSenderId());
        assertEquals("Test message", response.getContent());
    }

    @Test
    @DisplayName("TypingRequest - Constructor and Getters")
    void testTypingRequestConstructorAndGetters() {
        // When
        ChatMessageController.TypingRequest request = 
            new ChatMessageController.TypingRequest(testRoomId, true);
        
        // Then
        assertNotNull(request);
        assertEquals(testRoomId, request.getRoomId());
        assertTrue(request.isTyping());
    }

    @Test
    @DisplayName("TypingRequest - Default Constructor and Setters")
    void testTypingRequestDefaultConstructorAndSetters() {
        // Given
        ChatMessageController.TypingRequest request = 
            new ChatMessageController.TypingRequest();
        
        // When
        request.setRoomId(testRoomId);
        request.setTyping(false);
        
        // Then
        assertNotNull(request);
        assertEquals(testRoomId, request.getRoomId());
        assertFalse(request.isTyping());
    }

    @Test
    @DisplayName("TypingResponse - Constructor and Getters")
    void testTypingResponseConstructorAndGetters() {
        // When
        ChatMessageController.TypingResponse response = 
            new ChatMessageController.TypingResponse(testUserId, true);
        
        // Then
        assertNotNull(response);
        assertEquals(testUserId, response.getUserId());
        assertTrue(response.isTyping());
    }

    @Test
    @DisplayName("JoinRoomRequest - Constructor and Getters")
    void testJoinRoomRequestConstructorAndGetters() {
        // When
        ChatMessageController.JoinRoomRequest request = 
            new ChatMessageController.JoinRoomRequest(testRoomId);
        
        // Then
        assertNotNull(request);
        assertEquals(testRoomId, request.getRoomId());
    }

    @Test
    @DisplayName("JoinRoomRequest - Default Constructor and Setters")
    void testJoinRoomRequestDefaultConstructorAndSetters() {
        // Given
        ChatMessageController.JoinRoomRequest request = 
            new ChatMessageController.JoinRoomRequest();
        
        // When
        request.setRoomId(testRoomId);
        
        // Then
        assertNotNull(request);
        assertEquals(testRoomId, request.getRoomId());
    }

    @Test
    @DisplayName("UserJoinedResponse - Constructor and Getters")
    void testUserJoinedResponseConstructorAndGetters() {
        // When
        ChatMessageController.UserJoinedResponse response = 
            new ChatMessageController.UserJoinedResponse(testUserId);
        
        // Then
        assertNotNull(response);
        assertEquals(testUserId, response.getUserId());
    }

    @Test
    @DisplayName("ErrorResponse - Constructor and Getters")
    void testErrorResponseConstructorAndGetters() {
        // When
        ChatMessageController.ErrorResponse response = 
            new ChatMessageController.ErrorResponse("Test error message");
        
        // Then
        assertNotNull(response);
        assertEquals("Test error message", response.getMessage());
    }

    @Test
    @DisplayName("UnreadCountUpdate - Constructor and Getters")
    void testUnreadCountUpdateConstructorAndGetters() {
        // When
        UUID participantId = UUID.randomUUID();
        ChatMessageController.UnreadCountUpdate update = 
            new ChatMessageController.UnreadCountUpdate(testRoomId, participantId, 5L, 10L);
        
        // Then
        assertNotNull(update);
        assertEquals(testRoomId, update.getRoomId());
        assertEquals(participantId.toString(), update.getUserId());
        assertEquals(5L, update.getRoomUnreadCount());
        assertEquals(10L, update.getTotalUnreadCount());
    }
}















