package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;

/**
 * Unit tests for UserRepository using @DataJpaTest
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    // ========== findByEmail() Tests ==========

    @Test
    @DisplayName("shouldFindUserByEmail_successfully")
    void shouldFindUserByEmail_successfully() {
        // Given
        User user = createTestUser("test@test.com");
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@test.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenEmailNotFound")
    void shouldReturnEmpty_whenEmailNotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("notfound@test.com");

        // Then
        assertFalse(found.isPresent());
    }

    // ========== findByRole() Tests ==========

    @Test
    @DisplayName("shouldFindUsersByRole_successfully")
    void shouldFindUsersByRole_successfully() {
        // Given
        User admin1 = createTestUser("admin1@test.com");
        admin1.setRole(UserRole.ADMIN);
        entityManager.persistAndFlush(admin1);

        User admin2 = createTestUser("admin2@test.com");
        admin2.setRole(UserRole.ADMIN);
        entityManager.persistAndFlush(admin2);

        User customer = createTestUser("customer@test.com");
        customer.setRole(UserRole.CUSTOMER);
        entityManager.persistAndFlush(customer);

        // When
        Page<User> admins = userRepository.findByRole(UserRole.ADMIN, Pageable.unpaged());

        // Then
        assertEquals(2, admins.getTotalElements());
        admins.forEach(u -> assertEquals(UserRole.ADMIN, u.getRole()));
    }

    // ========== Helper Methods ==========

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email.split("@")[0]);
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        return user;
    }
}

