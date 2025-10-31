package com.example.booking.dto.ai;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AISearchRequest
 */
@DisplayName("AISearchRequest Tests")
public class AISearchRequestTest {

    private AISearchRequest request;

    @BeforeEach
    void setUp() {
        request = new AISearchRequest();
    }

    @Test
    @DisplayName("shouldSetAndGetQuery_successfully")
    void shouldSetAndGetQuery_successfully() {
        // Given
        String query = "Find Italian restaurants";

        // When
        request.setQuery(query);

        // Then
        assertEquals(query, request.getQuery());
    }

    @Test
    @DisplayName("shouldHandleNullQuery")
    void shouldHandleNullQuery() {
        // When
        request.setQuery(null);

        // Then
        assertNull(request.getQuery());
    }
}

