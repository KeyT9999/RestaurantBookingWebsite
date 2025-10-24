package com.example.booking.test.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Simple test to verify JUnit setup works
 */
class SimpleBookingTest {

    @Test
    void testBasicAssertion() {
        // Given
        String expected = "Hello World";
        
        // When
        String actual = "Hello World";
        
        // Then
        assertEquals(expected, actual);
        assertTrue(true);
        assertFalse(false);
    }

    @Test
    void testMathCalculation() {
        // Given
        int a = 5;
        int b = 3;
        
        // When
        int sum = a + b;
        int product = a * b;
        
        // Then
        assertEquals(8, sum);
        assertEquals(15, product);
    }

    @Test
    void testStringOperations() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        
        // When
        String fullName = firstName + " " + lastName;
        
        // Then
        assertEquals("John Doe", fullName);
        assertTrue(fullName.contains("John"));
        assertTrue(fullName.contains("Doe"));
    }
}
