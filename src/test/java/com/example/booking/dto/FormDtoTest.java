package com.example.booking.dto;

import com.example.booking.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Comprehensive tests for Form DTOs: RegisterForm, ProfileEditForm, ChangePasswordForm, 
 * ForgotPasswordForm, ResetPasswordForm, ProfileForm
 */
@DisplayName("Form DTOs Test Suite")
class FormDtoTest {

    // ========== RegisterForm Tests ==========
    @Nested
    @DisplayName("RegisterForm Tests")
    class RegisterFormTests {

        @Test
        @DisplayName("Should create RegisterForm with default constructor")
        void testDefaultConstructor() {
            RegisterForm form = new RegisterForm();
            assertThat(form).isNotNull();
        }

        @Test
        @DisplayName("Should create RegisterForm with constructor parameters")
        void testConstructor() {
            RegisterForm form = new RegisterForm("testuser", "test@example.com", 
                    "Password123!", "Password123!", "Test User", "0912345678");
            
            assertThat(form.getUsername()).isEqualTo("testuser");
            assertThat(form.getEmail()).isEqualTo("test@example.com");
            assertThat(form.getPassword()).isEqualTo("Password123!");
            assertThat(form.getConfirmPassword()).isEqualTo("Password123!");
            assertThat(form.getFullName()).isEqualTo("Test User");
            assertThat(form.getPhoneNumber()).isEqualTo("0912345678");
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            RegisterForm form = new RegisterForm();
            
            form.setUsername("user123");
            form.setEmail("user@example.com");
            form.setPassword("Pass123!");
            form.setConfirmPassword("Pass123!");
            form.setFullName("John Doe");
            form.setPhoneNumber("0987654321");
            form.setAddress("123 Test St");
            form.setAccountType("customer");

            assertThat(form.getUsername()).isEqualTo("user123");
            assertThat(form.getEmail()).isEqualTo("user@example.com");
            assertThat(form.getPassword()).isEqualTo("Pass123!");
            assertThat(form.getConfirmPassword()).isEqualTo("Pass123!");
            assertThat(form.getFullName()).isEqualTo("John Doe");
            assertThat(form.getPhoneNumber()).isEqualTo("0987654321");
            assertThat(form.getAddress()).isEqualTo("123 Test St");
            assertThat(form.getAccountType()).isEqualTo("customer");
        }

        @Test
        @DisplayName("Should return true when passwords match")
        void testIsPasswordMatching_WhenPasswordsMatch() {
            RegisterForm form = new RegisterForm();
            form.setPassword("Password123!");
            form.setConfirmPassword("Password123!");

            assertThat(form.isPasswordMatching()).isTrue();
        }

        @Test
        @DisplayName("Should return false when passwords do not match")
        void testIsPasswordMatching_WhenPasswordsDoNotMatch() {
            RegisterForm form = new RegisterForm();
            form.setPassword("Password123!");
            form.setConfirmPassword("Different123!");

            assertThat(form.isPasswordMatching()).isFalse();
        }

        @Test
        @DisplayName("Should return false when password is null")
        void testIsPasswordMatching_WhenPasswordIsNull() {
            RegisterForm form = new RegisterForm();
            form.setPassword(null);
            form.setConfirmPassword("Password123!");

            assertThat(form.isPasswordMatching()).isFalse();
        }

        @Test
        @DisplayName("Should resolve CUSTOMER role for customer account type")
        void testResolveRole_Customer() {
            RegisterForm form = new RegisterForm();
            form.setAccountType("customer");
            assertThat(form.resolveRole()).isEqualTo(UserRole.CUSTOMER);
        }

        @Test
        @DisplayName("Should resolve RESTAURANT_OWNER role for restaurant_owner account type")
        void testResolveRole_RestaurantOwner() {
            RegisterForm form = new RegisterForm();
            form.setAccountType("restaurant_owner");
            assertThat(form.resolveRole()).isEqualTo(UserRole.RESTAURANT_OWNER);
            
            form.setAccountType("restaurant-owner");
            assertThat(form.resolveRole()).isEqualTo(UserRole.RESTAURANT_OWNER);
            
            form.setAccountType("restaurant");
            assertThat(form.resolveRole()).isEqualTo(UserRole.RESTAURANT_OWNER);
        }

        @Test
        @DisplayName("Should return CUSTOMER as default role when account type is null")
        void testResolveRole_DefaultWhenNull() {
            RegisterForm form = new RegisterForm();
            form.setAccountType(null);
            assertThat(form.resolveRole()).isEqualTo(UserRole.CUSTOMER);
        }

