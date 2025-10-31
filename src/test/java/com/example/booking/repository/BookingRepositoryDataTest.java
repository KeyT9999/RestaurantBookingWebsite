package com.example.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;

@DataJpaTest
class BookingRepositoryDataTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should compute booking statistics and filtered queries")
    void shouldQueryBookings() {
        var fixture = persistFixture();

        List<Booking> active = bookingRepository.findActiveBookingsByCustomer(fixture.customer);
        assertThat(active).extracting(Booking::getStatus)
                .containsExactly(BookingStatus.PENDING, BookingStatus.CONFIRMED);

        List<Booking> upcoming = bookingRepository.findUpcomingBookings(
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(2));
        assertThat(upcoming).hasSize(2);
        assertThat(upcoming.get(0).getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        List<Booking> noShows = bookingRepository.findNoShowBookings(LocalDateTime.now());
        assertThat(noShows).hasSize(1);
        assertThat(noShows.get(0).getBookingTime()).isBefore(LocalDateTime.now());

        BigDecimal pendingDeposit = bookingRepository.sumDepositByStatus(BookingStatus.PENDING);
        assertThat(pendingDeposit).isEqualTo(BigDecimal.valueOf(50));

        long countByRestaurant = bookingRepository.countByRestaurantAndStatus(fixture.restaurant,
                BookingStatus.CONFIRMED);
        assertThat(countByRestaurant).isEqualTo(1);

        List<Object[]> dailyStats = bookingRepository.findDailyBookingStats(
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertThat(dailyStats).isNotEmpty();
    }

    private Fixture persistFixture() {
        User ownerUser = new User();
        ownerUser.setUsername("owner");
        ownerUser.setEmail("owner@example.com");
        ownerUser.setPassword("password");
        ownerUser.setFullName("Owner");
        entityManager.persist(ownerUser);

        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(ownerUser);
        owner.setOwnerName("Owner");
        entityManager.persist(owner);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setOwner(owner);
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        User customerUser = new User();
        customerUser.setUsername("customer");
        customerUser.setEmail("customer@example.com");
        customerUser.setPassword("password");
        customerUser.setFullName("Customer");
        entityManager.persist(customerUser);

        Customer customer = new Customer();
        customer.setUser(customerUser);
        customer.setFullName("Customer");
        entityManager.persist(customer);

        Booking pending = new Booking(customer, restaurant, LocalDateTime.now().plusMinutes(30),
                2, BigDecimal.valueOf(50), "note");
        pending.setStatus(BookingStatus.PENDING);
        entityManager.persist(pending);

        Booking confirmed = new Booking(customer, restaurant, LocalDateTime.now().plusMinutes(60),
                4, BigDecimal.valueOf(100), "confirmed");
        confirmed.setStatus(BookingStatus.CONFIRMED);
        entityManager.persist(confirmed);

        Booking oldPending = new Booking(customer, restaurant, LocalDateTime.now().minusHours(2),
                2, BigDecimal.ZERO, "old");
        oldPending.setStatus(BookingStatus.PENDING);
        entityManager.persist(oldPending);

        entityManager.flush();

        Fixture fixture = new Fixture();
        fixture.customer = customer;
        fixture.restaurant = restaurant;
        return fixture;
    }

    private static class Fixture {
        Customer customer;
        RestaurantProfile restaurant;
    }
}
