package com.example.booking.domain.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.ServiceStatus;

/**
 * Unit tests for ServiceStatusConverter
 */
@DisplayName("ServiceStatusConverter Tests")
public class ServiceStatusConverterTest {

    private ServiceStatusConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ServiceStatusConverter();
    }

    // ========== convertToDatabaseColumn() Tests ==========

    @Test
    @DisplayName("shouldConvertToDatabaseColumn_successfully")
    void shouldConvertToDatabaseColumn_successfully() {
        // When
        String result = converter.convertToDatabaseColumn(ServiceStatus.AVAILABLE);

        // Then
        assertEquals("AVAILABLE", result);
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
    @DisplayName("shouldConvertToEntityAttribute_uppercase")
    void shouldConvertToEntityAttribute_uppercase() {
        // When
        ServiceStatus result = converter.convertToEntityAttribute("AVAILABLE");

        // Then
        assertEquals(ServiceStatus.AVAILABLE, result);
    }

    @Test
    @DisplayName("shouldConvertToEntityAttribute_lowercase")
    void shouldConvertToEntityAttribute_lowercase() {
        // When
        ServiceStatus result = converter.convertToEntityAttribute("available");

        // Then
        assertEquals(ServiceStatus.AVAILABLE, result);
    }

    @Test
    @DisplayName("shouldThrowException_whenInvalidValue")
    void shouldThrowException_whenInvalidValue() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute("INVALID_STATUS");
        });
    }

    @Test
    @DisplayName("shouldReturnNull_whenDbDataIsNull")
    void shouldReturnNull_whenDbDataIsNull() {
        // When
        ServiceStatus result = converter.convertToEntityAttribute(null);

        // Then
        assertNull(result);
    }
}
