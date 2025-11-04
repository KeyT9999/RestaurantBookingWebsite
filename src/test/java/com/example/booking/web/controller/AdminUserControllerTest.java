package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.admin.UserCreateForm;
import com.example.booking.dto.admin.UserEditForm;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.UserRepository;

/**
 * Unit tests for AdminUserController
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("AdminUserController Tests")
public class AdminUserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AdminUserController adminUserController;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@test.com");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
    }

    // ========== list() Tests ==========

    @Test
    @DisplayName("shouldListUsers_successfully")
    void shouldListUsers_successfully() {
        // Given
        when(userRepository.findAll(any(Sort.class))).thenReturn(Arrays.asList(user));

        // When
        String view = adminUserController.list(0, "10", "createdAt", "desc", "", null, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== create() Tests ==========

    @Test
    @DisplayName("shouldShowCreateForm_successfully")
    void shouldShowCreateForm_successfully() {
        // When
        String view = adminUserController.createForm(model);

        // Then
        assertNotNull(view);
        verify(model, times(1)).addAttribute(anyString(), any(UserCreateForm.class));
    }

    @Test
    @DisplayName("shouldListUsers_withFilters")
    void shouldListUsers_withFilters() {
        // Given
        when(userRepository.findAll(any(Sort.class))).thenReturn(Arrays.asList(user));

        // When
        String view = adminUserController.list(0, "20", "email", "asc", "test", "CUSTOMER", model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldListUsers_withAllSize")
    void shouldListUsers_withAllSize() {
        // Given
        when(userRepository.findAll(any(Sort.class))).thenReturn(Arrays.asList(user));

        // When
        String view = adminUserController.list(0, "all", "createdAt", "desc", "", null, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== create() Tests ==========

    @Test
    @DisplayName("shouldCreateUser_successfully")
    void shouldCreateUser_successfully() {
        // Given
        UserCreateForm form = new UserCreateForm();
        form.setUsername("newuser");
        form.setEmail("new@test.com");
        form.setPassword("password123");
        form.setFullName("New User");
        form.setRole(UserRole.CUSTOMER);
        form.setActive(true);
        form.setEmailVerified(false);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        String view = adminUserController.create(form, bindingResult, model);

        // Then
        assertTrue(view.contains("redirect"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("shouldFailCreate_withValidationErrors")
    void shouldFailCreate_withValidationErrors() {
        // Given
        UserCreateForm form = new UserCreateForm();
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String view = adminUserController.create(form, bindingResult, model);

        // Then
        assertEquals("admin/user-form", view);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("shouldFailCreate_withDuplicateUsername")
    void shouldFailCreate_withDuplicateUsername() {
        // Given
        UserCreateForm form = new UserCreateForm();
        form.setUsername("existing");
        form.setEmail("new@test.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        // When
        String view = adminUserController.create(form, bindingResult, model);

        // Then
        assertEquals("admin/user-form", view);
        verify(bindingResult).rejectValue(eq("username"), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("shouldFailCreate_withDuplicateEmail")
    void shouldFailCreate_withDuplicateEmail() {
        // Given
        UserCreateForm form = new UserCreateForm();
        form.setUsername("newuser");
        form.setEmail("existing@test.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        // When
        String view = adminUserController.create(form, bindingResult, model);

        // Then
        assertEquals("admin/user-form", view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== edit() Tests ==========

    @Test
    @DisplayName("shouldShowEditForm_successfully")
    void shouldShowEditForm_successfully() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        String view = adminUserController.editForm(userId, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any(UserEditForm.class));
    }

    @Test
    @DisplayName("shouldShowEditForm_userNotFound")
    void shouldShowEditForm_userNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        String view = adminUserController.editForm(userId, model);

        // Then
        assertTrue(view.contains("redirect"));
    }

    @Test
    @DisplayName("shouldEditUser_successfully")
    void shouldEditUser_successfully() {
        // Given
        UserEditForm form = new UserEditForm();
        form.setUsername("testuser");
        form.setEmail("test@test.com");
        form.setFullName("Updated Name");
        form.setRole(UserRole.CUSTOMER);
        form.setActive(true);
        form.setEmailVerified(true);
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        String view = adminUserController.edit(userId, form, bindingResult, model);

        // Then
        assertTrue(view.contains("redirect"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("shouldFailEdit_withValidationErrors")
    void shouldFailEdit_withValidationErrors() {
        // Given
        UserEditForm form = new UserEditForm();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String view = adminUserController.edit(userId, form, bindingResult, model);

        // Then
        assertEquals("admin/user-form", view);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("shouldFailEdit_userNotFound")
    void shouldFailEdit_userNotFound() {
        // Given
        UserEditForm form = new UserEditForm();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        String view = adminUserController.edit(userId, form, bindingResult, model);

        // Then
        assertTrue(view.contains("redirect"));
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== toggleActive() Tests ==========

    @Test
    @DisplayName("shouldToggleActive_successfully")
    void shouldToggleActive_successfully() {
        // Given
        user.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = adminUserController.toggleActive(userId);

        // Then
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("shouldToggleActive_userNotFound")
    void shouldToggleActive_userNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        var response = adminUserController.toggleActive(userId);

        // Then
        assertNotNull(response);
        verify(userRepository, never()).save(any(User.class));
    }
}

