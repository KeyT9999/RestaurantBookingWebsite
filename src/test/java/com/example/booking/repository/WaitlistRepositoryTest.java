package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.WaitlistStatus;
import com.example.booking.domain.User;

/**
 * Unit tests for WaitlistRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("WaitlistRepository Tests")
public class WaitlistRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Test
    @DisplayName("shouldFindWaitlistsByRestaurantAndStatus_successfully")
    void shouldFindWaitlistsByRestaurantAndStatus_successfully() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        User user = new User();
        user.setEmail("customer@test.com");
        entityManager.persist(user);

        Customer customer = new Customer();
        customer.setUser(user);
        entityManager.persist(customer);

        Waitlist waitlist = new Waitlist();
        waitlist.setRestaurant(restaurant);
        waitlist.setCustomer(customer);
        waitlist.setStatus(WaitlistStatus.WAITING);
        entityManager.persistAndFlush(waitlist);

        // When
        List<Waitlist> found = waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            restaurant.getRestaurantId(), WaitlistStatus.WAITING);

        // Then
        assertTrue(found.size() > 0);
    }
}

