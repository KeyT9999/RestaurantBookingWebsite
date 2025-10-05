package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.BookingDish;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantTable;

import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantService;

import com.example.booking.dto.BookingForm;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;

import com.example.booking.service.VoucherService;

import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.exception.BookingConflictException;


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
    private VoucherService voucherService;

    @Autowired
    private BookingDishRepository bookingDishRepository;

    @Autowired
    private BookingServiceRepository bookingServiceRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RestaurantServiceRepository restaurantServiceRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private BookingConflictService conflictService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Tạo booking mới
     */
    public Booking createBooking(BookingForm form, UUID customerId) {
        try {
            System.out.println("🚨🚨🚨 BOOKING SERVICE CREATE BOOKING CALLED! 🚨🚨🚨");
            System.out.println("🔍 BookingService.createBooking() called - Transaction started");
            System.out.println("   Customer ID: " + customerId);
            System.out.println("   Restaurant ID: " + form.getRestaurantId());
            System.out.println("   Table ID: " + form.getTableId());
            System.out.println("   Guest Count: " + form.getGuestCount());
            System.out.println("   Booking Time: " + form.getBookingTime());

            // Validate conflicts BEFORE creating booking
            System.out.println("🔍 Validating booking conflicts...");
            try {
                conflictService.validateBookingConflicts(form, customerId);
                System.out.println("✅ No conflicts found, proceeding with booking creation");
            } catch (BookingConflictException e) {
                System.err.println("❌ Booking conflict detected: " + e.getMessage());
                throw e; // Re-throw to be handled by controller
            }

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

        // Process voucher if provided
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        String voucherCodeToApply = null;
        
        // Use voucher information from form if available
        if (form.getVoucherCodeApplied() != null && !form.getVoucherCodeApplied().trim().isEmpty()) {
            voucherCodeToApply = form.getVoucherCodeApplied();
            voucherDiscount = form.getVoucherDiscountAmount() != null ? form.getVoucherDiscountAmount() : BigDecimal.ZERO;
            System.out.println("✅ Using voucher from form: " + voucherCodeToApply + " with discount: " + voucherDiscount);
        } else if (form.getVoucherCode() != null && !form.getVoucherCode().trim().isEmpty()) {
            // Fallback to validation if no applied voucher
            try {
                // Validate voucher
                VoucherService.ValidationRequest validationReq = new VoucherService.ValidationRequest(
                    form.getVoucherCode(),
                    form.getRestaurantId(),
                    form.getBookingTime(),
                    form.getGuestCount(),
                    customer,
                    calculateOrderAmount(form) // Placeholder - should be calculated from actual order
                );
                
                VoucherService.ValidationResult validation = voucherService.validate(validationReq);
                if (validation.valid() && validation.calculatedDiscount() != null) {
                    voucherDiscount = validation.calculatedDiscount();
                    voucherCodeToApply = form.getVoucherCode();
                } else {
                    throw new IllegalArgumentException("Invalid voucher: " + validation.reason());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Voucher validation failed: " + e.getMessage());
            }
        }

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
            System.out.println("🚨🚨🚨 BOOKING REPOSITORY SAVE CALLED! 🚨🚨🚨");
            booking = bookingRepository.save(booking);
            System.out.println("✅ Booking saved successfully! ID: " + booking.getBookingId());

            // Force flush to ensure booking is persisted before creating BookingTable
            System.out.println("🔍 Flushing booking to database...");
            entityManager.flush();
            System.out.println("✅ Booking flushed successfully");
        } catch (Exception e) {
            System.err.println("❌ Error saving booking: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Apply voucher if valid
        if (voucherDiscount.compareTo(BigDecimal.ZERO) > 0 && voucherCodeToApply != null) {
            try {
                System.out.println("🔍 Applying voucher: " + voucherCodeToApply + " with discount: " + voucherDiscount);
                VoucherService.ApplyRequest applyReq = new VoucherService.ApplyRequest(
                    voucherCodeToApply,
                    form.getRestaurantId(),
                    customerId,
                    calculateOrderAmount(form),
                    booking.getBookingId()
                );
                
                VoucherService.ApplyResult applyResult = voucherService.applyToBooking(applyReq);
                if (!applyResult.success()) {
                    throw new IllegalArgumentException("Failed to apply voucher: " + applyResult.reason());
                }
                System.out.println("✅ Voucher applied successfully! Redemption ID: " + applyResult.redemptionId());
            } catch (Exception e) {
                throw new IllegalArgumentException("Voucher application failed: " + e.getMessage());
            }
        }

        // Assign table if specified
        if (form.getTableId() != null) {
            System.out.println("🔍 Assigning table to booking...");
            System.out.println("   Booking ID: " + booking.getBookingId());
            System.out.println("   Table ID: " + form.getTableId());
            try {
                assignTableToBooking(booking, form.getTableId());
                System.out.println("✅ Table assigned successfully");

                // Verify BookingTable was created
                System.out.println("🔍 Verifying BookingTable creation...");
                List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
                System.out.println("   Found " + bookingTables.size() + " BookingTable records");
                if (bookingTables.isEmpty()) {
                    System.err.println("❌ CRITICAL: No BookingTable records found after assignment!");
                } else {
                    for (BookingTable bt : bookingTables) {
                        System.out.println("   BookingTable ID: " + bt.getBookingTableId() +
                                ", Table: " + bt.getTable().getTableName());
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error assigning table: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } else {
            System.out.println("ℹ️ No table specified, skipping table assignment");
        }

        // Assign dishes if specified
        if (form.getDishIds() != null && !form.getDishIds().trim().isEmpty()) {
            System.out.println("🔍 Assigning dishes to booking...");
            try {
                assignDishesToBooking(booking, form.getDishIds());
                System.out.println("✅ Dishes assigned successfully");
            } catch (Exception e) {
                System.err.println("❌ Error assigning dishes: " + e.getMessage());
                e.printStackTrace();
                // Don't throw exception to avoid breaking booking creation
            }
        } else {
            System.out.println("ℹ️ No dishes specified, skipping dish assignment");
        }

        // Assign services if specified
        if (form.getServiceIds() != null && !form.getServiceIds().trim().isEmpty()) {
            System.out.println("🔍 Assigning services to booking...");
            try {
                assignServicesToBooking(booking, form.getServiceIds());
                System.out.println("✅ Services assigned successfully");
            } catch (Exception e) {
                System.err.println("❌ Error assigning services: " + e.getMessage());
                e.printStackTrace();
                // Don't throw exception to avoid breaking booking creation
            }
        } else {
            System.out.println("ℹ️ No services specified, skipping service assignment");
        }

        // Calculate and log total amount
        BigDecimal totalAmount = calculateTotalAmount(booking);
        System.out.println("💰 Final total booking amount: " + totalAmount);

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
     * Calculate order amount for voucher validation
     * This is a placeholder - should be calculated from actual order items
     */
    private BigDecimal calculateOrderAmount(BookingForm form) {
        // For now, use a placeholder amount
        // In a real implementation, this should calculate from order items
        return BigDecimal.valueOf(1000000); // 1,000,000 VND placeholder
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

        // Validate conflicts for update
        System.out.println("🔍 Validating booking update conflicts...");
        try {
            conflictService.validateBookingUpdateConflicts(bookingId, form, customerId);
            System.out.println("✅ No conflicts found, proceeding with booking update");
        } catch (BookingConflictException e) {
            System.err.println("❌ Booking update conflict detected: " + e.getMessage());
            throw e; // Re-throw to be handled by controller
        }

        // Validate booking time
        validateBookingTime(form.getBookingTime());

        // Update booking fields
        booking.setBookingTime(form.getBookingTime());
        booking.setNumberOfGuests(form.getGuestCount());
        booking.setDepositAmount(form.getDepositAmount() != null ? form.getDepositAmount() : BigDecimal.ZERO);

        // Update table assignment if changed
        if (form.getTableId() != null) {
            // Không cần update old table status - chỉ cần remove assignment
            if (!booking.getBookingTables().isEmpty()) {
                System.out.println("🔍 Removing old table assignments...");
                for (BookingTable bookingTable : booking.getBookingTables()) {
                    RestaurantTable oldTable = bookingTable.getTable();
                    System.out.println("✅ Removing assignment for table " + oldTable.getTableName());
                }
            }

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

        // Không cần update table status khi cancel booking
        // Status sẽ được quản lý riêng biệt
        if (!booking.getBookingTables().isEmpty()) {
            System.out.println("🔍 Cancelling table assignments...");
            for (BookingTable bookingTable : booking.getBookingTables()) {
                RestaurantTable table = bookingTable.getTable();
                System.out.println("✅ Cancelled assignment for table " + table.getTableName());
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    /**
     * Lấy danh sách booking của customer
     */
    @Transactional(readOnly = true)
    public List<Booking> findBookingsByCustomer(UUID customerId) {
        System.out.println("🔍 BookingService.findBookingsByCustomer() called for customer ID: " + customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        System.out.println("✅ Customer found: " + customer.getCustomerId());

        List<Booking> bookings = bookingRepository.findByCustomerOrderByBookingTimeDesc(customer);
        System.out.println("📋 Repository returned " + bookings.size() + " bookings");

        // Log each booking details
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            System.out.println("   Booking " + (i + 1) + ": ID=" + booking.getBookingId() +
                    ", Time=" + booking.getBookingTime() +
                    ", Status=" + booking.getStatus() +
                    ", Restaurant="
                    + (booking.getRestaurant() != null ? booking.getRestaurant().getRestaurantName() : "null"));
        }

        return bookings;
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
        System.out.println("   Booking ID: " + booking.getBookingId());
        System.out.println("   Booking status: " + booking.getStatus());

        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        System.out.println("✅ Table found: " + table.getTableName());
        System.out.println("   Table ID: " + table.getTableId());
        System.out.println("   Table status: " + table.getStatus());

        // Create booking table assignment
        System.out.println("🔍 Creating BookingTable assignment...");
        BookingTable bookingTable = new BookingTable(booking, table);
        System.out.println("   BookingTable object created");
        System.out.println("   BookingTable.booking: " + bookingTable.getBooking().getBookingId());
        System.out.println("   BookingTable.table: " + bookingTable.getTable().getTableName());
        System.out.println("   BookingTable.assignedAt: " + bookingTable.getAssignedAt());

        try {
            BookingTable savedBookingTable = bookingTableRepository.save(bookingTable);
            System.out.println("✅ BookingTable saved successfully");
            System.out.println("   Saved BookingTable ID: " + savedBookingTable.getBookingTableId());
        } catch (Exception e) {
            System.err.println("❌ Error saving BookingTable: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Không cần update table status khi tạo booking
        // Status sẽ được update tự động bởi TableStatusManagementService
        System.out.println("✅ Table assignment completed - status will be managed automatically");
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

    // ===== DISHES AND SERVICES MANAGEMENT =====

    /**
     * Assign dishes to booking
     */
    public void assignDishesToBooking(Booking booking, String dishIds) {
        if (dishIds == null || dishIds.trim().isEmpty()) {
            System.out.println("ℹ️ No dishes specified, skipping dish assignment");
            return;
        }

        System.out.println("🔍 Assigning dishes to booking: " + dishIds);

        // Parse dishIds: "1:2,3:1,5:3" -> Map<dishId, quantity>
        Map<Integer, Integer> dishMap = parseDishIds(dishIds);

        for (Map.Entry<Integer, Integer> entry : dishMap.entrySet()) {
            Integer dishId = entry.getKey();
            Integer quantity = entry.getValue();

            try {
                // Get dish from database
                Dish dish = dishRepository.findById(dishId)
                        .orElseThrow(() -> new IllegalArgumentException("Dish not found: " + dishId));

                // Create BookingDish
                BookingDish bookingDish = new BookingDish(booking, dish, quantity, dish.getPrice());
                bookingDishRepository.save(bookingDish);

                System.out.println(
                        "✅ Assigned dish: " + dish.getName() + " x" + quantity + " = " + bookingDish.getTotalPrice());

            } catch (Exception e) {
                System.err.println("❌ Error assigning dish " + dishId + ": " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        System.out.println("✅ All dishes assigned successfully");
    }

    /**
     * Assign services to booking
     */
    public void assignServicesToBooking(Booking booking, String serviceIds) {
        if (serviceIds == null || serviceIds.trim().isEmpty()) {
            System.out.println("ℹ️ No services specified, skipping service assignment");
            return;
        }

        System.out.println("🔍 Assigning services to booking: " + serviceIds);

        // Parse serviceIds: "1,2,3" -> List<serviceId>
        List<Integer> serviceIdList = parseServiceIds(serviceIds);

        for (Integer serviceId : serviceIdList) {
            try {
                // Get service from database
                RestaurantService service = restaurantServiceRepository.findById(serviceId)
                        .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));

                // Create BookingService
                com.example.booking.domain.BookingService bookingService = new com.example.booking.domain.BookingService(
                        booking, service, 1, service.getPrice());
                bookingServiceRepository.save(bookingService);

                System.out.println("✅ Assigned service: " + service.getName() + " = " + bookingService.getTotalPrice());

            } catch (Exception e) {
                System.err.println("❌ Error assigning service " + serviceId + ": " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        System.out.println("✅ All services assigned successfully");
    }

    /**
     * Calculate total amount for booking
     */
    public BigDecimal calculateTotalAmount(Booking booking) {
        BigDecimal total = BigDecimal.ZERO;

        // Add deposit amount
        total = total.add(booking.getDepositAmount());
        System.out.println("💰 Deposit amount: " + booking.getDepositAmount());

        // Add dishes total
        List<BookingDish> bookingDishes = bookingDishRepository.findByBooking(booking);
        if (!bookingDishes.isEmpty()) {
            BigDecimal dishesTotal = BigDecimal.ZERO;
            for (BookingDish bookingDish : bookingDishes) {
                dishesTotal = dishesTotal.add(bookingDish.getTotalPrice());
                System.out.println("🍽️ Dish: " + bookingDish.getDish().getName() +
                        " x" + bookingDish.getQuantity() +
                        " = " + bookingDish.getTotalPrice());
            }
            total = total.add(dishesTotal);
            System.out.println("💰 Dishes total: " + dishesTotal);
        }

        // Add services total
        List<com.example.booking.domain.BookingService> bookingServices = bookingServiceRepository
                .findByBooking(booking);
        if (!bookingServices.isEmpty()) {
            BigDecimal servicesTotal = BigDecimal.ZERO;
            for (com.example.booking.domain.BookingService bookingService : bookingServices) {
                servicesTotal = servicesTotal.add(bookingService.getTotalPrice());
                System.out.println("🔧 Service: " + bookingService.getService().getName() +
                        " x" + bookingService.getQuantity() +
                        " = " + bookingService.getTotalPrice());
            }
            total = total.add(servicesTotal);
            System.out.println("💰 Services total: " + servicesTotal);
        }

        System.out.println("💰 TOTAL AMOUNT: " + total);
        return total;
    }

    /**
     * Update booking with items
     */
    public Booking updateBookingWithItems(Integer bookingId, BookingForm form, UUID customerId) {
        System.out.println("🔍 Updating booking with items: " + bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Validate customer ownership
        if (!booking.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only update your own bookings");
        }

        // Validate booking can be updated
        if (!booking.canBeEdited()) {
            throw new IllegalArgumentException("Booking cannot be updated in current status");
        }

        // Update basic booking info
        booking.setBookingTime(form.getBookingTime());
        booking.setNumberOfGuests(form.getGuestCount());

        // Update deposit amount if table changed
        if (form.getTableId() != null && !form.getTableId().equals(getCurrentTableId(booking))) {
            RestaurantTable table = restaurantTableRepository.findById(form.getTableId())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));
            booking.setDepositAmount(table.getDepositAmount());

            // Update table assignment
            bookingTableRepository.deleteByBooking(booking);
            assignTableToBooking(booking, form.getTableId());
        }

        // Update dishes
        bookingDishRepository.deleteByBooking(booking);
        if (form.getDishIds() != null && !form.getDishIds().trim().isEmpty()) {
            assignDishesToBooking(booking, form.getDishIds());
        }

        // Update services
        bookingServiceRepository.deleteByBooking(booking);
        if (form.getServiceIds() != null && !form.getServiceIds().trim().isEmpty()) {
            assignServicesToBooking(booking, form.getServiceIds());
        }

        return bookingRepository.save(booking);
    }

    // Helper methods
    private Map<Integer, Integer> parseDishIds(String dishIds) {
        Map<Integer, Integer> dishMap = new HashMap<>();
        if (dishIds == null || dishIds.trim().isEmpty()) {
            return dishMap;
        }

        String[] pairs = dishIds.split(",");
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                Integer dishId = Integer.parseInt(parts[0].trim());
                Integer quantity = Integer.parseInt(parts[1].trim());
                dishMap.put(dishId, quantity);
            }
        }
        return dishMap;
    }

    private List<Integer> parseServiceIds(String serviceIds) {
        List<Integer> serviceIdList = new ArrayList<>();
        if (serviceIds == null || serviceIds.trim().isEmpty()) {
            return serviceIdList;
        }

        String[] ids = serviceIds.split(",");
        for (String id : ids) {
            if (!id.trim().isEmpty()) {
                serviceIdList.add(Integer.parseInt(id.trim()));
            }
        }
        return serviceIdList;
    }

    private Integer getCurrentTableId(Booking booking) {
        List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
        if (!bookingTables.isEmpty()) {
            return bookingTables.get(0).getTable().getTableId();
        }
        return null;
    }
}