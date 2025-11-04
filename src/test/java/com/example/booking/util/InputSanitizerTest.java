package com.example.booking.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for InputSanitizer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InputSanitizer Tests")
public class InputSanitizerTest {

    @InjectMocks
    private InputSanitizer inputSanitizer;

    // ========== sanitizeReviewComment() Tests ==========

    @Test
    @DisplayName("shouldSanitizeReviewComment_successfully")
    void shouldSanitizeReviewComment_successfully() {
        // Given
        String input = "Great restaurant! <strong>Love it</strong>";

        // When
        String result = inputSanitizer.sanitizeReviewComment(input);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("<script>"));
    }

    @Test
    @DisplayName("shouldRemoveXSSFromReviewComment")
    void shouldRemoveXSSFromReviewComment() {
        // Given
        String input = "Test <script>alert('XSS')</script> comment";

        // When
        String result = inputSanitizer.sanitizeReviewComment(input);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("javascript:"));
    }

    @Test
    @DisplayName("shouldReturnNull_whenInputIsNull")
    void shouldReturnNull_whenInputIsNull() {
        // When
        String result = inputSanitizer.sanitizeReviewComment(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenInputIsEmpty")
    void shouldReturnEmpty_whenInputIsEmpty() {
        // Given
        String input = "   ";

        // When
        String result = inputSanitizer.sanitizeReviewComment(input);

        // Then
        assertEquals("", result);
    }

    // ========== sanitizeChatMessage() Tests ==========

    @Test
    @DisplayName("shouldSanitizeChatMessage_successfully")
    void shouldSanitizeChatMessage_successfully() {
        // Given
        String input = "Hello <strong>world</strong>";

        // When
        String result = inputSanitizer.sanitizeChatMessage(input);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("<script>"));
    }

    @Test
    @DisplayName("shouldRemoveXSSFromChatMessage")
    void shouldRemoveXSSFromChatMessage() {
        // Given
        String input = "Test <script>alert('XSS')</script> message";

        // When
        String result = inputSanitizer.sanitizeChatMessage(input);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("<script>"));
    }

    // ========== sanitizeText() Tests ==========

    @Test
    @DisplayName("shouldSanitizeText_successfully")
    void shouldSanitizeText_successfully() {
        // Given
        String input = "Regular text input";

        // When
        String result = inputSanitizer.sanitizeText(input);

        // Then
        assertNotNull(result);
    }

    // ========== sanitizeName() Tests ==========

    @Test
    @DisplayName("shouldSanitizeName_successfully")
    void shouldSanitizeName_successfully() {
        // Given
        String input = "John Doe";

        // When
        String result = inputSanitizer.sanitizeName(input);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
    }

    @Test
    @DisplayName("shouldRemoveAllHTMLFromName")
    void shouldRemoveAllHTMLFromName() {
        // Given
        String input = "John <script>alert('XSS')</script> Doe";

        // When
        String result = inputSanitizer.sanitizeName(input);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
    }
}
