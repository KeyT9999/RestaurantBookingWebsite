package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;

/**
 * Unit tests for AdminSetupController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminSetupController Tests")
public class AdminSetupControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @InjectMocks
    private AdminSetupController adminSetupController;

    // ========== setupPage() Tests ==========

    @Test
    @DisplayName("shouldShowSetupPage_successfully")
    void shouldShowSetupPage_successfully() {
        // When
        String view = adminSetupController.setupPage(model);

        // Then
        assertEquals("admin/setup", view);
    }

    // ========== createAdmin() Tests ==========

    @Test
    @DisplayName("shouldCreateAdmin_whenNoAdminExists")
    void shouldCreateAdmin_whenNoAdminExists() {
        // Given
        Page<User> emptyPage = new PageImpl<>(java.util.Collections.emptyList());
        when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class))).thenReturn(emptyPage);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String view = adminSetupController.createAdmin(model);

        // Then
        assertEquals("admin/setup", view);
        verify(userRepository, times(1)).save(any(User.class));
        verify(model, times(1)).addAttribute(eq("message"), anyString());
    }

    @Test
    @DisplayName("shouldNotCreateAdmin_whenAdminExists")
    void shouldNotCreateAdmin_whenAdminExists() {
        // Given
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        Page<User> adminPage = new PageImpl<>(java.util.Arrays.asList(admin));
        when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class))).thenReturn(adminPage);

        // When
        String view = adminSetupController.createAdmin(model);

        // Then
        assertEquals("admin/setup", view);
        verify(userRepository, never()).save(any(User.class));
        verify(model, times(1)).addAttribute(eq("message"), contains("already exists"));
    }
}

