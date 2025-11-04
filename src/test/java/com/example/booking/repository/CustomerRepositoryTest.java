package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;

/**
 * Unit tests for CustomerRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CustomerRepository Tests")
public class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("shouldFindCustomerByUserId_successfully")
    void shouldFindCustomerByUserId_successfully() {
        // Given
        User user = new User();
        user.setEmail("customer@test.com");
        entityManager.persist(user);

        Customer customer = new Customer();
        customer.setUser(user);
        entityManager.persistAndFlush(customer);

        // When
        Optional<Customer> found = customerRepository.findByUserId(user.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getUser().getId());
    }
}

