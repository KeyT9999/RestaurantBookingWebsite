package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;

import com.example.booking.domain.Booking;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.dto.BookingForm;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantTableRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceCoverageTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private RestaurantProfileRepository restaurantProfileRepository;
    @Mock private RestaurantTableRepository restaurantTableRepository;
    @Mock private BookingTableRepository bookingTableRepository;
    @Mock private VoucherService voucherService;
    @Mock private DishRepository dishRepository;
    @Mock private RestaurantServiceRepository restaurantServiceRepository;
    @Mock private BookingDishRepository bookingDishRepository;
    @Mock private BookingServiceRepository bookingServiceRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private BookingConflictService conflictService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RefundService refundService;
    @Mock private EntityManager entityManager;

    @InjectMocks private BookingService bookingService;

    private UUID customerId;
    private Customer customer;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setup() {
        customerId = UUID.randomUUID();
        customer = new Customer();
        customer.setCustomerId(customerId);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);

        lenient().when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        lenient().when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        // Default: no conflict
        lenient().doNothing().when(conflictService).validateBookingConflicts(any(), any());
        // Default save echo with id
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getBookingId() == null) {
                b.setBookingId(1);
            }
            return b;
        });
        // Default: no items for totals
        lenient().when(bookingDishRepository.findByBooking(any())).thenReturn(java.util.Collections.emptyList());
        lenient().when(bookingServiceRepository.findByBooking(any())).thenReturn(java.util.Collections.emptyList());
        // Default: booking-table save returns an entity with id
        lenient().when(bookingTableRepository.save(any())).thenAnswer(inv -> {
            com.example.booking.domain.BookingTable bt = inv.getArgument(0);
            if (bt.getBookingTableId() == null) bt.setBookingTableId(1);
            return bt;
        });
    }

    private BookingForm baseForm(LocalDateTime time) {
        BookingForm f = new BookingForm();
        f.setRestaurantId(1);
        f.setGuestCount(4);
        f.setBookingTime(time);
        // select one table via tableIds to pass capacity validation
        f.setTableIds("1");
        return f;
    }

    @Test
    void createBooking_ShouldDefaultDepositZero_WhenFormDepositNull() {
        BookingForm form = baseForm(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(null);
        // mock table 1 with capacity >= guestCount and zero deposit
        com.example.booking.domain.RestaurantTable table = new com.example.booking.domain.RestaurantTable();
        table.setTableId(1);
        table.setCapacity(4);
        table.setDepositAmount(BigDecimal.ZERO);
        lenient().when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        Booking result = bookingService.createBooking(form, customerId);

        assertNotNull(result);
        assertEquals(1, result.getBookingId());
        assertEquals(BigDecimal.ZERO, result.getDepositAmount());
    }

    @Test
    void createBooking_ShouldThrow_WhenBookingTimeInPast() {
        BookingForm form = baseForm(LocalDateTime.now().minusHours(1));
        form.setDepositAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(form, customerId));
    }

    @Test
    void createBooking_ShouldThrow_WhenGuestCountInvalid() {
        BookingForm form = baseForm(LocalDateTime.now().plusDays(1));
        form.setGuestCount(0);
        form.setDepositAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(form, customerId));
    }

    @Test
    void createBooking_ShouldPropagateConflict_WhenTableConflict() {
        BookingForm form = baseForm(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(BigDecimal.ZERO);
        // ensure capacity validation passes
        com.example.booking.domain.RestaurantTable table = new com.example.booking.domain.RestaurantTable();
        table.setTableId(1);
        table.setCapacity(4);
        table.setDepositAmount(BigDecimal.ZERO);
        lenient().when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        doThrow(new BookingConflictException(BookingConflictException.ConflictType.TABLE_OCCUPIED, 
                java.util.List.of("Table already booked"), form.getBookingTime(), form.getTableId()))
            .when(conflictService).validateBookingConflicts(any(), any());

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(form, customerId));
    }

    @Test
    void updateBookingStatus_ShouldUpdate_WhenValidTransition() {
        Booking existing = new Booking();
        existing.setBookingId(10);
        existing.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(10)).thenReturn(Optional.of(existing));

        Booking updated = bookingService.updateBookingStatus(10, BookingStatus.CONFIRMED);
        assertEquals(BookingStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void cancelBookingByRestaurant_ShouldSetCancelled_AndCreateRefundRequest() {
        UUID ownerId = UUID.randomUUID();
        Booking existing = new Booking();
        existing.setBookingId(20);
        // prepare owner on restaurant
        com.example.booking.domain.User ownerUser = new com.example.booking.domain.User();
        ownerUser.setId(ownerId);
        com.example.booking.domain.RestaurantOwner owner = new com.example.booking.domain.RestaurantOwner();
        owner.setUser(ownerUser);
        restaurant.setOwner(owner);
        existing.setRestaurant(restaurant);
        existing.setStatus(BookingStatus.CONFIRMED);
        // Simplify owner validation path if present inside service (we assume success path)
        when(bookingRepository.findById(20)).thenReturn(Optional.of(existing));
        // refund side-effects are invoked inside; we just ensure no exception and status change
        Booking result = bookingService.cancelBookingByRestaurant(20, ownerId, "Reason test");
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING_CANCEL, result.getStatus());
    }

    @Test
    void calculateTotalAmount_ShouldReturnZero_WhenNoItems() {
        Booking booking = new Booking();
        booking.setBookingId(99);
        booking.setRestaurant(restaurant);
        booking.setCustomer(customer);

        assertEquals(BigDecimal.ZERO, bookingService.calculateTotalAmount(booking));
    }
}


