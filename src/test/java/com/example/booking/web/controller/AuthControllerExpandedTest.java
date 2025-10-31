package com.example.booking.web.controller;

import com.example.booking.domain.User;
import com.example.booking.dto.*;
import com.example.booking.service.AuthRateLimitingService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Expanded comprehensive tests for AuthController
 * Covers registration, login, forgot password, reset password, change password, and profile management
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Expanded Test Suite")
class AuthControllerExpandedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimpleUserService userService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private AuthRateLimitingService authRateLimitingService;

    @MockBean
    private ImageUploadService imageUploadService;

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should show registration form")
        void testShowRegisterForm_ShouldRenderView() throws Exception {
            mockMvc.perform(get("/auth/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeExists("registerForm"));
        }

        @Test
        @DisplayName("Should register user successfully")
        void testRegisterUser_WithValidData_ShouldSucceed() throws Exception {
            doNothing().when(userService).registerUser(any(RegisterForm.class));
            doNothing().when(authRateLimitingService).resetRegisterRateLimit(anyString());

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .param("username", "testuser")
                    .param("email", "test@example.com")
                    .param("password", "Password123!")
                    .param("confirmPassword", "Password123!")
                    .param("fullName", "Test User")
                    .param("phoneNumber", "0912345678")
                    .param("accountType", "customer"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/register-success"));

            verify(userService).registerUser(any(RegisterForm.class));
        }

        @Test
        @DisplayName("Should handle validation errors in registration")
        void testRegisterUser_WithValidationErrors_ShouldReturnToForm() throws Exception {
            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .param("username", "")  // Invalid: empty
                    .param("email", "invalid-email")  // Invalid format
                    .param("password", "short")  // Too short
                    .param("confirmPassword", "short"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"));

            verify(userService, never()).registerUser(any(RegisterForm.class));
        }

        @Test
        @DisplayName("Should handle password mismatch")
        void testRegisterUser_WithPasswordMismatch_ShouldReturnError() throws Exception {
            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .param("username", "testuser")
                    .param("email", "test@example.com")
                    .param("password", "Password123!")
                    .param("confirmPassword", "Different123!")
                    .param("fullName", "Test User")
                    .param("accountType", "customer"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeExists("errorMessage"));

            verify(userService, never()).registerUser(any(RegisterForm.class));
        }

        @Test
        @DisplayName("Should handle registration service exception")
        void testRegisterUser_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            doThrow(new RuntimeException("User already exists")).when(userService)
                    .registerUser(any(RegisterForm.class));

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .param("username", "testuser")
                    .param("email", "test@example.com")
                    .param("password", "Password123!")
                    .param("confirmPassword", "Password123!")
                    .param("fullName", "Test User")
                    .param("accountType", "customer"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("Forgot Password Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should show forgot password form")
        void testShowForgotPasswordForm_ShouldRenderView() throws Exception {
            mockMvc.perform(get("/auth/forgot-password"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/forgot-password"))
                    .andExpect(model().attributeExists("forgotPasswordForm"));
        }

        @Test
        @DisplayName("Should process forgot password successfully")
        void testProcessForgotPassword_WithValidEmail_ShouldSucceed() throws Exception {
            doNothing().when(userService).sendPasswordResetToken(anyString());

            mockMvc.perform(post("/auth/forgot-password")
                    .with(csrf())
                    .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/forgot-password"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(userService).sendPasswordResetToken("test@example.com");
        }

        @Test
        @DisplayName("Should handle invalid email in forgot password")
        void testProcessForgotPassword_WithInvalidEmail_ShouldHandleError() throws Exception {
            mockMvc.perform(post("/auth/forgot-password")
                    .with(csrf())
                    .param("email", "invalid-email"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/forgot-password"));

            verify(userService, never()).sendPasswordResetToken(anyString());
        }
    }

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should show reset password form with token")
        void testShowResetPasswordForm_WithToken_ShouldRenderView() throws Exception {
            String token = "reset-token-123";

            mockMvc.perform(get("/auth/reset-password")
                    .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/reset-password"))
                    .andExpect(model().attributeExists("resetPasswordForm"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should reset password successfully")
        void testProcessResetPassword_WithValidToken_ShouldSucceed() throws Exception {
            String token = "valid-token";
            when(userService.resetPassword(eq(token), anyString())).thenReturn(true);

            mockMvc.perform(post("/auth/reset-password")
                    .with(csrf())
                    .param("token", token)
                    .param("newPassword", "NewPassword123!")
                    .param("confirmNewPassword", "NewPassword123!"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(userService).resetPassword(eq(token), anyString());
        }

        @Test
        @WithMockUser
        @DisplayName("Should handle invalid token")
        void testProcessResetPassword_WithInvalidToken_ShouldHandleError() throws Exception {
            String token = "invalid-token";
            when(userService.resetPassword(eq(token), anyString())).thenReturn(false);

            mockMvc.perform(post("/auth/reset-password")
                    .with(csrf())
                    .param("token", token)
                    .param("newPassword", "NewPassword123!")
                    .param("confirmNewPassword", "NewPassword123!"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/reset-password"))
                    .andExpect(model().attributeExists("errorMessage"));

            verify(userService).resetPassword(eq(token), anyString());
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @WithMockUser
        @DisplayName("Should show change password form")
        void testShowChangePasswordForm_ShouldRenderView() throws Exception {
            mockMvc.perform(get("/auth/change-password"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/change-password"))
                    .andExpect(model().attributeExists("changePasswordForm"));
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should change password successfully")
        void testProcessChangePassword_WithValidData_ShouldSucceed() throws Exception {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername("testuser");
            // Note: getCurrentUser is a private method in AuthController, so we mock the service chain
            // The controller calls userService.findById() internally via getCurrentUser()
            when(userService.findById(any(UUID.class))).thenReturn(user);
            when(userService.changePassword(eq(user), anyString(), anyString())).thenReturn(true);

            mockMvc.perform(post("/auth/change-password")
                    .with(csrf())
                    .param("currentPassword", "OldPassword123!")
                    .param("newPassword", "NewPassword123!")
                    .param("confirmNewPassword", "NewPassword123!"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/profile"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(userService).changePassword(any(User.class), anyString(), anyString());
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle incorrect current password")
        void testProcessChangePassword_WithIncorrectCurrentPassword_ShouldHandleError() throws Exception {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername("testuser");
            when(userService.findById(any(UUID.class))).thenReturn(user);
            when(userService.changePassword(eq(user), anyString(), anyString())).thenReturn(false);

            mockMvc.perform(post("/auth/change-password")
                    .with(csrf())
                    .param("currentPassword", "WrongPassword123!")
                    .param("newPassword", "NewPassword123!")
                    .param("confirmNewPassword", "NewPassword123!"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/change-password"))
                    .andExpect(model().attributeExists("errorMessage"));

            verify(userService).changePassword(any(User.class), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should show profile page")
        void testShowProfile_ShouldRenderView() throws Exception {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            
            when(userService.findById(any(UUID.class))).thenReturn(user);

            mockMvc.perform(get("/auth/profile"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/profile"))
                    .andExpect(model().attributeExists("user"));

            // Verify findById was called (indirectly via getCurrentUser)
            verify(userService, atLeastOnce()).findById(any(UUID.class));
        }

        @Test
        @DisplayName("Should redirect to login when not authenticated")
        void testShowProfile_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/auth/profile"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }
}

