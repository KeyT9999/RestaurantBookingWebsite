package com.example.booking.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test for BookingMapper
 * Coverage: 100% (constructor only)
 */
@DisplayName("BookingMapper Tests")
class BookingMapperTest {

    @Test
    @DisplayName("shouldInstantiateMapper")
    void shouldInstantiateMapper() {
        // When - Instantiate the mapper
        BookingMapper mapper = new BookingMapper();
        
        // Then - Assert it's not null (covers constructor)
        assertNotNull(mapper);
    }
}

