package com.example.booking.common.base;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BaseEntity
 */
@DisplayName("BaseEntity Tests")
public class BaseEntityTest {

    @Test
    @DisplayName("shouldExist_successfully")
    void shouldExist_successfully() {
        // Given
        BaseEntity entity = new BaseEntity();

        // When & Then
        assertNotNull(entity);
        // BaseEntity is currently empty, but tests ensure class can be instantiated
    }
}

