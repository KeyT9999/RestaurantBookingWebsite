package com.example.booking.test.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    // ==================== EDGE CASES & ERROR HANDLING ====================

    @Test
    void testMathCalculation_WithNegativeNumbers() {
        // Given
        int a = -5;
        int b = -3;
        
        // When
        int sum = a + b;
        int product = a * b;
        
        // Then
        assertEquals(-8, sum);
        assertEquals(15, product);
    }

    @Test
    void testMathCalculation_WithZero() {
        // Given
        int a = 0;
        int b = 5;
        
        // When
        int sum = a + b;
        int product = a * b;
        
        // Then
        assertEquals(5, sum);
        assertEquals(0, product);
    }

    @Test
    void testStringOperations_WithEmptyString() {
        // Given
        String firstName = "";
        String lastName = "Doe";
        
        // When
        String fullName = firstName + " " + lastName;
        
        // Then
        assertEquals(" Doe", fullName);
        assertTrue(fullName.contains("Doe"));
        assertFalse(fullName.contains("John"));
    }

    @Test
    void testStringOperations_WithNull() {
        // Given
        String firstName = null;
        String lastName = "Doe";
        
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            String fullName = firstName.trim() + " " + lastName;
        });
    }
}
