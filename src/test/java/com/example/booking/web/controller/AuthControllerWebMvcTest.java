package com.example.booking.web.controller;

import com.example.booking.domain.User;
import com.example.booking.service.AuthRateLimitingService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController WebMvc Integration Tests")
class AuthControllerWebMvcTest {

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

    @MockBean
    private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

    @MockBean
    private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        // Note: isRegisterAllowed needs HttpServletRequest/Response, but filters are disabled in WebMvcTest
        // so this won't be called in tests
    }

    // ========== GET /auth/register ==========

    @Test
    @DisplayName("GET /auth/register - should show register form")
    void testShowRegisterForm() throws Exception {
        mockMvc.perform(get("/auth/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"))
            .andExpect(model().attributeExists("registerForm"));
    }

    // ========== POST /auth/register ==========

    @Test
    @DisplayName("POST /auth/register - valid data should register successfully")
    void testRegisterUser_Success() throws Exception {
        // Given
        when(userService.registerUser(any())).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .param("username", "newuser")
                .param("email", "new@test.com")
                .param("password", "Password123!")
                .param("confirmPassword", "Password123!")
                .param("fullName", "New User")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/auth/register-success"));
    }

    @Test
    @DisplayName("POST /auth/register - password mismatch should show error")
    void testRegisterUser_PasswordMismatch() throws Exception {
        mockMvc.perform(post("/auth/register")
                .param("username", "newuser")
                .param("email", "new@test.com")
                .param("password", "Password123!")
                .param("confirmPassword", "DifferentPass!")
                .param("fullName", "New User")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    // ========== GET /auth/register-success ==========

    @Test
    @DisplayName("GET /auth/register-success - should show success page")
    void testShowRegisterSuccess() throws Exception {
        mockMvc.perform(get("/auth/register-success"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register-success"));
    }

    // ========== GET /auth/verify-email ==========

    @Test
    @DisplayName("GET /auth/verify-email - valid token should verify")
    void testVerifyEmail_Success() throws Exception {
        // Given
        when(userService.verifyEmail("valid-token")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/auth/verify-email").param("token", "valid-token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("GET /auth/verify-email - invalid token should show error")
    void testVerifyEmail_Invalid() throws Exception {
        // Given
        when(userService.verifyEmail("invalid-token")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/auth/verify-email").param("token", "invalid-token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/auth/verify-result"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    // ========== GET /auth/forgot-password ==========

    @Test
    @DisplayName("GET /auth/forgot-password - should show form")
    void testShowForgotPasswordForm() throws Exception {
        mockMvc.perform(get("/auth/forgot-password"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/forgot-password"))
            .andExpect(model().attributeExists("forgotPasswordForm"));
    }

    // ========== POST /auth/forgot-password ==========

    @Test
    @DisplayName("POST /auth/forgot-password - should send reset email")
    void testProcessForgotPassword_Success() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                .param("email", "test@example.com")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/auth/forgot-password"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(userService).sendPasswordResetToken("test@example.com");
    }

    // ========== GET /auth/reset-password ==========

    @Test
    @DisplayName("GET /auth/reset-password - should show reset form")
    void testShowResetPasswordForm() throws Exception {
        mockMvc.perform(get("/auth/reset-password").param("token", "test-token"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/reset-password"))
            .andExpect(model().attributeExists("resetPasswordForm"));
    }

    // ========== POST /auth/reset-password ==========

    @Test
    @DisplayName("POST /auth/reset-password - valid should reset password")
    void testProcessResetPassword_Success() throws Exception {
        // Given
        when(userService.resetPassword("valid-token", "NewPass123!")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/reset-password")
                .param("token", "valid-token")
                .param("newPassword", "NewPass123!")
                .param("confirmNewPassword", "NewPass123!")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    // ========== GET /auth/profile ==========

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /auth/profile - should show user profile")
    void testShowProfile() throws Exception {
        // Given
        when(userService.findById(any())).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/auth/profile"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/profile"))
            .andExpect(model().attributeExists("user"));
    }

    // ========== GET /auth/profile/edit ==========

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /auth/profile/edit - should show edit form")
    void testShowEditProfileForm() throws Exception {
        // Given
        when(userService.findById(any())).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/auth/profile/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/profile-edit"))
            .andExpect(model().attributeExists("profileEditForm"));
    }

    // ========== POST /auth/profile/edit ==========

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /auth/profile/edit - should update profile")
    void testProcessEditProfile_Success() throws Exception {
        // Given
        when(userService.findById(any())).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/auth/profile/edit")
                .param("fullName", "Updated Name")
                .param("phoneNumber", "0123456789")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/auth/profile"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    // ========== POST /auth/profile/avatar ==========

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /auth/profile/avatar - should upload avatar")
    void testUploadAvatar_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("profileImage", "avatar.jpg", 
            "image/jpeg", "test image content".getBytes());
        when(userService.findById(any())).thenReturn(user);
        when(imageUploadService.uploadAvatar(any(), anyString())).thenReturn("https://cloudinary.com/avatar.jpg");

        // When & Then
        mockMvc.perform(multipart("/auth/profile/avatar")
                .file(file)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/auth/profile"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    // ========== GET /auth/oauth-account-type ==========

    @Test
    @DisplayName("GET /auth/oauth-account-type - should show account type selection")
    void testShowOAuthAccountTypeSelection() throws Exception {
        mockMvc.perform(get("/auth/oauth-account-type"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/oauth-account-type"));
    }

    // ========== POST /auth/oauth-complete ==========

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /auth/oauth-complete - should complete OAuth registration")
    void testCompleteOAuthRegistration() throws Exception {
        // Given
        when(userService.findById(any())).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/auth/oauth-complete")
                .param("accountType", "customer")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }
}

