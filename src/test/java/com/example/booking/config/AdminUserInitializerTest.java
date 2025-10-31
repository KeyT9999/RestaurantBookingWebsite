package com.example.booking.config;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for AdminUserInitializer
 */
@SpringBootTest
@DisplayName("AdminUserInitializer Test Suite")
class AdminUserInitializerTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private AdminUserInitializer adminUserInitializer;

    @Test
    @DisplayName("Should create admin user when none exists")
    void testCreateAdminUserWhenNoneExists() throws Exception {
        // Mock: No admin users exist
        when(userRepository.findByRole(eq(UserRole.ADMIN), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Run the initializer
        adminUserInitializer.run();

        // Verify: Admin user was saved
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should not create admin user when one already exists")
    void testDoNotCreateAdminUserWhenExists() throws Exception {
        // Mock: Admin user already exists
        User existingAdmin = new User();
        existingAdmin.setRole(UserRole.ADMIN);
        when(userRepository.findByRole(eq(UserRole.ADMIN), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(existingAdmin)));

        // Run the initializer
        adminUserInitializer.run();

        // Verify: Admin user was NOT saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should set correct admin user properties")
    void testAdminUserProperties() throws Exception {
        // Mock: No admin users exist
        when(userRepository.findByRole(eq(UserRole.ADMIN), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Capture the saved user
        User savedUser = new User();
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            savedUser.setUsername(user.getUsername());
            savedUser.setEmail(user.getEmail());
            savedUser.setPassword(user.getPassword());
            savedUser.setRole(user.getRole());
            savedUser.setActive(user.getActive());
            return null;
        }).when(userRepository).save(any(User.class));

        // Run the initializer
        adminUserInitializer.run();

        // Verify: Admin user has correct properties
        verify(userRepository).save(argThat(user ->
                "admin".equals(user.getUsername()) &&
                "admin@bookeat.vn".equals(user.getEmail()) &&
                UserRole.ADMIN.equals(user.getRole()) &&
                Boolean.TRUE.equals(user.getActive())
        ));
    }
}

