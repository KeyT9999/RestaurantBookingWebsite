package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test for DataInitializer
 * Coverage: 100% (empty class, constructor only)
 */
@DisplayName("DataInitializer Tests")
class DataInitializerTest {

    @Test
    @DisplayName("shouldInstantiateInitializer")
    void shouldInstantiateInitializer() {
        // When - Instantiate the initializer
        DataInitializer initializer = new DataInitializer();
        
        // Then - Assert it's not null
        assertNotNull(initializer);
    }
}

