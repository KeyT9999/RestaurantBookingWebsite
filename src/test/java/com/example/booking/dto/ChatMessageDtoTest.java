package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatMessageDto Test")
class ChatMessageDtoTest {

    @Test
    @DisplayName("Should create ChatMessageDto and set/get all fields")
    void testChatMessageDto_ShouldSetAndGetFields() {
        ChatMessageDto dto = new ChatMessageDto();
        UUID senderId = UUID.randomUUID();
        LocalDateTime sentAt = LocalDateTime.now();

        dto.setMessageId(1);
        dto.setRoomId("room-123");
        dto.setSenderId(senderId);
        dto.setSenderName("John Doe");
        dto.setContent("Hello, world!");
        dto.setMessageType("TEXT");
        dto.setFileUrl(null);
        dto.setSentAt(sentAt);
        dto.setIsRead(false);

        assertEquals(1, dto.getMessageId());
        assertEquals("room-123", dto.getRoomId());
        assertEquals(senderId, dto.getSenderId());
        assertEquals("John Doe", dto.getSenderName());
        assertEquals("Hello, world!", dto.getContent());
        assertEquals("TEXT", dto.getMessageType());
        assertNull(dto.getFileUrl());
        assertEquals(sentAt, dto.getSentAt());
        assertFalse(dto.getIsRead());
    }

    @Test
    @DisplayName("Should create ChatMessageDto with constructor")
    void testChatMessageDto_Constructor() {
        UUID senderId = UUID.randomUUID();
        LocalDateTime sentAt = LocalDateTime.now();

        ChatMessageDto dto = new ChatMessageDto(
            1, "room-123", senderId, "Jane Doe", "Test message",
            "TEXT", null, sentAt, true
        );

        assertEquals(1, dto.getMessageId());
        assertEquals("room-123", dto.getRoomId());
        assertEquals(senderId, dto.getSenderId());
        assertEquals("Jane Doe", dto.getSenderName());
        assertEquals("Test message", dto.getContent());
        assertTrue(dto.getIsRead());
    }
}

