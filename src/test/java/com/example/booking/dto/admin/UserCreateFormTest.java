package com.example.booking.dto.admin;

import com.example.booking.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserCreateForm.
 * Coverage Target: 100%
 * Test Cases: 4
 *
 * @author Professional Test Engineer
 */
@DisplayName("UserCreateForm Tests")
class UserCreateFormTest {

    @Test
    @DisplayName("Should create form with default values")
    void constructor_SetsDefaults() {
        // When
        UserCreateForm form = new UserCreateForm();

        // Then
        assertThat(form).isNotNull();
        assertThat(form.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(form.isEmailVerified()).isTrue();
        assertThat(form.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should set and get all required fields")
    void settersGetters_RequiredFields_WorkCorrectly() {
        // Given
        UserCreateForm form = new UserCreateForm();

        // When
        form.setUsername("testuser");
        form.setEmail("test@example.com");
        form.setPassword("password123");
        form.setFullName("Test User");

        // Then
        assertThat(form.getUsername()).isEqualTo("testuser");
        assertThat(form.getEmail()).isEqualTo("test@example.com");
        assertThat(form.getPassword()).isEqualTo("password123");
        assertThat(form.getFullName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should set and get all optional fields")
    void settersGetters_OptionalFields_WorkCorrectly() {
        // Given
        UserCreateForm form = new UserCreateForm();

        // When
        form.setPhoneNumber("0901234567");
        form.setAddress("123 Test St");
        form.setRole(UserRole.RESTAURANT_OWNER);
        form.setEmailVerified(false);
        form.setActive(false);

        // Then
        assertThat(form.getPhoneNumber()).isEqualTo("0901234567");
        assertThat(form.getAddress()).isEqualTo("123 Test St");
        assertThat(form.getRole()).isEqualTo(UserRole.RESTAURANT_OWNER);
        assertThat(form.isEmailVerified()).isFalse();
        assertThat(form.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle all user roles")
    void setRole_AllRoles_WorkCorrectly() {
        // Given
        UserCreateForm form = new UserCreateForm();

        // When & Then - CUSTOMER
        form.setRole(UserRole.CUSTOMER);
        assertThat(form.getRole()).isEqualTo(UserRole.CUSTOMER);

        // When & Then - RESTAURANT_OWNER
        form.setRole(UserRole.RESTAURANT_OWNER);
        assertThat(form.getRole()).isEqualTo(UserRole.RESTAURANT_OWNER);

        // When & Then - ADMIN
        form.setRole(UserRole.ADMIN);
        assertThat(form.getRole()).isEqualTo(UserRole.ADMIN);
    }
}

