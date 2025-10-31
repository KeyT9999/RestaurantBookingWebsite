package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.Booking;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;

/**
 * Unit tests for BookingRepository using @DataJpaTest
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BookingRepository Tests")
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    // ========== findById() Tests ==========

    @Test
    @DisplayName("shouldFindBookingById_successfully")
    void shouldFindBookingById_successfully() {
        // Given
        Booking booking = createTestBooking();
        entityManager.persistAndFlush(booking);

        // When
        Optional<Booking> found = bookingRepository.findById(booking.getBookingId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(booking.getBookingId(), found.get().getBookingId());
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenBookingNotFound")
    void shouldReturnEmpty_whenBookingNotFound() {
        // When
        Optional<Booking> found = bookingRepository.findById(999);

        // Then
        assertFalse(found.isPresent());
    }

    // ========== Helper Methods ==========

    private Booking createTestBooking() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        entityManager.persist(user);

        Customer customer = new Customer();
        customer.setUser(user);
        entityManager.persist(customer);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusHours(2));
        booking.setStatus(BookingStatus.PENDING);
        
        return booking;
    }
}

