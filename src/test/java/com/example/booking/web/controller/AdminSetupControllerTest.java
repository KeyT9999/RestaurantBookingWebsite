package com.example.booking.web.controller;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive Test Suite for AdminSetupController
 * 
 * Test Categories:
 * 1. setupPage() - GET /admin-setup - 2 test cases
 * 2. createAdmin() - POST /admin-setup/create-admin - 4+ test cases
 * 
 * Each endpoint is tested for:
 * - Happy Path: Valid scenarios that should succeed
 * - Edge Cases: Admin already exists, repository errors
 * - Error Handling: Service exceptions and errors
 */
@WebMvcTest(AdminSetupController.class)
@DisplayName("AdminSetupController Test Suite")
class AdminSetupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    private User existingAdmin;

    @BeforeEach
    void setUp() {
        existingAdmin = new User();
        existingAdmin.setId(UUID.randomUUID());
        existingAdmin.setUsername("admin");
        existingAdmin.setEmail("admin@bookeat.vn");
        existingAdmin.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUx0J8KqF0vVjqKjKjKjKjKjK");
        existingAdmin.setRole(UserRole.ADMIN);
        existingAdmin.setActive(true);
    }

    // ============================================================================
    // TEST GROUP 1: setupPage() - GET /admin-setup
    // ============================================================================

    @Nested
    @DisplayName("1. setupPage() - 2 Test Cases")
    class SetupPageTests {

        @Test
        @DisplayName("Happy Path: Setup page should load successfully")
        void testSetupPage_ShouldLoadSuccessfully() throws Exception {
            // When & Then
            mockMvc.perform(get("/admin-setup"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"));
        }

        @Test
        @DisplayName("Happy Path: Setup page should be accessible without authentication")
        void testSetupPage_ShouldBeAccessibleWithoutAuth() throws Exception {
            // When & Then
            mockMvc.perform(get("/admin-setup"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"));
        }
    }

    // ============================================================================
    // TEST GROUP 2: createAdmin() - POST /admin-setup/create-admin
    // ============================================================================

    @Nested
    @DisplayName("2. createAdmin() - 4+ Test Cases")
    class CreateAdminTests {

        @Test
        @DisplayName("Happy Path: Create admin when no admin exists should succeed")
        void testCreateAdmin_WhenNoAdminExists_ShouldSucceed() throws Exception {
            // Given - No admin exists
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(emptyPage);

            User savedAdmin = new User();
            savedAdmin.setId(UUID.randomUUID());
            savedAdmin.setUsername("admin");
            savedAdmin.setEmail("admin@bookeat.vn");
            savedAdmin.setRole(UserRole.ADMIN);
            when(userRepository.save(any(User.class))).thenReturn(savedAdmin);

            // When & Then
            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attribute("message", 
                            containsString("Admin user created successfully")));

            // Verify admin was created
            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Business Logic: Create admin when admin already exists should return message")
        void testCreateAdmin_WhenAdminExists_ShouldReturnMessage() throws Exception {
            // Given - Admin already exists
            List<User> adminList = new ArrayList<>();
            adminList.add(existingAdmin);
            Page<User> adminPage = new PageImpl<>(adminList);
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(adminPage);

            // When & Then
            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attribute("message", 
                            containsString("Admin user already exists")));

            // Verify admin was NOT created
            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Error Handling: Repository exception should be handled gracefully")
        void testCreateAdmin_WhenRepositoryThrowsException_ShouldHandleGracefully() throws Exception {
            // Given - Repository throws exception
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attribute("message", 
                            containsString("Error creating admin user")));

            // Verify repository was called
            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
        }

        @Test
        @DisplayName("Happy Path: Created admin should have correct attributes")
        void testCreateAdmin_CreatedAdminShouldHaveCorrectAttributes() throws Exception {
            // Given - No admin exists
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(emptyPage);

            User savedAdmin = new User();
            savedAdmin.setId(UUID.randomUUID());
            savedAdmin.setUsername("admin");
            savedAdmin.setEmail("admin@bookeat.vn");
            savedAdmin.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUx0J8KqF0vVjqKjKjKjKjKjK");
            savedAdmin.setRole(UserRole.ADMIN);
            savedAdmin.setActive(true);
            when(userRepository.save(any(User.class))).thenReturn(savedAdmin);

            // When
            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attribute("message", 
                            containsString("Username: admin")));

            // Then - Verify admin was created with correct attributes
            verify(userRepository).save(argThat(user -> {
                User u = (User) user;
                return "admin".equals(u.getUsername()) &&
                       "admin@bookeat.vn".equals(u.getEmail()) &&
                       UserRole.ADMIN.equals(u.getRole()) &&
                       Boolean.TRUE.equals(u.getActive());
            }));
        }
    }
}
