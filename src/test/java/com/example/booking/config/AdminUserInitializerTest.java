package com.example.booking.config;

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

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;

/**
 * Unit tests for AdminUserInitializer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserInitializer Tests")
public class AdminUserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserInitializer initializer;

    @BeforeEach
    void setUp() {
        // Default setup
    }

    // ========== run() Tests - Admin User Not Exists ==========

    @Test
    @DisplayName("shouldCreateAdminUser_whenNoAdminExists")
    void shouldCreateAdminUser_whenNoAdminExists() throws Exception {
        // Given
        Page<User> emptyPage = new PageImpl<>(java.util.Collections.emptyList());
        when(userRepository.findByRole(UserRole.ADMIN, any(Pageable.class))).thenReturn(emptyPage);
        
        User savedUser = new User();
        savedUser.setUsername("admin");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        initializer.run();

        // Then
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN, any(Pageable.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("shouldSetCorrectAdminUserProperties_whenCreatingAdmin")
    void shouldSetCorrectAdminUserProperties_whenCreatingAdmin() throws Exception {
        // Given
        Page<User> emptyPage = new PageImpl<>(java.util.Collections.emptyList());
        when(userRepository.findByRole(UserRole.ADMIN, any(Pageable.class))).thenReturn(emptyPage);
        
        User savedUser = new User();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("admin", user.getUsername());
            assertEquals("admin@bookeat.vn", user.getEmail());
            assertEquals(UserRole.ADMIN, user.getRole());
            assertTrue(Boolean.TRUE.equals(user.getActive()));
            assertNotNull(user.getPassword());
            return savedUser;
        });

        // When
        initializer.run();

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ========== run() Tests - Admin User Already Exists ==========

    @Test
    @DisplayName("shouldNotCreateAdminUser_whenAdminAlreadyExists")
    void shouldNotCreateAdminUser_whenAdminAlreadyExists() throws Exception {
        // Given
        User existingAdmin = new User();
        existingAdmin.setUsername("admin");
        existingAdmin.setRole(UserRole.ADMIN);
        
        Page<User> adminPage = new PageImpl<>(java.util.Collections.singletonList(existingAdmin));
        when(userRepository.findByRole(UserRole.ADMIN, any(Pageable.class))).thenReturn(adminPage);

        // When
        initializer.run();

        // Then
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN, any(Pageable.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("shouldHandleEmptyArgs_whenRunning")
    void shouldHandleEmptyArgs_whenRunning() throws Exception {
        // Given
        Page<User> emptyPage = new PageImpl<>(java.util.Collections.emptyList());
        when(userRepository.findByRole(UserRole.ADMIN, any(Pageable.class))).thenReturn(emptyPage);
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        assertDoesNotThrow(() -> {
            initializer.run();
        });

        // Then
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN, any(Pageable.class));
    }

    @Test
    @DisplayName("shouldHandleMultipleArgs_whenRunning")
    void shouldHandleMultipleArgs_whenRunning() throws Exception {
        // Given
        Page<User> emptyPage = new PageImpl<>(java.util.Collections.emptyList());
        when(userRepository.findByRole(UserRole.ADMIN, any(Pageable.class))).thenReturn(emptyPage);
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        assertDoesNotThrow(() -> {
            initializer.run("arg1", "arg2", "arg3");
        });

        // Then
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN, any(Pageable.class));
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("shouldHandleNullPage_whenNoAdminExists")
    void shouldHandleNullPage_whenNoAdminExists() throws Exception {
        // Given
        when(userRepository.findByRole(UserRole.ADMIN, any(Pageable.class)))
            .thenReturn(new PageImpl<>(java.util.Collections.emptyList()));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        assertDoesNotThrow(() -> {
            initializer.run();
        });

        // Then
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN, any(Pageable.class));
        verify(userRepository, times(1)).save(any(User.class));
    }
}

