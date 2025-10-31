package com.example.booking.web.controller.api;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Expanded comprehensive tests for AdminApiController
 */
@WebMvcTest(AdminApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminApiController Expanded Test Suite")
class AdminApiControllerExpandedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Nested
    @DisplayName("createAdmin() Tests")
    class CreateAdminTests {

        @Test
        @DisplayName("Should create admin when none exists")
        void testCreateAdmin_WhenNoExisting_ShouldCreate() throws Exception {
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/api/admin/create-admin")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("created successfully")));

            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should not create admin when one already exists")
        void testCreateAdmin_WhenExists_ShouldReturnExistsMessage() throws Exception {
            User existingAdmin = new User();
            existingAdmin.setUsername("admin");
            existingAdmin.setRole(UserRole.ADMIN);

            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.singletonList(existingAdmin)));

            mockMvc.perform(post("/api/admin/create-admin")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("already exists")));

            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle exception when checking for existing admin")
        void testCreateAdmin_WhenCheckThrowsException_ShouldHandleError() throws Exception {
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(post("/api/admin/create-admin")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error creating admin user")));

            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle exception when saving admin")
        void testCreateAdmin_WhenSaveThrowsException_ShouldHandleError() throws Exception {
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));
            when(userRepository.save(any(User.class)))
                    .thenThrow(new RuntimeException("Save operation failed"));

            mockMvc.perform(post("/api/admin/create-admin")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error creating admin user")));

            verify(userRepository).findByRole(eq(UserRole.ADMIN), any(Pageable.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should create admin with correct properties")
        void testCreateAdmin_ShouldCreateWithCorrectProperties() throws Exception {
            when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/api/admin/create-admin")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

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

