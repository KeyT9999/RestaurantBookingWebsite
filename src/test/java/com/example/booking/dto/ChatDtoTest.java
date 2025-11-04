package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for Chat DTOs: ChatRoomDto, ChatMessageDto
 */
@DisplayName("Chat DTOs Test Suite")
class ChatDtoTest {

    @Nested
    @DisplayName("ChatRoomDto Tests")
    class ChatRoomDtoTests {

        @Test
        @DisplayName("Should create ChatRoomDto with default constructor")
        void testDefaultConstructor() {
            ChatRoomDto dto = new ChatRoomDto();
            assertThat(dto).isNotNull();
        }

        @Test
        @DisplayName("Should create ChatRoomDto with all constructor parameters")
        void testAllArgsConstructor() {
            UUID participantId = UUID.randomUUID();
            LocalDateTime lastMessageAt = LocalDateTime.now();

            ChatRoomDto dto = new ChatRoomDto("room-123", participantId, "John Doe",
                    "CUSTOMER", 10, "Restaurant Name", "Hello", lastMessageAt, 5L, true);

            assertThat(dto.getRoomId()).isEqualTo("room-123");
            assertThat(dto.getParticipantId()).isEqualTo(participantId);
            assertThat(dto.getParticipantName()).isEqualTo("John Doe");
            assertThat(dto.getParticipantRole()).isEqualTo("CUSTOMER");
            assertThat(dto.getRestaurantId()).isEqualTo(10);
            assertThat(dto.getRestaurantName()).isEqualTo("Restaurant Name");
            assertThat(dto.getLastMessage()).isEqualTo("Hello");
            assertThat(dto.getLastMessageAt()).isEqualTo(lastMessageAt);
            assertThat(dto.getUnreadCount()).isEqualTo(5L);
            assertThat(dto.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            ChatRoomDto dto = new ChatRoomDto();
            UUID participantId = UUID.randomUUID();
            LocalDateTime lastMessageAt = LocalDateTime.now();

            dto.setRoomId("room-456");
            dto.setParticipantId(participantId);
            dto.setParticipantName("Jane Doe");
            dto.setParticipantRole("RESTAURANT_OWNER");
            dto.setRestaurantId(20);
            dto.setRestaurantName("Another Restaurant");
            dto.setLastMessage("Hi there");
            dto.setLastMessageAt(lastMessageAt);
            dto.setUnreadCount(10L);
            dto.setIsActive(false);

            assertThat(dto.getRoomId()).isEqualTo("room-456");
            assertThat(dto.getParticipantId()).isEqualTo(participantId);
            assertThat(dto.getParticipantName()).isEqualTo("Jane Doe");
            assertThat(dto.getParticipantRole()).isEqualTo("RESTAURANT_OWNER");
            assertThat(dto.getRestaurantId()).isEqualTo(20);
            assertThat(dto.getRestaurantName()).isEqualTo("Another Restaurant");
            assertThat(dto.getLastMessage()).isEqualTo("Hi there");
            assertThat(dto.getLastMessageAt()).isEqualTo(lastMessageAt);
            assertThat(dto.getUnreadCount()).isEqualTo(10L);
            assertThat(dto.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            ChatRoomDto dto = new ChatRoomDto();

            dto.setRoomId(null);
            dto.setParticipantId(null);
            dto.setParticipantName(null);
            dto.setParticipantRole(null);
            dto.setRestaurantName(null);
            dto.setLastMessage(null);
            dto.setLastMessageAt(null);
            dto.setUnreadCount(null);
            dto.setIsActive(null);

            assertThat(dto.getRoomId()).isNull();
            assertThat(dto.getParticipantId()).isNull();
            assertThat(dto.getParticipantName()).isNull();
            assertThat(dto.getParticipantRole()).isNull();
            assertThat(dto.getRestaurantName()).isNull();
            assertThat(dto.getLastMessage()).isNull();
            assertThat(dto.getLastMessageAt()).isNull();
            assertThat(dto.getUnreadCount()).isNull();
            assertThat(dto.getIsActive()).isNull();
        }

        @Test
        @DisplayName("Should handle zero unread count")
        void testZeroUnreadCount() {
            ChatRoomDto dto = new ChatRoomDto();
            dto.setUnreadCount(0L);

            assertThat(dto.getUnreadCount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("ChatMessageDto Tests")
    class ChatMessageDtoTests {

        @Test
        @DisplayName("Should create ChatMessageDto with default constructor")
        void testDefaultConstructor() {
            ChatMessageDto dto = new ChatMessageDto();
            assertThat(dto).isNotNull();
        }

        @Test
        @DisplayName("Should create ChatMessageDto with all constructor parameters")
        void testAllArgsConstructor() {
            UUID senderId = UUID.randomUUID();
            LocalDateTime sentAt = LocalDateTime.now();

            ChatMessageDto dto = new ChatMessageDto(1, "room-123", senderId, "John Doe",
                    "Hello", "TEXT", "file.jpg", sentAt, false);

            assertThat(dto.getMessageId()).isEqualTo(1);
            assertThat(dto.getRoomId()).isEqualTo("room-123");
            assertThat(dto.getSenderId()).isEqualTo(senderId);
            assertThat(dto.getSenderName()).isEqualTo("John Doe");
            assertThat(dto.getContent()).isEqualTo("Hello");
            assertThat(dto.getMessageType()).isEqualTo("TEXT");
            assertThat(dto.getFileUrl()).isEqualTo("file.jpg");
            assertThat(dto.getSentAt()).isEqualTo(sentAt);
            assertThat(dto.getIsRead()).isFalse();
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            ChatMessageDto dto = new ChatMessageDto();
            UUID senderId = UUID.randomUUID();
            LocalDateTime sentAt = LocalDateTime.now();

            dto.setMessageId(2);
            dto.setRoomId("room-456");
            dto.setSenderId(senderId);
            dto.setSenderName("Jane Doe");
            dto.setContent("Hi there");
            dto.setMessageType("IMAGE");
            dto.setFileUrl("image.jpg");
            dto.setSentAt(sentAt);
            dto.setIsRead(true);

            assertThat(dto.getMessageId()).isEqualTo(2);
            assertThat(dto.getRoomId()).isEqualTo("room-456");
            assertThat(dto.getSenderId()).isEqualTo(senderId);
            assertThat(dto.getSenderName()).isEqualTo("Jane Doe");
            assertThat(dto.getContent()).isEqualTo("Hi there");
            assertThat(dto.getMessageType()).isEqualTo("IMAGE");
            assertThat(dto.getFileUrl()).isEqualTo("image.jpg");
            assertThat(dto.getSentAt()).isEqualTo(sentAt);
            assertThat(dto.getIsRead()).isTrue();
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            ChatMessageDto dto = new ChatMessageDto();

            dto.setRoomId(null);
            dto.setSenderId(null);
            dto.setSenderName(null);
            dto.setContent(null);
            dto.setMessageType(null);
            dto.setFileUrl(null);
            dto.setSentAt(null);
            dto.setIsRead(null);

            assertThat(dto.getRoomId()).isNull();
            assertThat(dto.getSenderId()).isNull();
            assertThat(dto.getSenderName()).isNull();
            assertThat(dto.getContent()).isNull();
            assertThat(dto.getMessageType()).isNull();
            assertThat(dto.getFileUrl()).isNull();
            assertThat(dto.getSentAt()).isNull();
            assertThat(dto.getIsRead()).isNull();
        }

        @Test
        @DisplayName("Should handle different message types")
        void testDifferentMessageTypes() {
            ChatMessageDto dto = new ChatMessageDto();

            dto.setMessageType("TEXT");
            assertThat(dto.getMessageType()).isEqualTo("TEXT");

            dto.setMessageType("IMAGE");
            assertThat(dto.getMessageType()).isEqualTo("IMAGE");

            dto.setMessageType("FILE");
            assertThat(dto.getMessageType()).isEqualTo("FILE");
        }
    }
}

