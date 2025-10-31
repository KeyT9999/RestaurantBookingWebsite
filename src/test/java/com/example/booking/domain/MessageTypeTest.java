package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for MessageType enum
 */
@DisplayName("MessageType Enum Tests")
public class MessageTypeTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(MessageType.TEXT);
        assertNotNull(MessageType.IMAGE);
        assertNotNull(MessageType.FILE);
        assertNotNull(MessageType.SYSTEM);
    }

    @Test
    @DisplayName("shouldGetValue_forAllValues")
    void shouldGetValue_forAllValues() {
        assertEquals("TEXT", MessageType.TEXT.getValue());
        assertEquals("IMAGE", MessageType.IMAGE.getValue());
        assertEquals("FILE", MessageType.FILE.getValue());
        assertEquals("SYSTEM", MessageType.SYSTEM.getValue());
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Tin nhắn văn bản", MessageType.TEXT.getDisplayName());
        assertEquals("Hình ảnh", MessageType.IMAGE.getDisplayName());
        assertEquals("Tệp tin", MessageType.FILE.getDisplayName());
        assertEquals("Tin nhắn hệ thống", MessageType.SYSTEM.getDisplayName());
    }
}
