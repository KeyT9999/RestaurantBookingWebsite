package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.User;
import com.example.booking.dto.RegisterForm;
import com.example.booking.dto.ForgotPasswordForm;
import com.example.booking.dto.ResetPasswordForm;
import com.example.booking.service.SimpleUserService;

/**
 * Unit tests for AuthController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
public class AuthControllerTest {

    @Mock
    private SimpleUserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AuthController controller;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
    }

    // ========== showRegisterForm() Tests ==========

    @Test
    @DisplayName("shouldShowRegisterForm_successfully")
    void shouldShowRegisterForm_successfully() {
        // When
        String view = controller.showRegisterForm(model);

        // Then
        assertEquals("auth/register", view);
        verify(model, times(1)).addAttribute(eq("registerForm"), any(RegisterForm.class));
    }

    // ========== registerUser() Tests ==========

    @Test
    @DisplayName("shouldRegisterUser_successfully")
    void shouldRegisterUser_successfully() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setUsername("newuser");
        form.setEmail("newuser@example.com");
        form.setPassword("password123");
        form.setConfirmPassword("password123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(RegisterForm.class))).thenReturn(user);

        // When
        String view = controller.registerUser(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("redirect:/auth/register-success", view);
        verify(userService, times(1)).registerUser(any(RegisterForm.class));
    }

    @Test
    @DisplayName("shouldReturnForm_whenValidationErrors")
    void shouldReturnForm_whenValidationErrors() {
        // Given
        RegisterForm form = new RegisterForm();

        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String view = controller.registerUser(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("auth/register", view);
        verify(userService, never()).registerUser(any(RegisterForm.class));
    }

    // ========== showForgotPasswordForm() Tests ==========

    @Test
    @DisplayName("shouldShowForgotPasswordForm_successfully")
    void shouldShowForgotPasswordForm_successfully() {
        // When
        String view = controller.showForgotPasswordForm(model);

        // Then
        assertEquals("auth/forgot-password", view);
        verify(model, times(1)).addAttribute(eq("forgotPasswordForm"), any(ForgotPasswordForm.class));
    }

    // ========== processForgotPassword() Tests ==========

    @Test
    @DisplayName("shouldProcessForgotPassword_successfully")
    void shouldProcessForgotPassword_successfully() {
        // Given
        ForgotPasswordForm form = new ForgotPasswordForm();
        form.setEmail("test@example.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        doNothing().when(userService).sendPasswordResetToken("test@example.com");

        // When
        String view = controller.processForgotPassword(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("redirect:/auth/forgot-password", view);
        verify(userService, times(1)).sendPasswordResetToken("test@example.com");
    }

    // ========== showResetPasswordForm() Tests ==========

    @Test
    @DisplayName("shouldShowResetPasswordForm_successfully")
    void shouldShowResetPasswordForm_successfully() {
        // Given
        String token = "valid-token";

        // When
        String view = controller.showResetPasswordForm(token, model);

        // Then
        assertEquals("auth/reset-password", view);
        verify(model, times(1)).addAttribute(eq("resetPasswordForm"), any(ResetPasswordForm.class));
    }

    // ========== processResetPassword() Tests ==========

    @Test
    @DisplayName("shouldResetPassword_successfully")
    void shouldResetPassword_successfully() {
        // Given
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken("valid-token");
        form.setNewPassword("newpassword123");
        form.setConfirmNewPassword("newpassword123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.resetPassword("valid-token", "newpassword123")).thenReturn(true);

        // When
        String view = controller.processResetPassword(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("redirect:/login", view);
        verify(userService, times(1)).resetPassword("valid-token", "newpassword123");
    }
}
