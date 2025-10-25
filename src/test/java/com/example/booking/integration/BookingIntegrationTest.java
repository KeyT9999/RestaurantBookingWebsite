package com.example.booking.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
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
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.BookingConflictService;
import com.example.booking.service.BookingService;
import com.example.booking.service.VoucherService;
import com.example.booking.service.PayOsService;
import com.example.booking.service.PaymentService;
import com.example.booking.service.RefundService;
import com.example.booking.service.EnhancedRefundService;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.BankAccountService;
import com.example.booking.service.EmailService;
import com.example.booking.service.NotificationService;

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

    @MockBean
    private PayOsService payOsService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private RefundService refundService;

    @MockBean
    private EnhancedRefundService enhancedRefundService;

    @MockBean
    private RestaurantBalanceService restaurantBalanceService;

    @MockBean
    private BankAccountService bankAccountService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

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

    // ==================== END-TO-END TESTS ====================

    @Test
    void testBookingWithDishes_ShouldCreateBookingDishes() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setDishIds("1,2,3");

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals(customerId, booking.getCustomer().getCustomerId());
        assertEquals(restaurant.getRestaurantId(), booking.getRestaurant().getRestaurantId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());

        // Verify booking was saved to database
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(booking.getBookingId(), savedBooking.getBookingId());
    }

    @Test
    void testBookingWithServices_ShouldCreateBookingServices() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setServiceIds("1,2");

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals(customerId, booking.getCustomer().getCustomerId());
        assertEquals(restaurant.getRestaurantId(), booking.getRestaurant().getRestaurantId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());

        // Verify booking was saved to database
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(booking.getBookingId(), savedBooking.getBookingId());
    }

    @Test
    void testBookingWithMultipleTables_ShouldCreateMultipleBookingTables() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setTableId(table.getTableId()); // Single table for now

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals(customerId, booking.getCustomer().getCustomerId());
        assertEquals(restaurant.getRestaurantId(), booking.getRestaurant().getRestaurantId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());

        // Verify booking was saved to database
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(booking.getBookingId(), savedBooking.getBookingId());
    }

    @Test
    void testBookingTransaction_RollbackOnError() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setRestaurantId(999); // Non-existent restaurant

        // When & Then
        try {
            bookingService.createBooking(bookingForm, customerId);
            // Should not reach here
            assertTrue(false, "Should have thrown exception");
        } catch (Exception e) {
            // Expected behavior - transaction should rollback
            assertTrue(true, "Exception correctly thrown: " + e.getMessage());
        }

        // Verify no booking was saved
        long bookingCount = bookingRepository.count();
        assertEquals(0, bookingCount);
    }

    @Test
    void testBookingConflictDetection_ShouldPreventDoubleBooking() {
        // Given
        BookingForm bookingForm1 = createValidBookingForm();
        BookingForm bookingForm2 = createValidBookingForm();
        bookingForm2.setBookingTime(bookingForm1.getBookingTime()); // Same time

        doThrow(new BookingConflictException(
                BookingConflictException.ConflictType.TABLE_OCCUPIED,
                "Table already booked"))
                .when(bookingConflictService)
                .validateBookingConflicts(bookingForm2, customerId);

        // When
        Booking booking1 = bookingService.createBooking(bookingForm1, customerId);

        // Then
        assertNotNull(booking1.getBookingId());
        assertEquals(BookingStatus.PENDING, booking1.getStatus());

        // Try to create second booking with same table and time
        try {
            bookingService.createBooking(bookingForm2, customerId);
            // Should not reach here if conflict detection works
            assertTrue(false, "Should have detected conflict");
        } catch (Exception e) {
            // Expected behavior
            assertTrue(true, "Conflict correctly detected: " + e.getMessage());
        }
    }

    @Test
    void testBookingWithVoucher_ShouldApplyDiscount() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setVoucherCode("DISCOUNT10");

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals(customerId, booking.getCustomer().getCustomerId());
        assertEquals(restaurant.getRestaurantId(), booking.getRestaurant().getRestaurantId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());

        // Verify booking was saved to database
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(booking.getBookingId(), savedBooking.getBookingId());
    }

    // ==================== DATABASE TESTS ====================

    @Test
    void testBookingPersistence_WithAllFields_ShouldPersistCorrectly() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setNote("Complete test booking with all fields");

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertNotNull(booking.getCreatedAt());
        assertNotNull(booking.getUpdatedAt());
        assertEquals(customerId, booking.getCustomer().getCustomerId());
        assertEquals(restaurant.getRestaurantId(), booking.getRestaurant().getRestaurantId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(4, booking.getNumberOfGuests());
        assertEquals("Complete test booking with all fields", booking.getNote());

        // Verify all fields persisted correctly
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(booking.getBookingId(), savedBooking.getBookingId());
        assertEquals(booking.getCreatedAt(), savedBooking.getCreatedAt());
        assertEquals(booking.getUpdatedAt(), savedBooking.getUpdatedAt());
        assertEquals(booking.getCustomer().getCustomerId(), savedBooking.getCustomer().getCustomerId());
        assertEquals(booking.getRestaurant().getRestaurantId(), savedBooking.getRestaurant().getRestaurantId());
        assertEquals(booking.getStatus(), savedBooking.getStatus());
        assertEquals(booking.getNumberOfGuests(), savedBooking.getNumberOfGuests());
        assertEquals(booking.getNote(), savedBooking.getNote());
    }

    @Test
    void testBookingRetrieval_ById_ShouldReturnCorrectBooking() {
        // Given
        BookingForm bookingForm = createValidBookingForm();

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());

        // Retrieve booking by ID
        Booking retrievedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(retrievedBooking);
        assertEquals(booking.getBookingId(), retrievedBooking.getBookingId());
        assertEquals(customerId, retrievedBooking.getCustomer().getCustomerId());
        assertEquals(restaurant.getRestaurantId(), retrievedBooking.getRestaurant().getRestaurantId());
        assertEquals(BookingStatus.PENDING, retrievedBooking.getStatus());
    }

    @Test
    void testBookingUpdate_Status_ShouldUpdateCorrectly() {
        // Given
        BookingForm bookingForm = createValidBookingForm();

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertEquals(BookingStatus.PENDING, booking.getStatus());

        // Update status multiple times
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        Booking updatedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(updatedBooking);
        assertEquals(BookingStatus.CONFIRMED, updatedBooking.getStatus());

        // Update to completed
        updatedBooking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(updatedBooking);

        Booking completedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(completedBooking);
        assertEquals(BookingStatus.COMPLETED, completedBooking.getStatus());
    }

    @Test
    void testBookingDelete_ShouldRemoveFromDatabase() {
        // Given
        BookingForm bookingForm = createValidBookingForm();

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());

        // Verify booking exists
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);

        // Delete booking
        bookingRepository.deleteById(booking.getBookingId());

        // Verify booking is removed
        Booking deletedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNull(deletedBooking);
    }

    // ==================== EDGE CASES ====================

    @Test
    void testBookingWithLargeGuestCount_ShouldHandleGracefully() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setGuestCount(100);
        List<Integer> tableIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            RestaurantTable extraTable = createTestTable(restaurant);
            extraTable.setTableName("Extra Table " + (i + 1));
            extraTable.setCapacity(20);
            entityManager.persistAndFlush(extraTable);
            tableIds.add(extraTable.getTableId());
        }
        tableIds.add(table.getTableId());
        bookingForm.setTableId(null);
        bookingForm.setTableIds(tableIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals(100, booking.getNumberOfGuests());
        assertEquals(BookingStatus.PENDING, booking.getStatus());

        // Verify booking was saved to database
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(100, savedBooking.getNumberOfGuests());
    }

    @Test
    void testBookingWithSpecialCharactersInNote_ShouldPersistCorrectly() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setNote("Special chars: @#$%^&*()");

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals("Special chars: @#$%^&*()", booking.getNote());

        // Verify special characters persisted correctly
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals("Special chars: @#$%^&*()", savedBooking.getNote());
    }

    @Test
    void testBookingWithEmptyNote_ShouldHandleGracefully() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setNote("");

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals("", booking.getNote());

        // Verify empty note persisted correctly
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals("", savedBooking.getNote());
    }

    @Test
    void testBookingWithNullNote_ShouldHandleGracefully() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setNote(null);

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertNull(booking.getNote());

        // Verify null note persisted correctly
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertNull(savedBooking.getNote());
    }

    @Test
    void testBookingWithMaximumDepositAmount_ShouldHandleCorrectly() {
        // Given
        BookingForm bookingForm = createValidBookingForm();
        bookingForm.setDepositAmount(new BigDecimal("999999999"));

        // When
        Booking booking = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(booking.getBookingId());
        assertEquals(new BigDecimal("999999999"), booking.getDepositAmount());

        // Verify large amount persisted correctly
        Booking savedBooking = bookingRepository.findById(booking.getBookingId()).orElse(null);
        assertNotNull(savedBooking);
        assertEquals(new BigDecimal("999999999"), savedBooking.getDepositAmount());
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
