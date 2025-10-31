package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.dto.ProfileEditForm;
import com.example.booking.dto.RegisterForm;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@DisplayName("SimpleUserService Tests")
public class SimpleUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RestaurantOwnerRepository restaurantOwnerRepository;

    @InjectMocks
    private SimpleUserService simpleUserService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhoneNumber("0123456789");
        testUser.setAddress("Test Address");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setEmailVerified(true);
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    // ========== updateProfile() Tests ==========

    @Test
    @DisplayName("Should update user fields with valid data")
    public void testUpdateProfile_WithValidData_ShouldUpdateUserFields() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setFullName("Updated Name");
        form.setPhoneNumber("0987654321");
        form.setAddress("Updated Address");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });


        // When
        User result = simpleUserService.updateProfile(testUser, form);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getFullName());
        assertEquals("0987654321", result.getPhoneNumber());
        assertEquals("Updated Address", result.getAddress());
        assertNotNull(result.getUpdatedAt());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update only provided fields with partial data")
    public void testUpdateProfile_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setFullName("New Name Only");
        form.setPhoneNumber(null);
        form.setAddress(null);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });


        // When
        User result = simpleUserService.updateProfile(testUser, form);

        // Then
        assertNotNull(result);
        assertEquals("New Name Only", result.getFullName());
        assertEquals(null, result.getPhoneNumber());
        assertEquals(null, result.getAddress());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update other fields when fullName is empty")
    public void testUpdateProfile_WithEmptyFullName_ShouldStillUpdateOtherFields() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setFullName("");
        form.setPhoneNumber("0999999999");
        form.setAddress("New Address");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });

        // When
        User result = simpleUserService.updateProfile(testUser, form);

        // Then
        assertNotNull(result);
        assertEquals("", result.getFullName());
        assertEquals("0999999999", result.getPhoneNumber());
        assertEquals("New Address", result.getAddress());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update with special characters")
    public void testUpdateProfile_WithSpecialCharacters_ShouldUpdateSuccessfully() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setFullName("Nguyễn Văn A");
        form.setPhoneNumber("0123456789");
        form.setAddress("123 Đường ABC");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });

        // When
        User result = simpleUserService.updateProfile(testUser, form);

        // Then
        assertNotNull(result);
        assertEquals("Nguyễn Văn A", result.getFullName());
        assertEquals("123 Đường ABC", result.getAddress());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update with long address")
    public void testUpdateProfile_WithLongAddress_ShouldUpdateSuccessfully() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setFullName("Test User");
        form.setPhoneNumber("0123456789");
        String longAddress = "This is a very long address that exceeds 200 characters to test the system's ability to handle long addresses in the profile update functionality with additional text.";
        form.setAddress(longAddress);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });

        // When
        User result = simpleUserService.updateProfile(testUser, form);

        // Then
        assertNotNull(result);
        assertEquals(longAddress, result.getAddress());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should save to database")
    public void testUpdateProfile_ShouldSaveToDatabase() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setFullName("Database Test");
        form.setPhoneNumber("0123456789");
        form.setAddress("Test Address");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });

        // When
        User result = simpleUserService.updateProfile(testUser, form);

        // Then
        assertNotNull(result);
        assertEquals("Database Test", result.getFullName());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update timestamp even with same values")
    public void testUpdateProfile_WithSameValues_ShouldStillUpdateTimestamp() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setFullName(testUser.getFullName());
        form.setPhoneNumber(testUser.getPhoneNumber());
        form.setAddress(testUser.getAddress());


        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUpdatedAt(LocalDateTime.now());
            return u;
        });

        // When
        User result = simpleUserService.updateProfile(testUser, form);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUpdatedAt());
        verify(userRepository).save(testUser);
    }

    // ========== loadUserByUsername() Tests ==========

    @Test
    @DisplayName("Should load user by username successfully")
    public void testLoadUserByUsername_WithValidUsername_ShouldReturnUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsernameIgnoreCase(username))
                .thenReturn(java.util.Optional.of(testUser));

        // When
        org.springframework.security.core.userdetails.UserDetails result = simpleUserService
                .loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository).findByUsernameIgnoreCase(username);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    public void testLoadUserByUsername_WithNonExistentUsername_ShouldThrowException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsernameIgnoreCase(username))
                .thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            simpleUserService.loadUserByUsername(username);
        });
        verify(userRepository).findByUsernameIgnoreCase(username);
    }

    // ========== registerUser() Tests ==========

    @Test
    @DisplayName("Should throw exception when password confirmation doesn't match")
    public void testRegisterUser_WithPasswordMismatch_ShouldThrowException() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setUsername("newuser");
        form.setEmail("newuser@example.com");
        form.setPassword("Password123!");
        form.setConfirmPassword("DifferentPassword123!");
        form.setFullName("New User");
        form.setAccountType("customer");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleUserService.registerUser(form);
        });
        assertTrue(exception.getMessage().contains("Mật khẩu xác nhận không khớp"));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    public void testRegisterUser_WithExistingUsername_ShouldThrowException() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setUsername("testuser");
        form.setEmail("newuser@example.com");
        form.setPassword("Password123!");
        form.setConfirmPassword("Password123!");
        form.setFullName("New User");
        form.setAccountType("customer");

        when(userRepository.existsByUsernameIgnoreCase("testuser")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleUserService.registerUser(form);
        });
        assertTrue(exception.getMessage().contains("Username đã tồn tại"));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    public void testRegisterUser_WithExistingEmail_ShouldThrowException() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setUsername("newuser");
        form.setEmail("test@example.com");
        form.setPassword("Password123!");
        form.setConfirmPassword("Password123!");
        form.setFullName("New User");
        form.setAccountType("customer");

        when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleUserService.registerUser(form);
        });
        assertTrue(exception.getMessage().contains("Email đã được sử dụng"));
    }

    @Test
    @DisplayName("Should register customer successfully with active=true")
    public void testRegisterUser_WithCustomerRole_ShouldSetActiveTrue() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setUsername("newcustomer");
        form.setEmail("newcustomer@example.com");
        form.setPassword("Password123!");
        form.setConfirmPassword("Password123!");
        form.setFullName("New Customer");
        form.setAccountType("customer");

        when(userRepository.existsByUsernameIgnoreCase("newcustomer")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("newcustomer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // When
        User result = simpleUserService.registerUser(form, UserRole.CUSTOMER);

        // Then
        assertNotNull(result);
        assertTrue(result.getActive());
        assertEquals(UserRole.CUSTOMER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should register restaurant owner successfully with active=false")
    public void testRegisterUser_WithRestaurantOwnerRole_ShouldSetActiveFalse() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setUsername("newowner");
        form.setEmail("newowner@example.com");
        form.setPassword("Password123!");
        form.setConfirmPassword("Password123!");
        form.setFullName("New Owner");
        form.setAccountType("restaurant_owner");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername("newowner");
        savedUser.setRole(UserRole.RESTAURANT_OWNER);
        savedUser.setActive(false);

        when(userRepository.existsByUsernameIgnoreCase("newowner")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("newowner@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            u.setActive(false);
            return u;
        });
        when(restaurantOwnerRepository.existsByUser(any(User.class))).thenReturn(false);
        when(restaurantOwnerRepository.save(any(RestaurantOwner.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // When
        User result = simpleUserService.registerUser(form, UserRole.RESTAURANT_OWNER);

        // Then
        assertNotNull(result);
        assertFalse(result.getActive());
        assertEquals(UserRole.RESTAURANT_OWNER, result.getRole());
        verify(userRepository).save(any(User.class));
        verify(restaurantOwnerRepository).save(any(RestaurantOwner.class));
    }

    // ========== createRestaurantOwnerIfNeeded() Tests ==========

    @Test
    @DisplayName("Should create RestaurantOwner when user is restaurant owner and doesn't have record")
    public void testCreateRestaurantOwnerIfNeeded_WithRestaurantOwnerRole_NoRecord_ShouldCreate() {
        // Given
        User restaurantOwnerUser = new User();
        restaurantOwnerUser.setId(UUID.randomUUID());
        restaurantOwnerUser.setRole(UserRole.RESTAURANT_OWNER);

        when(restaurantOwnerRepository.existsByUser(restaurantOwnerUser)).thenReturn(false);
        when(restaurantOwnerRepository.save(any(RestaurantOwner.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // When
        simpleUserService.createRestaurantOwnerIfNeeded(restaurantOwnerUser);

        // Then
        verify(restaurantOwnerRepository).save(any(RestaurantOwner.class));
    }

    @Test
    @DisplayName("Should not create RestaurantOwner when record already exists")
    public void testCreateRestaurantOwnerIfNeeded_WithExistingRecord_ShouldNotCreate() {
        // Given
        User restaurantOwnerUser = new User();
        restaurantOwnerUser.setId(UUID.randomUUID());
        restaurantOwnerUser.setRole(UserRole.RESTAURANT_OWNER);

        when(restaurantOwnerRepository.existsByUser(restaurantOwnerUser)).thenReturn(true);

        // When
        simpleUserService.createRestaurantOwnerIfNeeded(restaurantOwnerUser);

        // Then
        verify(restaurantOwnerRepository, never()).save(any(RestaurantOwner.class));
    }

    @Test
    @DisplayName("Should not create RestaurantOwner when user is not restaurant owner")
    public void testCreateRestaurantOwnerIfNeeded_WithCustomerRole_ShouldNotCreate() {
        // Given
        User customerUser = new User();
        customerUser.setId(UUID.randomUUID());
        customerUser.setRole(UserRole.CUSTOMER);

        // When
        simpleUserService.createRestaurantOwnerIfNeeded(customerUser);

        // Then
        verify(restaurantOwnerRepository, never()).existsByUser(any(User.class));
        verify(restaurantOwnerRepository, never()).save(any(RestaurantOwner.class));
    }

    // ========== changePassword() Tests ==========

    @Test
    @DisplayName("Should change password successfully with correct current password")
    public void testChangePassword_WithCorrectCurrentPassword_ShouldChangeSuccessfully() {
        // Given
        testUser.setPassword("$2a$10$encodedPassword");
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        boolean result = simpleUserService.changePassword(testUser, "oldPassword", "newPassword");

        // Then
        assertTrue(result);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should return false when current password is incorrect")
    public void testChangePassword_WithIncorrectCurrentPassword_ShouldReturnFalse() {
        // Given
        testUser.setPassword("$2a$10$encodedPassword");
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // When
        boolean result = simpleUserService.changePassword(testUser, "wrongPassword", "newPassword");

        // Then
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== verifyEmail() Tests ==========

    @Test
    @DisplayName("Should verify email successfully")
    public void testVerifyEmail_WithValidToken_ShouldVerifySuccessfully() {
        // Given
        String token = "validToken123";
        testUser.setEmailVerified(false);
        testUser.setEmailVerificationToken(token);
        testUser.setRole(UserRole.CUSTOMER);

        when(userRepository.findByEmailVerificationToken(token))
                .thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        boolean result = simpleUserService.verifyEmail(token);

        // Then
        assertTrue(result);
        assertTrue(testUser.getEmailVerified());
        assertTrue(testUser.getActive());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should verify email and set active=true for RESTAURANT_OWNER")
    public void testVerifyEmail_WithRestaurantOwner_ShouldSetActiveTrue() {
        // Given
        String token = "validToken123";
        testUser.setEmailVerified(false);
        testUser.setEmailVerificationToken(token);
        testUser.setRole(UserRole.RESTAURANT_OWNER);
        testUser.setActive(false);

        when(userRepository.findByEmailVerificationToken(token))
                .thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        boolean result = simpleUserService.verifyEmail(token);

        // Then
        assertTrue(result);
        assertTrue(testUser.getEmailVerified());
        assertTrue(testUser.getActive());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should return false when token is invalid")
    public void testVerifyEmail_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String token = "invalidToken";
        when(userRepository.findByEmailVerificationToken(token))
                .thenReturn(java.util.Optional.empty());

        // When
        boolean result = simpleUserService.verifyEmail(token);

        // Then
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== resetPassword() Tests ==========

    @Test
    @DisplayName("Should reset password successfully with valid token")
    public void testResetPassword_WithValidToken_ShouldResetSuccessfully() {
        // Given
        String token = "validToken123";
        testUser.setPasswordResetToken(token);
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByValidPasswordResetToken(token, any(LocalDateTime.class)))
                .thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        boolean result = simpleUserService.resetPassword(token, "newPassword");

        // Then
        assertTrue(result);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should return false when token is invalid or expired")
    public void testResetPassword_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String token = "invalidToken";
        when(userRepository.findByValidPasswordResetToken(token, any(LocalDateTime.class)))
                .thenReturn(java.util.Optional.empty());

        // When
        boolean result = simpleUserService.resetPassword(token, "newPassword");

        // Then
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== updateUserRole() Tests ==========

    @Test
    @DisplayName("Should update user role successfully")
    public void testUpdateUserRole_ShouldUpdateRoleSuccessfully() {
        // Given
        testUser.setRole(UserRole.CUSTOMER);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        simpleUserService.updateUserRole(testUser, UserRole.RESTAURANT_OWNER);

        // Then
        assertEquals(UserRole.RESTAURANT_OWNER, testUser.getRole());
        verify(userRepository).save(testUser);
    }

    // ========== findById() Tests ==========

    @Test
    @DisplayName("Should find user by ID successfully")
    public void testFindById_WithValidId_ShouldReturnUser() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        User result = simpleUserService.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    public void testFindById_WithNonExistentId_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleUserService.findById(userId);
        });
        assertTrue(exception.getMessage().contains("không tồn tại"));
    }

    // ========== findByEmail() Tests ==========

    @Test
    @DisplayName("Should return user when email exists")
    public void testFindByEmail_WithExistingEmail_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = simpleUserService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Should return empty when email not found")
    public void testFindByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = simpleUserService.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Should return empty when email is null")
    public void testFindByEmail_WithNullEmail_ShouldReturnEmpty() {
        // When
        Optional<User> result = simpleUserService.findByEmail(null);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
    }

    // ========== findByUsername() Tests ==========

    @Test
    @DisplayName("Should return user when username exists")
    public void testFindByUsername_WithExistingUsername_ShouldReturnUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = simpleUserService.findByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(userRepository).findByUsernameIgnoreCase(username);
    }

    @Test
    @DisplayName("Should return empty when username not found")
    public void testFindByUsername_WithNonExistentUsername_ShouldReturnEmpty() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        // When
        Optional<User> result = simpleUserService.findByUsername(username);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByUsernameIgnoreCase(username);
    }

    @Test
    @DisplayName("Should return empty when username is null")
    public void testFindByUsername_WithNullUsername_ShouldReturnEmpty() {
        // When
        Optional<User> result = simpleUserService.findByUsername(null);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByUsernameIgnoreCase(anyString());
    }

    // ========== sendPasswordResetToken() Tests ==========

    @Test
    @DisplayName("Should send password reset token successfully")
    public void testSendPasswordResetToken_WithValidEmail_ShouldSendToken() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        // When
        simpleUserService.sendPasswordResetToken(email);

        // Then
        verify(userRepository).save(any(User.class));
        verify(emailService).sendPasswordResetEmail(eq(email), anyString());
        assertNotNull(testUser.getPasswordResetToken());
        assertNotNull(testUser.getPasswordResetTokenExpiry());
    }

    @Test
    @DisplayName("Should not throw exception when email not found")
    public void testSendPasswordResetToken_WithNonExistentEmail_ShouldNotThrowException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then - Should not throw exception for security
        assertDoesNotThrow(() -> {
            simpleUserService.sendPasswordResetToken(email);
        });
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // ========== updateProfileImage() Tests ==========

    @Test
    @DisplayName("Should update profile image successfully")
    public void testUpdateProfileImage_ShouldUpdateImageUrl() {
        // Given
        String imageUrl = "https://example.com/profile.jpg";
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = simpleUserService.updateProfileImage(testUser, imageUrl);

        // Then
        assertNotNull(result);
        assertEquals(imageUrl, result.getProfileImageUrl());
        assertNotNull(result.getUpdatedAt());
        verify(userRepository).save(testUser);
    }

    // ========== upsertGoogleUser() Tests ==========

    @Test
    @DisplayName("Should update existing Google user by Google ID")
    public void testUpsertGoogleUser_WithExistingGoogleId_ShouldUpdate() {
        // Given
        String googleId = "google123";
        String email = "test@example.com";
        String name = "Test User";

        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setGoogleId(googleId);
        existingUser.setEmail(email);

        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // When
        User result = simpleUserService.upsertGoogleUser(googleId, email, name);

        // Then
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertNotNull(result.getLastLogin());
        verify(userRepository).save(existingUser);
        verify(restaurantOwnerRepository, never()).save(any(RestaurantOwner.class));
    }

    @Test
    @DisplayName("Should link Google account to existing user by email")
    public void testUpsertGoogleUser_WithExistingEmail_ShouldLinkAccount() {
        // Given
        String googleId = "google123";
        String email = "test@example.com";
        String name = "Test User";

        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail(email);
        existingUser.setRole(UserRole.CUSTOMER);

        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // When
        User result = simpleUserService.upsertGoogleUser(googleId, email, name);

        // Then
        assertNotNull(result);
        assertEquals(googleId, result.getGoogleId());
        assertTrue(result.getEmailVerified());
        assertNotNull(result.getLastLogin());
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Should create new Google user when not exists")
    public void testUpsertGoogleUser_WithNewUser_ShouldCreate() {
        // Given
        String googleId = "google123";
        String email = "newuser@gmail.com";
        String name = "New User";

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setGoogleId(googleId);
        newUser.setEmail(email);
        newUser.setUsername(email);
        newUser.setFullName(name);
        newUser.setRole(UserRole.CUSTOMER);
        newUser.setEmailVerified(true);
        newUser.setLastLogin(LocalDateTime.now());

        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        // When
        User result = simpleUserService.upsertGoogleUser(googleId, email, name);

        // Then
        assertNotNull(result);
        assertEquals(googleId, result.getGoogleId());
        assertEquals(email, result.getEmail());
        assertTrue(result.getEmailVerified());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should create RestaurantOwner when new Google user is restaurant owner")
    public void testUpsertGoogleUser_WithRestaurantOwnerRole_ShouldCreateOwner() {
        // Given
        String googleId = "google123";
        String email = "owner@gmail.com";
        String name = "Owner User";

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setGoogleId(googleId);
        newUser.setEmail(email);
        newUser.setRole(UserRole.RESTAURANT_OWNER);

        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(restaurantOwnerRepository.save(any(RestaurantOwner.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // When
        User result = simpleUserService.upsertGoogleUser(googleId, email, name, UserRole.RESTAURANT_OWNER);

        // Then
        assertNotNull(result);
        assertEquals(UserRole.RESTAURANT_OWNER, result.getRole());
        verify(restaurantOwnerRepository).save(any(RestaurantOwner.class));
    }

    @Test
    @DisplayName("Should handle null email in upsertGoogleUser")
    public void testUpsertGoogleUser_WithNullEmail_ShouldUseGoogleIdAsUsername() {
        // Given
        String googleId = "google123";
        String email = null;
        String name = "User";

        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        // When
        User result = simpleUserService.upsertGoogleUser(googleId, email, name);

        // Then
        assertNotNull(result);
        assertEquals(googleId, result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle null name in upsertGoogleUser")
    public void testUpsertGoogleUser_WithNullName_ShouldUseDefaultName() {
        // Given
        String googleId = "google123";
        String email = "test@gmail.com";
        String name = null;

        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        // When
        User result = simpleUserService.upsertGoogleUser(googleId, email, name);

        // Then
        assertNotNull(result);
        assertEquals("Google User", result.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle user with null role in upsertGoogleUser")
    public void testUpsertGoogleUser_WithNullRole_ShouldSetCustomerRole() {
        // Given
        String googleId = "google123";
        String email = "test@gmail.com";
        String name = "Test User";

        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail(email);
        existingUser.setRole(null); // Null role

        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // When
        User result = simpleUserService.upsertGoogleUser(googleId, email, name);

        // Then
        assertNotNull(result);
        assertEquals(UserRole.CUSTOMER, result.getRole());
        verify(userRepository).save(existingUser);
    }

    // ========== getCurrentUser() Tests ==========

    @Test
    @DisplayName("Should return empty for getCurrentUser (placeholder)")
    public void testGetCurrentUser_ShouldReturnEmpty() {
        // When
        Optional<User> result = simpleUserService.getCurrentUser();

        // Then
        assertFalse(result.isPresent());
    }

    // ========== findByGoogleId() Tests ==========

    @Test
    @DisplayName("Should return user when Google ID exists")
    public void testFindByGoogleId_WithExistingGoogleId_ShouldReturnUser() {
        // Given
        String googleId = "google123";
        testUser.setGoogleId(googleId);
        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = simpleUserService.findByGoogleId(googleId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(googleId, result.get().getGoogleId());
        verify(userRepository).findByGoogleId(googleId);
    }

    @Test
    @DisplayName("Should return empty when Google ID not found")
    public void testFindByGoogleId_WithNonExistentGoogleId_ShouldReturnEmpty() {
        // Given
        String googleId = "nonexistent";
        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = simpleUserService.findByGoogleId(googleId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByGoogleId(googleId);
    }
}

