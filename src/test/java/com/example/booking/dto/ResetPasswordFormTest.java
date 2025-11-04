package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ResetPasswordForm DTO
 */
@DisplayName("ResetPasswordForm DTO Tests")
public class ResetPasswordFormTest {

    private ResetPasswordForm resetPasswordForm;

    @BeforeEach
    void setUp() {
        resetPasswordForm = new ResetPasswordForm();
    }

    @Test
    @DisplayName("shouldSetAndGetToken_successfully")
    void shouldSetAndGetToken_successfully() {
        // Given
        String token = "reset-token-123";

        // When
        resetPasswordForm.setToken(token);

        // Then
        assertEquals(token, resetPasswordForm.getToken());
    }

    @Test
    @DisplayName("shouldSetAndGetNewPassword_successfully")
    void shouldSetAndGetNewPassword_successfully() {
        // Given
        String password = "newPassword123";

        // When
        resetPasswordForm.setNewPassword(password);

        // Then
        assertEquals(password, resetPasswordForm.getNewPassword());
    }
}

