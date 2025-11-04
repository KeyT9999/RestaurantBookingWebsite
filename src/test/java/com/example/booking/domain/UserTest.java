package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for User domain entity
 */
@DisplayName("User Domain Entity Tests")
public class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
    }

    // ========== getAuthorities() Tests ==========

    @Test
    @DisplayName("shouldGetAuthorities_successfully")
    void shouldGetAuthorities_successfully() {
        // When
        Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    @DisplayName("shouldReturnCorrectAuthority_forAdmin")
    void shouldReturnCorrectAuthority_forAdmin() {
        // Given
        user.setRole(UserRole.ADMIN);

        // When
        Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    // ========== isAccountNonExpired() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_forActiveAccount")
    void shouldReturnTrue_forActiveAccount() {
        // When
        boolean result = user.isAccountNonExpired();

        // Then
        assertTrue(result);
    }

    // ========== isAccountNonLocked() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_forNonLockedAccount")
    void shouldReturnTrue_forNonLockedAccount() {
        // When
        boolean result = user.isAccountNonLocked();

        // Then
        assertTrue(result);
    }

    // ========== isCredentialsNonExpired() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_forValidCredentials")
    void shouldReturnTrue_forValidCredentials() {
        // When
        boolean result = user.isCredentialsNonExpired();

        // Then
        assertTrue(result);
    }

    // ========== isEnabled() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenUserIsActive")
    void shouldReturnTrue_whenUserIsActive() {
        // Given
        user.setActive(true);

        // When
        boolean result = user.isEnabled();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenUserIsInactive")
    void shouldReturnFalse_whenUserIsInactive() {
        // Given
        user.setActive(false);

        // When
        boolean result = user.isEnabled();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenEmailNotVerified")
    void shouldReturnFalse_whenEmailNotVerified() {
        // Given
        user.setEmailVerified(false);
        user.setDeletedAt(null);

        // When
        boolean result = user.isEnabled();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenUserIsDeleted")
    void shouldReturnFalse_whenUserIsDeleted() {
        // Given
        user.setEmailVerified(true);
        user.setDeletedAt(java.time.LocalDateTime.now());

        // When
        boolean result = user.isEnabled();

        // Then
        assertFalse(result);
    }

    // ========== isAccountNonLocked() Edge Cases Tests ==========

    @Test
    @DisplayName("shouldReturnFalse_whenActiveIsFalse")
    void shouldReturnFalse_whenActiveIsFalse() {
        // Given
        user.setActive(false);

        // When
        boolean result = user.isAccountNonLocked();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenActiveIsNull")
    void shouldReturnFalse_whenActiveIsNull() {
        // Given
        user.setActive(null);

        // When
        boolean result = user.isAccountNonLocked();

        // Then
        assertFalse(result);
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateUser_withConstructor")
    void shouldCreateUser_withConstructor() {
        // Given
        String username = "newuser";
        String email = "newuser@test.com";
        String password = "password123";
        String fullName = "New User";

        // When
        User newUser = new User(username, email, password, fullName);

        // Then
        assertEquals(username, newUser.getUsername());
        assertEquals(email, newUser.getEmail());
        assertEquals(password, newUser.getPassword());
        assertEquals(fullName, newUser.getFullName());
    }

    // ========== Lifecycle Callback Tests ==========

    @Test
    @DisplayName("shouldSetCreatedAtAndUpdatedAt_onPrePersist")
    void shouldSetCreatedAtAndUpdatedAt_onPrePersist() throws Exception {
        // Given
        User newUser = new User();

        // When - Simulate @PrePersist by calling prePersist directly
        java.lang.reflect.Method prePersist = User.class.getDeclaredMethod("prePersist");
        prePersist.setAccessible(true);
        prePersist.invoke(newUser);

        // Then
        assertNotNull(newUser.getCreatedAt());
        assertNotNull(newUser.getUpdatedAt());
        assertEquals(newUser.getCreatedAt(), newUser.getUpdatedAt());
    }

    @Test
    @DisplayName("shouldNotOverrideCreatedAt_onPrePersist_whenAlreadySet")
    void shouldNotOverrideCreatedAt_onPrePersist_whenAlreadySet() throws Exception {
        // Given
        User newUser = new User();
        java.time.LocalDateTime existingCreatedAt = java.time.LocalDateTime.now().minusDays(1);
        newUser.setCreatedAt(existingCreatedAt);

        // When - Simulate @PrePersist
        java.lang.reflect.Method prePersist = User.class.getDeclaredMethod("prePersist");
        prePersist.setAccessible(true);
        prePersist.invoke(newUser);

        // Then
        assertEquals(existingCreatedAt, newUser.getCreatedAt()); // Should keep existing createdAt
        assertNotNull(newUser.getUpdatedAt());
    }

    @Test
    @DisplayName("shouldSetUpdatedAt_onPreUpdate")
    void shouldSetUpdatedAt_onPreUpdate() throws Exception {
        // Given
        java.time.LocalDateTime initialCreatedAt = java.time.LocalDateTime.now().minusHours(1);
        user.setCreatedAt(initialCreatedAt);
        user.setUpdatedAt(initialCreatedAt);

        // When - Simulate @PreUpdate
        java.lang.reflect.Method preUpdate = User.class.getDeclaredMethod("preUpdate");
        preUpdate.setAccessible(true);
        preUpdate.invoke(user);

        // Then
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(initialCreatedAt));
        assertEquals(initialCreatedAt, user.getCreatedAt()); // createdAt should not change
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetId_successfully")
    void shouldSetAndGetId_successfully() {
        // Given
        UUID newId = UUID.randomUUID();

        // When
        user.setId(newId);

        // Then
        assertEquals(newId, user.getId());
    }

    @Test
    @DisplayName("shouldSetAndGetUsername_successfully")
    void shouldSetAndGetUsername_successfully() {
        // Given
        String username = "newusername";

        // When
        user.setUsername(username);

        // Then
        assertEquals(username, user.getUsername());
    }

    @Test
    @DisplayName("shouldSetAndGetEmail_successfully")
    void shouldSetAndGetEmail_successfully() {
        // Given
        String email = "newemail@test.com";

        // When
        user.setEmail(email);

        // Then
        assertEquals(email, user.getEmail());
    }

    @Test
    @DisplayName("shouldSetAndGetPassword_successfully")
    void shouldSetAndGetPassword_successfully() {
        // Given
        String password = "newpassword123";

        // When
        user.setPassword(password);

        // Then
        assertEquals(password, user.getPassword());
    }

    @Test
    @DisplayName("shouldSetAndGetFullName_successfully")
    void shouldSetAndGetFullName_successfully() {
        // Given
        String fullName = "John Doe";

        // When
        user.setFullName(fullName);

        // Then
        assertEquals(fullName, user.getFullName());
    }

    @Test
    @DisplayName("shouldSetAndGetPhoneNumber_successfully")
    void shouldSetAndGetPhoneNumber_successfully() {
        // Given
        String phoneNumber = "0987654321";

        // When
        user.setPhoneNumber(phoneNumber);

        // Then
        assertEquals(phoneNumber, user.getPhoneNumber());
    }

    @Test
    @DisplayName("shouldSetAndGetAddress_successfully")
    void shouldSetAndGetAddress_successfully() {
        // Given
        String address = "123 Main Street";

        // When
        user.setAddress(address);

        // Then
        assertEquals(address, user.getAddress());
    }

    @Test
    @DisplayName("shouldSetAndGetProfileImageUrl_successfully")
    void shouldSetAndGetProfileImageUrl_successfully() {
        // Given
        String imageUrl = "https://example.com/image.jpg";

        // When
        user.setProfileImageUrl(imageUrl);

        // Then
        assertEquals(imageUrl, user.getProfileImageUrl());
    }

    @Test
    @DisplayName("shouldSetAndGetRole_successfully")
    void shouldSetAndGetRole_successfully() {
        // Given
        UserRole role = UserRole.RESTAURANT_OWNER;

        // When
        user.setRole(role);

        // Then
        assertEquals(role, user.getRole());
    }

    @Test
    @DisplayName("shouldSetAndGetEmailVerified_successfully")
    void shouldSetAndGetEmailVerified_successfully() {
        // Given
        Boolean emailVerified = true;

        // When
        user.setEmailVerified(emailVerified);

        // Then
        assertEquals(emailVerified, user.getEmailVerified());
    }

    @Test
    @DisplayName("shouldSetAndGetEmailVerificationToken_successfully")
    void shouldSetAndGetEmailVerificationToken_successfully() {
        // Given
        String token = "verification-token-123";

        // When
        user.setEmailVerificationToken(token);

        // Then
        assertEquals(token, user.getEmailVerificationToken());
    }

    @Test
    @DisplayName("shouldSetAndGetPasswordResetToken_successfully")
    void shouldSetAndGetPasswordResetToken_successfully() {
        // Given
        String token = "reset-token-123";

        // When
        user.setPasswordResetToken(token);

        // Then
        assertEquals(token, user.getPasswordResetToken());
    }

    @Test
    @DisplayName("shouldSetAndGetPasswordResetTokenExpiry_successfully")
    void shouldSetAndGetPasswordResetTokenExpiry_successfully() {
        // Given
        java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusHours(1);

        // When
        user.setPasswordResetTokenExpiry(expiry);

        // Then
        assertEquals(expiry, user.getPasswordResetTokenExpiry());
    }

    @Test
    @DisplayName("shouldSetAndGetGoogleId_successfully")
    void shouldSetAndGetGoogleId_successfully() {
        // Given
        String googleId = "google-123456";

        // When
        user.setGoogleId(googleId);

        // Then
        assertEquals(googleId, user.getGoogleId());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt_successfully")
    void shouldSetAndGetCreatedAt_successfully() {
        // Given
        java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

        // When
        user.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetUpdatedAt_successfully")
    void shouldSetAndGetUpdatedAt_successfully() {
        // Given
        java.time.LocalDateTime updatedAt = java.time.LocalDateTime.now();

        // When
        user.setUpdatedAt(updatedAt);

        // Then
        assertEquals(updatedAt, user.getUpdatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetLastLogin_successfully")
    void shouldSetAndGetLastLogin_successfully() {
        // Given
        java.time.LocalDateTime lastLogin = java.time.LocalDateTime.now();

        // When
        user.setLastLogin(lastLogin);

        // Then
        assertEquals(lastLogin, user.getLastLogin());
    }

    @Test
    @DisplayName("shouldSetAndGetActive_successfully")
    void shouldSetAndGetActive_successfully() {
        // Given
        Boolean active = false;

        // When
        user.setActive(active);

        // Then
        assertEquals(active, user.getActive());
    }

    @Test
    @DisplayName("shouldSetAndGetDeletedAt_successfully")
    void shouldSetAndGetDeletedAt_successfully() {
        // Given
        java.time.LocalDateTime deletedAt = java.time.LocalDateTime.now();

        // When
        user.setDeletedAt(deletedAt);

        // Then
        assertEquals(deletedAt, user.getDeletedAt());
    }

    // ========== getAuthorities() for All Roles Tests ==========

    @Test
    @DisplayName("shouldReturnCorrectAuthority_forRestaurantOwner")
    void shouldReturnCorrectAuthority_forRestaurantOwner() {
        // Given
        user.setRole(UserRole.RESTAURANT_OWNER);

        // When
        Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_OWNER")));
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("shouldHandleNullFullName")
    void shouldHandleNullFullName() {
        // When
        user.setFullName(null);

        // Then
        assertNull(user.getFullName());
    }

    @Test
    @DisplayName("shouldHandleNullPhoneNumber")
    void shouldHandleNullPhoneNumber() {
        // When
        user.setPhoneNumber(null);

        // Then
        assertNull(user.getPhoneNumber());
    }

    @Test
    @DisplayName("shouldHandleNullAddress")
    void shouldHandleNullAddress() {
        // When
        user.setAddress(null);

        // Then
        assertNull(user.getAddress());
    }

    @Test
    @DisplayName("shouldHandleNullProfileImageUrl")
    void shouldHandleNullProfileImageUrl() {
        // When
        user.setProfileImageUrl(null);

        // Then
        assertNull(user.getProfileImageUrl());
    }
}

