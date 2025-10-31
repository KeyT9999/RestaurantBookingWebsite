package com.example.booking.common.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PageResponse
 */
@DisplayName("PageResponse Tests")
public class PageResponseTest {

    @Test
    @DisplayName("shouldCreatePageResponse_successfully")
    void shouldCreatePageResponse_successfully() {
        // Given
        List<String> data = new ArrayList<>();
        data.add("item1");
        data.add("item2");

        // When
        PageResponse response = new PageResponse();

        // Then
        assertNotNull(response);
    }
}

