package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.BookingForm;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private BookingTableRepository bookingTableRepository;

    /**
     * Tạo booking mới
     */
    public Booking createBooking(BookingForm form, UUID customerId) {
        // Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Validate restaurant exists
        restaurantProfileRepository.findById(form.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Validate booking time
        validateBookingTime(form.getBookingTime());

        // Create booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setBookingTime(form.getBookingTime());
        booking.setNumberOfGuests(form.getGuestCount());
        booking.setDepositAmount(form.getDepositAmount() != null ? form.getDepositAmount() : BigDecimal.ZERO);
        booking.setStatus(BookingStatus.PENDING);

        // Save booking first
        booking = bookingRepository.save(booking);

        // Assign table if specified
        if (form.getTableId() != null) {
            assignTableToBooking(booking, form.getTableId());
        }

        return booking;
    }

    /**
     * Cập nhật booking
     */
    public Booking updateBooking(Integer bookingId, BookingForm form, UUID customerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Check if customer owns this booking
        if (!booking.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only edit your own bookings");
        }

        // Check if booking can be edited
        if (!booking.canBeEdited()) {
            throw new IllegalArgumentException("This booking cannot be edited");
        }

        // Validate booking time
        validateBookingTime(form.getBookingTime());

        // Update booking fields
        booking.setBookingTime(form.getBookingTime());
        booking.setNumberOfGuests(form.getGuestCount());
        booking.setDepositAmount(form.getDepositAmount() != null ? form.getDepositAmount() : BigDecimal.ZERO);

        // Update table assignment if changed
        if (form.getTableId() != null) {
            // Remove existing table assignments
            bookingTableRepository.deleteByBooking(booking);

            // Assign new table
            assignTableToBooking(booking, form.getTableId());
        }

        return bookingRepository.save(booking);
    }

    /**
     * Hủy booking
     */
    public Booking cancelBooking(Integer bookingId, UUID customerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Check if customer owns this booking
        if (!booking.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only cancel your own bookings");
        }

        // Check if booking can be cancelled
        if (!booking.canBeCancelled()) {
            throw new IllegalArgumentException("This booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    /**
     * Lấy danh sách booking của customer
     */
    @Transactional(readOnly = true)
    public List<Booking> findBookingsByCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        return bookingRepository.findByCustomerOrderByBookingTimeDesc(customer);
    }

    /**
     * Lấy booking theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Booking> findBookingById(Integer bookingId) {
        return bookingRepository.findById(bookingId);
    }

    /**
     * Lấy danh sách bàn trống trong khung giờ
     */
    @Transactional(readOnly = true)
    public List<RestaurantTable> findAvailableTables(Integer restaurantId, LocalDateTime bookingTime,
            Integer guestCount) {
        // Validate restaurant exists
        restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Calculate time range (2 hours before and after booking time)
        LocalDateTime startTime = bookingTime.minusHours(2);
        LocalDateTime endTime = bookingTime.plusHours(2);

        // Find tables that are not booked in this time range
        List<RestaurantTable> allTables = restaurantTableRepository
                .findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount);

        return allTables.stream()
                .filter(table -> !isTableBookedInTimeRange(table, startTime, endTime))
                .toList();
    }

    /**
     * Validate booking time
     */
    private void validateBookingTime(LocalDateTime bookingTime) {
        if (bookingTime == null) {
            throw new IllegalArgumentException("Booking time cannot be null");
        }

        LocalDateTime minimumTime = LocalDateTime.now().plusMinutes(30);
        if (bookingTime.isBefore(minimumTime)) {
            throw new IllegalArgumentException("Booking time must be at least 30 minutes from now");
        }
    }

    /**
     * Assign table to booking
     */
    private void assignTableToBooking(Booking booking, Integer tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        // Check if table is available
        LocalDateTime startTime = booking.getBookingTime().minusHours(2);
        LocalDateTime endTime = booking.getBookingTime().plusHours(2);

        if (isTableBookedInTimeRange(table, startTime, endTime)) {
            throw new IllegalArgumentException("Table is not available at the requested time");
        }

        // Create booking table assignment
        BookingTable bookingTable = new BookingTable(booking, table);
        bookingTableRepository.save(bookingTable);
    }

    /**
     * Check if table is booked in time range
     */
    private boolean isTableBookedInTimeRange(RestaurantTable table, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingTableRepository.existsByTableAndBookingTimeRange(table, startTime, endTime);
    }

    /**
     * Get booking statistics
     */
    @Transactional(readOnly = true)
    public long getBookingCountByStatus(BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }

    /**
     * Get booking count in date range
     */
    @Transactional(readOnly = true)
    public long getBookingCountInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.countByBookingTimeBetween(startDate, endDate);
    }
}