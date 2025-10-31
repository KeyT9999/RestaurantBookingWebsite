package com.example.booking.web.controller;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Expanded comprehensive tests for AdminSetupController
 * Covers additional scenarios and edge cases for better coverage
 */
@WebMvcTest(AdminSetupController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminSetupController Expanded Test Suite")
class AdminSetupControllerExpandedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Nested
    @DisplayName("setupPage() Tests")
    class SetupPageTests {

        @Test
        @DisplayName("Should render setup page successfully")
        void testSetupPage_ShouldRenderView() throws Exception {
            mockMvc.perform(get("/admin-setup"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attributeDoesNotExist("message"));
        }

        @Test
        @DisplayName("Should render setup page without requiring authentication")
        void testSetupPage_WithoutAuthentication_ShouldRender() throws Exception {
            // Setup endpoint should be accessible without authentication
            mockMvc.perform(get("/admin-setup"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"));
        }
    }

    @Nested
    @DisplayName("createAdmin() Tests")
    class CreateAdminTests {

        @Test
        @DisplayName("Should create admin when none exists")
        void testCreateAdmin_WhenNoAdminExists_ShouldCreateSuccessfully() throws Exception {
            // Mock: No admin users exist
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));
            
            // Mock: Save admin user
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return user;
            });

            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attributeExists("message"))
                    .andExpect(model().attribute("message", 
                            org.hamcrest.Matchers.containsString("Admin user created successfully")));

            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should not create admin when one already exists")
        void testCreateAdmin_WhenAdminExists_ShouldNotCreate() throws Exception {
            // Mock: Admin user already exists
            User existingAdmin = new User();
            existingAdmin.setUsername("admin");
            existingAdmin.setEmail("admin@bookeat.vn");
            existingAdmin.setRole(UserRole.ADMIN);
            
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.singletonList(existingAdmin)));

            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attributeExists("message"))
                    .andExpect(model().attribute("message", 
                            org.hamcrest.Matchers.containsString("Admin user already exists")));

            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle exception when checking for existing admin")
        void testCreateAdmin_WhenCheckThrowsException_ShouldHandleError() throws Exception {
            // Mock: Repository throws exception
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attributeExists("message"))
                    .andExpect(model().attribute("message", 
                            org.hamcrest.Matchers.containsString("Error creating admin user")));

            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle exception when saving admin")
        void testCreateAdmin_WhenSaveThrowsException_ShouldHandleError() throws Exception {
            // Mock: No admin exists
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));
            
            // Mock: Save throws exception
            when(userRepository.save(any(User.class)))
                    .thenThrow(new RuntimeException("Save operation failed"));

            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/setup"))
                    .andExpect(model().attributeExists("message"))
                    .andExpect(model().attribute("message", 
                            org.hamcrest.Matchers.containsString("Error creating admin user")));

            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should create admin with correct properties")
        void testCreateAdmin_ShouldCreateWithCorrectProperties() throws Exception {
            // Mock: No admin exists
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));
            
            // Capture saved user
            User savedUser = new User();
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                savedUser.setUsername(user.getUsername());
                savedUser.setEmail(user.getEmail());
                savedUser.setPassword(user.getPassword());
                savedUser.setRole(user.getRole());
                savedUser.setActive(user.getActive());
                return user;
            });

            mockMvc.perform(post("/admin-setup/create-admin"))
                    .andExpect(status().isOk());

            // Verify admin user properties
            verify(userRepository).save(argThat(user ->
                    "admin".equals(user.getUsername()) &&
                    "admin@bookeat.vn".equals(user.getEmail()) &&
                    UserRole.ADMIN.equals(user.getRole()) &&
                    Boolean.TRUE.equals(user.getActive()) &&
                    user.getPassword() != null &&
                    user.getPassword().startsWith("$2a$")
            ));
        }
    }
}

