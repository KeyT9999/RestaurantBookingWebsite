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
import com.example.booking.domain.UserRole;
import com.example.booking.dto.RegisterForm;
import com.example.booking.dto.ForgotPasswordForm;
import com.example.booking.dto.ResetPasswordForm;
import com.example.booking.dto.ChangePasswordForm;
import com.example.booking.dto.ProfileEditForm;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.AuthRateLimitingService;
import com.example.booking.service.ImageUploadService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Optional;

/**
 * Unit tests for AuthController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
public class AuthControllerTest {

    @Mock
    private SimpleUserService userService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private AuthRateLimitingService authRateLimitingService;

    @Mock
    private ImageUploadService imageUploadService;

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

    @Test
    @DisplayName("shouldReturnForm_whenPasswordMismatch")
    void shouldReturnForm_whenPasswordMismatch() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setPassword("password123");
        form.setConfirmPassword("different");

        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        String view = controller.registerUser(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("auth/register", view);
        verify(userService, never()).registerUser(any(RegisterForm.class));
    }

    @Test
    @DisplayName("shouldHandleException_whenRegistrationFails")
    void shouldHandleException_whenRegistrationFails() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setPassword("password123");
        form.setConfirmPassword("password123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(RegisterForm.class))).thenThrow(new RuntimeException("Registration failed"));

        // When
        String view = controller.registerUser(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("auth/register", view);
        verify(model, times(1)).addAttribute(eq("errorMessage"), anyString());
    }

    // ========== Restaurant Registration Tests ==========

    @Test
    @DisplayName("shouldShowRestaurantRegisterForm_successfully")
    void shouldShowRestaurantRegisterForm_successfully() {
        // When
        String view = controller.showRestaurantRegisterForm(model);

        // Then
        assertEquals("auth/register-restaurant", view);
        verify(model, times(1)).addAttribute(eq("registerForm"), any(RegisterForm.class));
        verify(model, times(1)).addAttribute(eq("isRestaurantRegistration"), eq(true));
    }

    @Test
    @DisplayName("shouldRegisterRestaurantOwner_successfully")
    void shouldRegisterRestaurantOwner_successfully() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setPassword("password123");
        form.setConfirmPassword("password123");
        User restaurantOwner = new User();
        restaurantOwner.setRole(UserRole.RESTAURANT_OWNER);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(RegisterForm.class), eq(UserRole.RESTAURANT_OWNER)))
            .thenReturn(restaurantOwner);
        doNothing().when(restaurantOwnerService).ensureRestaurantOwnerExists(any(UUID.class));

        // When
        String view = controller.registerRestaurantOwner(form, bindingResult, true, model, redirectAttributes);

        // Then
        assertEquals("redirect:/auth/register-success", view);
        verify(userService, times(1)).registerUser(any(RegisterForm.class), eq(UserRole.RESTAURANT_OWNER));
        verify(restaurantOwnerService, times(1)).ensureRestaurantOwnerExists(any(UUID.class));
    }

    @Test
    @DisplayName("shouldReturnError_whenTermsNotAccepted")
    void shouldReturnError_whenTermsNotAccepted() {
        // Given
        RegisterForm form = new RegisterForm();

        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        String view = controller.registerRestaurantOwner(form, bindingResult, false, model, redirectAttributes);

        // Then
        assertEquals("auth/register-restaurant", view);
        verify(model, times(1)).addAttribute(eq("errorMessage"), anyString());
        verify(userService, never()).registerUser(any(RegisterForm.class), any(UserRole.class));
    }

    // ========== Email Verification Tests ==========

    @Test
    @DisplayName("shouldVerifyEmail_successfully")
    void shouldVerifyEmail_successfully() {
        // Given
        String token = "valid-token";
        when(userService.verifyEmail(token)).thenReturn(true);

        // When
        String view = controller.verifyEmail(token, redirectAttributes);

        // Then
        assertEquals("redirect:/login", view);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    @DisplayName("shouldHandleInvalidToken")
    void shouldHandleInvalidToken() {
        // Given
        String token = "invalid-token";
        when(userService.verifyEmail(token)).thenReturn(false);

        // When
        String view = controller.verifyEmail(token, redirectAttributes);

        // Then
        assertEquals("redirect:/auth/verify-result", view);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldShowVerifyResult_successfully")
    void shouldShowVerifyResult_successfully() {
        // When
        String view = controller.showVerifyResult();

        // Then
        assertEquals("auth/verify-result", view);
    }

    @Test
    @DisplayName("shouldShowRegisterSuccess_successfully")
    void shouldShowRegisterSuccess_successfully() {
        // When
        String view = controller.showRegisterSuccess();

        // Then
        assertEquals("auth/register-success", view);
    }

    // ========== Change Password Tests ==========

    @Test
    @DisplayName("shouldShowChangePasswordForm_successfully")
    void shouldShowChangePasswordForm_successfully() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);

        // When
        String view = controller.showChangePasswordForm(model, authentication);

        // Then
        assertEquals("auth/change-password", view);
        verify(model, times(1)).addAttribute(eq("changePasswordForm"), any(ChangePasswordForm.class));
    }

    @Test
    @DisplayName("shouldRedirectToLogin_whenNotAuthenticatedForChangePassword")
    void shouldRedirectToLogin_whenNotAuthenticatedForChangePassword() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        String view = controller.showChangePasswordForm(model, authentication);

        // Then
        assertEquals("redirect:/login", view);
    }

    @Test
    @DisplayName("shouldChangePassword_successfully")
    void shouldChangePassword_successfully() {
        // Given
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("oldpass");
        form.setNewPassword("newpass123");
        form.setConfirmNewPassword("newpass123");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userService.findById(any(UUID.class))).thenReturn(user);
        when(userService.changePassword(any(User.class), eq("oldpass"), eq("newpass123"))).thenReturn(true);

        // When
        String view = controller.processChangePassword(form, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/auth/profile", view);
        verify(userService, times(1)).changePassword(any(User.class), eq("oldpass"), eq("newpass123"));
    }

    // ========== Profile Tests ==========

    @Test
    @DisplayName("shouldShowProfile_successfully")
    void shouldShowProfile_successfully() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userService.findById(any(UUID.class))).thenReturn(user);

        // When
        String view = controller.showProfile(model, authentication);

        // Then
        assertEquals("auth/profile", view);
        verify(model, times(1)).addAttribute(eq("user"), eq(user));
    }

    @Test
    @DisplayName("shouldShowEditProfileForm_successfully")
    void shouldShowEditProfileForm_successfully() {
        // Given
        user.setFullName("Test User");
        user.setPhoneNumber("0123456789");
        user.setAddress("Test Address");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userService.findById(any(UUID.class))).thenReturn(user);

        // When
        String view = controller.showEditProfileForm(model, authentication);

        // Then
        assertEquals("auth/profile-edit", view);
        verify(model, times(1)).addAttribute(eq("profileEditForm"), any(ProfileEditForm.class));
    }

    @Test
    @DisplayName("shouldProcessEditProfile_successfully")
    void shouldProcessEditProfile_successfully() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setFullName("Updated Name");
        form.setPhoneNumber("0987654321");
        form.setAddress("New Address");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userService.findById(any(UUID.class))).thenReturn(user);
        doNothing().when(userService).updateProfile(any(User.class), any(ProfileEditForm.class));

        // When
        String view = controller.processEditProfile(form, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/auth/profile", view);
        verify(userService, times(1)).updateProfile(any(User.class), any(ProfileEditForm.class));
    }

    // ========== Avatar Upload Tests ==========

    @Test
    @DisplayName("shouldUploadAvatar_successfully")
    void shouldUploadAvatar_successfully() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile("profileImage", "avatar.jpg", 
            "image/jpeg", "test content".getBytes());

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userService.findById(any(UUID.class))).thenReturn(user);
        when(imageUploadService.uploadAvatar(any(MultipartFile.class), anyInt()))
            .thenReturn("http://example.com/avatar.jpg");
        doNothing().when(userService).updateProfileImage(any(User.class), anyString());

        // When
        String view = controller.uploadAvatar(file, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/auth/profile", view);
        verify(imageUploadService, times(1)).uploadAvatar(any(MultipartFile.class), anyInt());
        verify(userService, times(1)).updateProfileImage(any(User.class), anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenFileEmpty")
    void shouldReturnError_whenFileEmpty() {
        // Given
        MultipartFile file = new MockMultipartFile("profileImage", "", "", new byte[0]);
        when(authentication.isAuthenticated()).thenReturn(true);

        // When
        String view = controller.uploadAvatar(file, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/auth/profile", view);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // ========== OAuth Tests ==========

    @Test
    @DisplayName("shouldShowOAuthAccountTypeSelection_successfully")
    void shouldShowOAuthAccountTypeSelection_successfully() {
        // When
        String view = controller.showOAuthAccountTypeSelection();

        // Then
        assertEquals("auth/oauth-account-type", view);
    }

    @Test
    @DisplayName("shouldCompleteOAuthRegistration_successfully")
    void shouldCompleteOAuthRegistration_successfully() {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("oauth@example.com");
        when(userService.findByEmail("oauth@example.com")).thenReturn(Optional.of(user));
        when(userService.findById(any(UUID.class))).thenReturn(user);
        doNothing().when(userService).updateUserRole(any(User.class), any(UserRole.class));
        doNothing().when(userService).createRestaurantOwnerIfNeeded(any(User.class));

        // When
        String view = controller.completeOAuthRegistration("customer", authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/", view);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    @DisplayName("shouldCompleteOAuthRegistration_RestaurantOwner")
    void shouldCompleteOAuthRegistration_RestaurantOwner() {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("owner@example.com");
        when(userService.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
        when(userService.findById(any(UUID.class))).thenReturn(user);
        doNothing().when(userService).updateUserRole(any(User.class), eq(UserRole.RESTAURANT_OWNER));
        doNothing().when(userService).createRestaurantOwnerIfNeeded(any(User.class));

        // When
        String view = controller.completeOAuthRegistration("restaurant_owner", authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/", view);
        verify(userService, times(1)).updateUserRole(any(User.class), eq(UserRole.RESTAURANT_OWNER));
        verify(userService, times(1)).createRestaurantOwnerIfNeeded(any(User.class));
    }

    @Test
    @DisplayName("shouldHandleException_whenForgotPasswordFails")
    void shouldHandleException_whenForgotPasswordFails() {
        // Given
        ForgotPasswordForm form = new ForgotPasswordForm();
        form.setEmail("test@example.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("Service error")).when(userService).sendPasswordResetToken("test@example.com");

        // When
        String view = controller.processForgotPassword(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("auth/forgot-password", view);
        verify(model, times(1)).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldHandleException_whenResetPasswordFails")
    void shouldHandleException_whenResetPasswordFails() {
        // Given
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken("token");
        form.setNewPassword("newpass123");
        form.setConfirmNewPassword("newpass123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.resetPassword("token", "newpass123")).thenThrow(new RuntimeException("Service error"));

        // When
        String view = controller.processResetPassword(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("auth/reset-password", view);
        verify(model, times(1)).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenResetPasswordTokenInvalid")
    void shouldReturnError_whenResetPasswordTokenInvalid() {
        // Given
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken("invalid-token");
        form.setNewPassword("newpass123");
        form.setConfirmNewPassword("newpass123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.resetPassword("invalid-token", "newpass123")).thenReturn(false);

        // When
        String view = controller.processResetPassword(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("auth/reset-password", view);
        verify(model, times(1)).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenPasswordMismatchInReset")
    void shouldReturnError_whenPasswordMismatchInReset() {
        // Given
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken("token");
        form.setNewPassword("newpass123");
        form.setConfirmNewPassword("different");

        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        String view = controller.processResetPassword(form, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("auth/reset-password", view);
        verify(model, times(1)).addAttribute(eq("errorMessage"), anyString());
        verify(userService, never()).resetPassword(anyString(), anyString());
    }
}
