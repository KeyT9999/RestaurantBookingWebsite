package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ChangePasswordForm DTO
 */
@DisplayName("ChangePasswordForm DTO Tests")
public class ChangePasswordFormTest {

    private ChangePasswordForm changePasswordForm;

    @BeforeEach
    void setUp() {
        changePasswordForm = new ChangePasswordForm();
    }

    @Test
    @DisplayName("shouldSetAndGetCurrentPassword_successfully")
    void shouldSetAndGetCurrentPassword_successfully() {
        // Given
        String password = "currentPassword123";

        // When
        changePasswordForm.setCurrentPassword(password);

        // Then
        assertEquals(password, changePasswordForm.getCurrentPassword());
    }

    @Test
    @DisplayName("shouldSetAndGetNewPassword_successfully")
    void shouldSetAndGetNewPassword_successfully() {
        // Given
        String password = "newPassword123";

        // When
        changePasswordForm.setNewPassword(password);

        // Then
        assertEquals(password, changePasswordForm.getNewPassword());
    }
}

