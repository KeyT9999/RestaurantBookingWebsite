package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RegisterForm DTO
 */
@DisplayName("RegisterForm DTO Tests")
public class RegisterFormTest {

    private RegisterForm registerForm;

    @BeforeEach
    void setUp() {
        registerForm = new RegisterForm();
    }

    @Test
    @DisplayName("shouldSetAndGetEmail_successfully")
    void shouldSetAndGetEmail_successfully() {
        // Given
        String email = "test@test.com";

        // When
        registerForm.setEmail(email);

        // Then
        assertEquals(email, registerForm.getEmail());
    }

    @Test
    @DisplayName("shouldSetAndGetPassword_successfully")
    void shouldSetAndGetPassword_successfully() {
        // Given
        String password = "password123";

        // When
        registerForm.setPassword(password);

        // Then
        assertEquals(password, registerForm.getPassword());
    }
}