        @Test
        @DisplayName("Should return CUSTOMER as default role for unknown account type")
        void testResolveRole_DefaultForUnknownType() {
            RegisterForm form = new RegisterForm();
            form.setAccountType("unknown_type");
            assertThat(form.resolveRole()).isEqualTo(UserRole.CUSTOMER);
        }
    }

    // ========== ProfileEditForm Tests ==========
    @Nested
    @DisplayName("ProfileEditForm Tests")
    class ProfileEditFormTests {

        @Test
        @DisplayName("Should create ProfileEditForm with default constructor")
        void testDefaultConstructor() {
            ProfileEditForm form = new ProfileEditForm();
            assertThat(form).isNotNull();
        }

        @Test
        @DisplayName("Should create ProfileEditForm with constructor parameters")
        void testConstructor() {
            ProfileEditForm form = new ProfileEditForm("John Doe", "0912345678", "123 Test St");
            
            assertThat(form.getFullName()).isEqualTo("John Doe");
            assertThat(form.getPhoneNumber()).isEqualTo("0912345678");
            assertThat(form.getAddress()).isEqualTo("123 Test St");
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            ProfileEditForm form = new ProfileEditForm();
            
            form.setFullName("Jane Doe");
            form.setPhoneNumber("0987654321");
            form.setAddress("456 Another St");

            assertThat(form.getFullName()).isEqualTo("Jane Doe");
            assertThat(form.getPhoneNumber()).isEqualTo("0987654321");
            assertThat(form.getAddress()).isEqualTo("456 Another St");
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            ProfileEditForm form = new ProfileEditForm();
            
            form.setFullName(null);
            form.setPhoneNumber(null);
            form.setAddress(null);

            assertThat(form.getFullName()).isNull();
            assertThat(form.getPhoneNumber()).isNull();
            assertThat(form.getAddress()).isNull();
        }
    }

    // ========== ChangePasswordForm Tests ==========
    @Nested
    @DisplayName("ChangePasswordForm Tests")
    class ChangePasswordFormTests {

        @Test
        @DisplayName("Should create ChangePasswordForm with default constructor")
        void testDefaultConstructor() {
            ChangePasswordForm form = new ChangePasswordForm();
            assertThat(form).isNotNull();
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            ChangePasswordForm form = new ChangePasswordForm();
            
            form.setCurrentPassword("OldPass123!");
            form.setNewPassword("NewPass123!");
            form.setConfirmNewPassword("NewPass123!");

            assertThat(form.getCurrentPassword()).isEqualTo("OldPass123!");
            assertThat(form.getNewPassword()).isEqualTo("NewPass123!");
            assertThat(form.getConfirmNewPassword()).isEqualTo("NewPass123!");
        }

        @Test
        @DisplayName("Should return true when new passwords match")
        void testIsNewPasswordMatching_WhenPasswordsMatch() {
            ChangePasswordForm form = new ChangePasswordForm();
            form.setNewPassword("NewPass123!");
            form.setConfirmNewPassword("NewPass123!");

            assertThat(form.isNewPasswordMatching()).isTrue();
        }

        @Test
        @DisplayName("Should return false when new passwords do not match")
        void testIsNewPasswordMatching_WhenPasswordsDoNotMatch() {
            ChangePasswordForm form = new ChangePasswordForm();
            form.setNewPassword("NewPass123!");
            form.setConfirmNewPassword("Different123!");

            assertThat(form.isNewPasswordMatching()).isFalse();
        }

        @Test
        @DisplayName("Should return false when new password is null")
        void testIsNewPasswordMatching_WhenPasswordIsNull() {
            ChangePasswordForm form = new ChangePasswordForm();
            form.setNewPassword(null);
            form.setConfirmNewPassword("NewPass123!");

            assertThat(form.isNewPasswordMatching()).isFalse();
        }
    }

    // ========== ForgotPasswordForm Tests ==========
    @Nested
    @DisplayName("ForgotPasswordForm Tests")
    class ForgotPasswordFormTests {

        @Test
        @DisplayName("Should create ForgotPasswordForm with default constructor")
        void testDefaultConstructor() {
            ForgotPasswordForm form = new ForgotPasswordForm();
            assertThat(form).isNotNull();
        }

