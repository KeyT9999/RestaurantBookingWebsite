package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserRoleAttributeConverter
 */
@DisplayName("UserRoleAttributeConverter Tests")
public class UserRoleAttributeConverterTest {

    private UserRoleAttributeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new UserRoleAttributeConverter();
    }

    // ========== convertToDatabaseColumn() Tests ==========

    @Test
    @DisplayName("shouldConvertToDatabaseColumn_successfully")
    void shouldConvertToDatabaseColumn_successfully() {
        // When
        String result = converter.convertToDatabaseColumn(UserRole.ADMIN);

        // Then
        assertEquals("admin", result);
    }

    @Test
    @DisplayName("shouldReturnNull_whenAttributeIsNull")
    void shouldReturnNull_whenAttributeIsNull() {
        // When
        String result = converter.convertToDatabaseColumn(null);

        // Then
        assertNull(result);
    }

    // ========== convertToEntityAttribute() Tests ==========

    @Test
    @DisplayName("shouldConvertToEntityAttribute_lowercase")
    void shouldConvertToEntityAttribute_lowercase() {
        // When
        UserRole result = converter.convertToEntityAttribute("customer");

        // Then
        assertEquals(UserRole.CUSTOMER, result);
    }

    @Test
    @DisplayName("shouldConvertToEntityAttribute_uppercase")
    void shouldConvertToEntityAttribute_uppercase() {
        // When
        UserRole result = converter.convertToEntityAttribute("CUSTOMER");

        // Then
        assertEquals(UserRole.CUSTOMER, result);
    }

    @Test
    @DisplayName("shouldConvertToEntityAttribute_restaurantOwner")
    void shouldConvertToEntityAttribute_restaurantOwner() {
        // When
        UserRole result = converter.convertToEntityAttribute("restaurant_owner");

        // Then
        assertEquals(UserRole.RESTAURANT_OWNER, result);
    }

    @Test
    @DisplayName("shouldThrowException_whenInvalidValue")
    void shouldThrowException_whenInvalidValue() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute("invalid_role");
        });
    }

    @Test
    @DisplayName("shouldReturnNull_whenDbDataIsNull")
    void shouldReturnNull_whenDbDataIsNull() {
        // When
        UserRole result = converter.convertToEntityAttribute(null);

        // Then
        assertNull(result);
    }
}

