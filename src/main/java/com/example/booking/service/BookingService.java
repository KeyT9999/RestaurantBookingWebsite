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
import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.repository.NotificationRepository;

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

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Tạo booking mới
     */
    public Booking createBooking(BookingForm form, UUID customerId) {
        try {
            System.out.println("🔍 BookingService.createBooking() called - Transaction started");
            System.out.println("   Customer ID: " + customerId);
            System.out.println("   Restaurant ID: " + form.getRestaurantId());
            System.out.println("   Table ID: " + form.getTableId());
            System.out.println("   Guest Count: " + form.getGuestCount());
            System.out.println("   Booking Time: " + form.getBookingTime());

        // Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        System.out.println("✅ Customer found: " + customer.getCustomerId());

        // Validate restaurant exists
        try {
            restaurantProfileRepository.findById(form.getRestaurantId())
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
            System.out.println("✅ Restaurant found: " + form.getRestaurantId());
        } catch (Exception e) {
            System.err.println("❌ Restaurant validation failed: " + e.getMessage());
            System.err.println("   Looking for restaurant ID: " + form.getRestaurantId());
            throw e;
        }

        // Validate booking time
        validateBookingTime(form.getBookingTime());
        System.out.println("✅ Booking time validated");

        // Create booking
        System.out.println("🔍 Creating booking object...");
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setBookingTime(form.getBookingTime());
        booking.setNumberOfGuests(form.getGuestCount());

        // Set deposit amount from table if table is selected, otherwise use form value
        BigDecimal depositAmount = BigDecimal.ZERO;
        if (form.getTableId() != null) {
            System.out.println("🔍 Looking for table ID: " + form.getTableId());
            RestaurantTable table = restaurantTableRepository.findById(form.getTableId())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));
            depositAmount = table.getDepositAmount();
            System.out.println("✅ Table found, deposit amount: " + depositAmount);
        } else if (form.getDepositAmount() != null) {
            depositAmount = form.getDepositAmount();
            System.out.println("✅ Using form deposit amount: " + depositAmount);
        }
        booking.setDepositAmount(depositAmount);
        booking.setStatus(BookingStatus.PENDING);
        System.out.println("✅ Booking object created with status: " + booking.getStatus());

        // Save booking first
        System.out.println("🔍 Saving booking to database...");
        try {
            booking = bookingRepository.save(booking);
            System.out.println("✅ Booking saved successfully! ID: " + booking.getBookingId());
        } catch (Exception e) {
            System.err.println("❌ Error saving booking: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Assign table if specified
        if (form.getTableId() != null) {
            System.out.println("🔍 Assigning table to booking...");
            try {
                assignTableToBooking(booking, form.getTableId());
                System.out.println("✅ Table assigned successfully");
            } catch (Exception e) {
                System.err.println("❌ Error assigning table: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } else {
            System.out.println("ℹ️ No table specified, skipping table assignment");
        }

        // Create notification for customer
        System.out.println("🔍 Creating notification...");
        try {
            createBookingNotification(booking);
            System.out.println("✅ Notification created successfully");
        } catch (Exception e) {
            System.err.println("❌ Error creating notification: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception to avoid breaking booking creation
        }

        System.out.println("🎉 Booking creation completed successfully!");
        return booking;
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR in createBooking: " + e.getMessage());
            System.err.println("❌ Exception type: " + e.getClass().getName());
            System.err.println("❌ Transaction will be rolled back!");
            e.printStackTrace();
            throw e;
        }
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
        System.out.println("🔍 Validating booking time: " + bookingTime);

        if (bookingTime == null) {
            System.err.println("❌ Booking time is null");
            throw new IllegalArgumentException("Booking time cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minimumTime = now.plusMinutes(30);
        LocalDateTime maximumTime = now.plusDays(30);

        System.out.println("   Current time: " + now);
        System.out.println("   Minimum time: " + minimumTime);
        System.out.println("   Maximum time: " + maximumTime);
        System.out.println("   Booking time: " + bookingTime);

        if (bookingTime.isBefore(minimumTime)) {
            System.err.println("❌ Booking time too early");
            throw new IllegalArgumentException("Booking time must be at least 30 minutes from now");
        }

        if (bookingTime.isAfter(maximumTime)) {
            System.err.println("❌ Booking time too far in future");
            throw new IllegalArgumentException("Booking time cannot be more than 30 days in the future");
        }

        System.out.println("✅ Booking time validation passed");
    }

    /**
     * Assign table to booking
     */
    private void assignTableToBooking(Booking booking, Integer tableId) {
        System.out.println("🔍 assignTableToBooking called with tableId: " + tableId);

        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        System.out.println("✅ Table found: " + table.getTableName());

        // Check if table is available
        LocalDateTime startTime = booking.getBookingTime().minusHours(2);
        LocalDateTime endTime = booking.getBookingTime().plusHours(2);
        System.out.println("🔍 Checking availability from " + startTime + " to " + endTime);

        if (isTableBookedInTimeRange(table, startTime, endTime)) {
            System.err.println("❌ Table is booked in time range");
            throw new IllegalArgumentException("Table is not available at the requested time");
        }
        System.out.println("✅ Table is available");

        // Create booking table assignment
        System.out.println("🔍 Creating BookingTable assignment...");
        BookingTable bookingTable = new BookingTable(booking, table);
        bookingTableRepository.save(bookingTable);
        System.out.println("✅ BookingTable saved successfully");
    }

    /**
     * Check if table is booked in time range
     */
    private boolean isTableBookedInTimeRange(RestaurantTable table, LocalDateTime startTime, LocalDateTime endTime) {
        System.out.println(
                "🔍 Checking if table " + table.getTableName() + " is booked from " + startTime + " to " + endTime);
        boolean isBooked = bookingTableRepository.existsByTableAndBookingTimeRange(table, startTime, endTime);
        System.out.println("   Result: " + (isBooked ? "BOOKED" : "AVAILABLE"));
        return isBooked;
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

    /**
     * Create notification for booking creation
     */
    private void createBookingNotification(Booking booking) {
        System.out.println("🔍 Creating notification for booking ID: " + booking.getBookingId());
        try {
            Notification notification = new Notification();
            notification.setRecipientUserId(booking.getCustomer().getUser().getId());
            notification.setType(NotificationType.BOOKING_CONFIRMED);
            notification.setTitle("Đặt bàn thành công");
            notification.setContent(String.format(
                    "Bạn đã đặt bàn thành công! Booking ID: %d, Thời gian: %s, Số khách: %d",
                    booking.getBookingId(),
                    booking.getBookingTime().toString(),
                    booking.getNumberOfGuests()));
            notification.setLinkUrl("/booking/my");
            notification.setStatus(NotificationStatus.SENT);
            notification.setPriority(1);
            notification.setPublishAt(LocalDateTime.now());

            System.out.println("🔍 Saving notification...");
            notificationRepository.save(notification);
            System.out.println("✅ Created booking notification for customer: " + booking.getCustomer().getCustomerId());
        } catch (Exception e) {
            System.err.println("❌ Error creating booking notification: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception to avoid breaking booking creation
        }
    }
}