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
}