        @Test
        @DisplayName("Should create ForgotPasswordForm with constructor parameter")
        void testConstructor() {
            ForgotPasswordForm form = new ForgotPasswordForm("test@example.com");
            
            assertThat(form.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should test getters and setters")
        void testSettersAndGetters() {
            ForgotPasswordForm form = new ForgotPasswordForm();
            
            form.setEmail("user@example.com");

            assertThat(form.getEmail()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("Should handle null email")
        void testNullEmail() {
            ForgotPasswordForm form = new ForgotPasswordForm();
            form.setEmail(null);

            assertThat(form.getEmail()).isNull();
        }
    }

    // ========== ResetPasswordForm Tests ==========
    @Nested
    @DisplayName("ResetPasswordForm Tests")
    class ResetPasswordFormTests {

        @Test
        @DisplayName("Should create ResetPasswordForm with default constructor")
        void testDefaultConstructor() {
            ResetPasswordForm form = new ResetPasswordForm();
            assertThat(form).isNotNull();
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            ResetPasswordForm form = new ResetPasswordForm();
            
            form.setToken("reset-token-123");
            form.setNewPassword("NewPass123!");
            form.setConfirmNewPassword("NewPass123!");

            assertThat(form.getToken()).isEqualTo("reset-token-123");
            assertThat(form.getNewPassword()).isEqualTo("NewPass123!");
            assertThat(form.getConfirmNewPassword()).isEqualTo("NewPass123!");
        }

        @Test
        @DisplayName("Should return true when passwords match")
        void testIsPasswordMatching_WhenPasswordsMatch() {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setNewPassword("NewPass123!");
            form.setConfirmNewPassword("NewPass123!");

            assertThat(form.isPasswordMatching()).isTrue();
        }

        @Test
        @DisplayName("Should return false when passwords do not match")
        void testIsPasswordMatching_WhenPasswordsDoNotMatch() {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setNewPassword("NewPass123!");
            form.setConfirmNewPassword("Different123!");

            assertThat(form.isPasswordMatching()).isFalse();
        }

        @Test
        @DisplayName("Should return false when new password is null")
        void testIsPasswordMatching_WhenPasswordIsNull() {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setNewPassword(null);
            form.setConfirmNewPassword("NewPass123!");

            assertThat(form.isPasswordMatching()).isFalse();
        }
    }

    // ========== ProfileForm Tests ==========
    @Nested
    @DisplayName("ProfileForm Tests")
    class ProfileFormTests {

        @Test
        @DisplayName("Should create ProfileForm with default constructor")
        void testDefaultConstructor() {
            ProfileForm form = new ProfileForm();
            assertThat(form).isNotNull();
        }

        @Test
        @DisplayName("Should create ProfileForm with constructor parameters")
        void testConstructor() {
            ProfileForm form = new ProfileForm("John Doe", "john@example.com", 
                    "0912345678", "https://example.com/image.jpg");
            
            assertThat(form.getFullName()).isEqualTo("John Doe");
            assertThat(form.getEmail()).isEqualTo("john@example.com");
            assertThat(form.getPhoneNumber()).isEqualTo("0912345678");
            assertThat(form.getCurrentProfileImageUrl()).isEqualTo("https://example.com/image.jpg");
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            ProfileForm form = new ProfileForm();
            MultipartFile mockFile = mock(MultipartFile.class);
            
            form.setFullName("Jane Doe");
            form.setEmail("jane@example.com");
            form.setPhoneNumber("0987654321");
            form.setProfileImage(mockFile);
            form.setCurrentProfileImageUrl("https://example.com/new-image.jpg");

            assertThat(form.getFullName()).isEqualTo("Jane Doe");
            assertThat(form.getEmail()).isEqualTo("jane@example.com");
            assertThat(form.getPhoneNumber()).isEqualTo("0987654321");
            assertThat(form.getProfileImage()).isEqualTo(mockFile);
            assertThat(form.getCurrentProfileImageUrl()).isEqualTo("https://example.com/new-image.jpg");
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            ProfileForm form = new ProfileForm();
            
            form.setFullName(null);
            form.setEmail(null);
            form.setPhoneNumber(null);
            form.setProfileImage(null);
            form.setCurrentProfileImageUrl(null);

            assertThat(form.getFullName()).isNull();
            assertThat(form.getEmail()).isNull();
            assertThat(form.getPhoneNumber()).isNull();
            assertThat(form.getProfileImage()).isNull();
            assertThat(form.getCurrentProfileImageUrl()).isNull();
        }
    }
}

