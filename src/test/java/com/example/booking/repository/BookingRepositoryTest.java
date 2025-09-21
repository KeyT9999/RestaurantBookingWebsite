package com.example.booking.repository;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingStatus;
import com.example.booking.domain.DiningTable;
import com.example.booking.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DiningTableRepository diningTableRepository;

    private Restaurant restaurant;
    private DiningTable table;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        
        // Create and persist restaurant
        restaurant = new Restaurant("Test Restaurant", "A test restaurant", "123 Test St", "0123456789");
        restaurant = entityManager.persistAndFlush(restaurant);

        // Create and persist table
        table = new DiningTable(restaurant, "Table 1", 4, "A nice table");
        table = entityManager.persistAndFlush(table);
    }

    @Test
    void shouldFindBookingsByCustomerId() {
        // Given
        LocalDateTime bookingTime = LocalDateTime.now().plusHours(2);
        
        Booking booking1 = createBooking(customerId, bookingTime);
        Booking booking2 = createBooking(customerId, bookingTime.plusHours(1));
        Booking booking3 = createBooking(UUID.randomUUID(), bookingTime.plusHours(2)); // Different customer
        
        entityManager.persistAndFlush(booking1);
        entityManager.persistAndFlush(booking2);
        entityManager.persistAndFlush(booking3);

        // When
        List<Booking> bookings = bookingRepository.findByCustomerIdOrderByBookingTimeDesc(customerId);

        // Then
        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getBookingTime()).isAfter(bookings.get(1).getBookingTime()); // Descending order
    }

    @Test
    void shouldFindConflictingBookings() {
        // Given
        LocalDateTime baseTime = LocalDateTime.now().plusHours(2);
        
        // Create a booking at baseTime
        Booking existingBooking = createBooking(customerId, baseTime);
        existingBooking.setTableId(table.getId());
        existingBooking.setStatus(BookingStatus.CONFIRMED);
        entityManager.persistAndFlush(existingBooking);

        // When - Check for conflicts in a 2-hour window around baseTime
        LocalDateTime startTime = baseTime.minusHours(1);
        LocalDateTime endTime = baseTime.plusHours(1);
        UUID excludeId = UUID.randomUUID(); // Different ID to not exclude existing booking
        
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                table.getId(), startTime, endTime, excludeId);

        // Then
        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getId()).isEqualTo(existingBooking.getId());
    }

    @Test
    void shouldNotFindConflictingBookingsWhenExcluded() {
        // Given
        LocalDateTime baseTime = LocalDateTime.now().plusHours(2);
        
        Booking existingBooking = createBooking(customerId, baseTime);
        existingBooking.setTableId(table.getId());
        existingBooking.setStatus(BookingStatus.CONFIRMED);
        entityManager.persistAndFlush(existingBooking);

        // When - Exclude the existing booking
        LocalDateTime startTime = baseTime.minusHours(1);
        LocalDateTime endTime = baseTime.plusHours(1);
        
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                table.getId(), startTime, endTime, existingBooking.getId());

        // Then
        assertThat(conflicts).isEmpty();
    }

    @Test
    void shouldFindActiveBookingsByCustomer() {
        // Given
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
        
        Booking pendingBooking = createBooking(customerId, futureTime);
        pendingBooking.setStatus(BookingStatus.PENDING);
        
        Booking confirmedBooking = createBooking(customerId, futureTime.plusHours(1));
        confirmedBooking.setStatus(BookingStatus.CONFIRMED);
        
        Booking cancelledBooking = createBooking(customerId, futureTime.plusHours(2));
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        
        entityManager.persistAndFlush(pendingBooking);
        entityManager.persistAndFlush(confirmedBooking);
        entityManager.persistAndFlush(cancelledBooking);

        // When
        List<Booking> activeBookings = bookingRepository.findActiveBookingsByCustomer(customerId);

        // Then
        assertThat(activeBookings).hasSize(2);
        assertThat(activeBookings).extracting(Booking::getStatus)
                .containsExactlyInAnyOrder(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    }

    private Booking createBooking(UUID customerId, LocalDateTime bookingTime) {
        return new Booking(
                customerId,
                restaurant.getId(),
                table.getId(),
                4,
                bookingTime,
                new BigDecimal("100000"),
                "Test booking"
        );
    }
} 