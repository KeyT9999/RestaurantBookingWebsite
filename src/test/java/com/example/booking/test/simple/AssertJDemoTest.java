package com.example.booking.test.simple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 * Demo test để showcase AssertJ assertions
 */
class AssertJDemoTest {

    @Test
    void testAssertJBasicAssertions() {
        // Given
        String name = "John Doe";
        int age = 25;
        String[] hobbies = {"coding", "reading", "gaming"};
        
        // When & Then - AssertJ assertions
        assertThat(name)
            .isNotNull()
            .isNotEmpty()
            .contains("John")
            .startsWith("John")
            .endsWith("Doe");
            
        assertThat(age)
            .isGreaterThan(18)
            .isLessThan(100)
            .isBetween(20, 30);
            
        assertThat(hobbies)
            .hasSize(3)
            .contains("coding", "reading")
            .doesNotContain("sleeping")
            .startsWith("coding");
    }
    
    @Test
    void testAssertJExceptionHandling() {
        // Given
        Calculator calculator = new Calculator();
        
        // When & Then - Exception assertions
        assertThatThrownBy(() -> calculator.divide(10, 0))
            .isInstanceOf(ArithmeticException.class)
            .hasMessageContaining("by zero");
            
        assertThatCode(() -> calculator.add(5, 3))
            .doesNotThrowAnyException();
    }
    
    @Test
    void testAssertJCollectionAssertions() {
        // Given
        java.util.List<String> fruits = java.util.Arrays.asList("apple", "banana", "cherry");
        
        // When & Then - Collection assertions
        assertThat(fruits)
            .hasSize(3)
            .containsExactly("apple", "banana", "cherry")
            .containsOnly("banana", "cherry", "apple")
            .doesNotHaveDuplicates();
    }
    
    // Helper class for testing
    static class Calculator {
        public int add(int a, int b) {
            return a + b;
        }
        
        public int divide(int a, int b) {
            if (b == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return a / b;
        }
    }
}
