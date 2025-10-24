package com.example.booking.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.BookingForm;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.BookingConflictService;
import com.example.booking.service.BookingService;
import com.example.booking.service.VoucherService;

/**
 * Simplified integration tests for Booking functionality
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Import(BookingService.class)
class BookingIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private BookingConflictService bookingConflictService;

    private Customer customer;
    private RestaurantProfile restaurant;
    private RestaurantTable table;
    private UUID customerId;
    private User customerUser;
    private User ownerUser;
    private RestaurantOwner restaurantOwner;

    @BeforeEach
    void setUp() {
        customerUser = createTestUser("customer", UserRole.CUSTOMER);
        ownerUser = createTestUser("owner", UserRole.RESTAURANT_OWNER);

        entityManager.persistAndFlush(customerUser);
        entityManager.persistAndFlush(ownerUser);

        customer = createTestCustomer(customerUser);
        restaurantOwner = createTestOwner(ownerUser);
        entityManager.persistAndFlush(restaurantOwner);

        entityManager.persistAndFlush(customer);

        restaurant = createTestRestaurant(restaurantOwner);
        entityManager.persistAndFlush(restaurant);

        table = createTestTable(restaurant);
        entityManager.persistAndFlush(table);

        customerId = customer.getCustomerId();
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void testBookingFlow_EndToEnd() {
        // Given
        BookingForm bookingForm = createValidBookingForm();

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals(customerId, booking.getCustomer().getCustomerId());
        assertEquals(restaurant.getRestaurantId(), booking.getRestaurant().getRestaurantId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertNotNull(booking.getCreatedAt());

        // Verify booking was saved to database
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(booking.getBookingId(), savedBooking.getBookingId());
    }

    @Test
    void testBookingAmountCalculation_ShouldBeCorrect() {
        // Given
        BookingForm bookingForm = createValidBookingForm();

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        BigDecimal totalAmount = bookingService.calculateTotalAmount(booking);
        assertEquals(booking.getDepositAmount(), totalAmount);
    }

    @Test
    void testBookingStatusFlow_ShouldUpdateCorrectly() {
        // Given
        BookingForm bookingForm = createValidBookingForm();

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertEquals(BookingStatus.PENDING, booking.getStatus());

        // Update status
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Verify status update
        Booking updatedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(updatedBooking);
        assertEquals(BookingStatus.CONFIRMED, updatedBooking.getStatus());
    }

    // ==================== HELPER METHODS ====================

    private Customer createTestCustomer(User user) {
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName(user.getFullName());
        return customer;
    }

    private RestaurantProfile createTestRestaurant(RestaurantOwner owner) {
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setOwner(owner);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("123 Test Street");
        restaurant.setPhone("0987654321");
        restaurant.setTermsAccepted(true);
        return restaurant;
    }

    private RestaurantTable createTestTable(RestaurantProfile restaurant) {
        RestaurantTable table = new RestaurantTable();
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setRestaurant(restaurant);
        table.setStatus(TableStatus.AVAILABLE);
        table.setDepositAmount(new BigDecimal("100000"));
        return table;
    }

    private RestaurantOwner createTestOwner(User user) {
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(user);
        owner.setOwnerName(user.getFullName());
        return owner;
    }

    private User createTestUser(String suffix, UserRole role) {
        User user = new User();
        user.setUsername(suffix + "_user");
        user.setEmail(suffix + "@example.com");
        user.setPassword("Password123!");
        user.setFullName("Test " + suffix);
        user.setPhoneNumber("0987654321");
        user.setRole(role);
        return user;
    }

    private BookingForm createValidBookingForm() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(restaurant.getRestaurantId());
        form.setTableId(table.getTableId());
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setNote("Test booking");
        return form;
    }
}
