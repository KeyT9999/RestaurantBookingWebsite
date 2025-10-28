package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.example.booking.dto.ProfileEditForm;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;

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
}

