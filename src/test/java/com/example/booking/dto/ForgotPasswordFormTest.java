package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ForgotPasswordForm DTO
 */
@DisplayName("ForgotPasswordForm DTO Tests")
public class ForgotPasswordFormTest {

    private ForgotPasswordForm forgotPasswordForm;

    @BeforeEach
    void setUp() {
        forgotPasswordForm = new ForgotPasswordForm();
    }

    @Test
    @DisplayName("shouldSetAndGetEmail_successfully")
    void shouldSetAndGetEmail_successfully() {
        // Given
        String email = "test@test.com";

        // When
        forgotPasswordForm.setEmail(email);

        // Then
        assertEquals(email, forgotPasswordForm.getEmail());
    }
}

