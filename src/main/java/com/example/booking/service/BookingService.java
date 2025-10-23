package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingDish;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Dish;
import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantTableRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


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

            // Validate customer FIRST
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        System.out.println("✅ Customer found: " + customer.getCustomerId());

        // Validate restaurant exists and get restaurant object
        RestaurantProfile restaurant;
        try {
            restaurant = restaurantProfileRepository.findById(form.getRestaurantId())
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
            System.out.println(
                    "✅ Restaurant found: " + restaurant.getRestaurantId() + " - " + restaurant.getRestaurantName());
        } catch (Exception e) {
            System.err.println("❌ Restaurant validation failed: " + e.getMessage());
            System.err.println("   Looking for restaurant ID: " + form.getRestaurantId());
            throw e;
        }

        // Validate booking time
        validateBookingTime(form.getBookingTime());
        System.out.println("✅ Booking time validated");

        // Validate guest count
        validateGuestCount(form.getGuestCount());
        System.out.println("✅ Guest count validated");

        // Validate table capacity BEFORE conflict validation
        System.out.println("🔍 VALIDATING TABLE CAPACITY BEFORE CONFLICT CHECK...");
        validateTableCapacity(form);
        System.out.println("✅ Table capacity validated");

        // Validate conflicts AFTER basic validations
        System.out.println("🔍 NOW VALIDATING BOOKING CONFLICTS...");
        try {
            conflictService.validateBookingConflicts(form, customerId);
            System.out.println("✅ No conflicts found, proceeding with booking creation");
        } catch (BookingConflictException e) {
            System.err.println("❌ Booking conflict detected: " + e.getMessage());
            throw e; // Re-throw to be handled by controller
        }

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
        booking.setRestaurant(restaurant); // Set restaurant directly
        booking.setBookingTime(form.getBookingTime());
        booking.setNumberOfGuests(form.getGuestCount());

        // Set deposit amount from tables if tables are selected, otherwise use form
        // value
        BigDecimal depositAmount = BigDecimal.ZERO;
        if (form.getTableIds() != null && !form.getTableIds().trim().isEmpty()) {
            System.out.println("🔍 Calculating deposit from multiple tables: " + form.getTableIds());
            String[] tableIdArray = form.getTableIds().split(",");
            for (String tableIdStr : tableIdArray) {
                try {
                    Integer tableId = Integer.parseInt(tableIdStr.trim());
                    RestaurantTable table = restaurantTableRepository.findById(tableId)
                            .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableId));
                    depositAmount = depositAmount.add(table.getDepositAmount());
                    System.out.println("   Table " + table.getTableName() + " deposit: " + table.getDepositAmount());
                } catch (NumberFormatException e) {
                    System.err.println("❌ Invalid table ID format: " + tableIdStr);
                    throw new IllegalArgumentException("Invalid table ID format: " + tableIdStr);
                }
            }
            System.out.println("✅ Total deposit amount from multiple tables: " + depositAmount);
        } else if (form.getTableId() != null) {
            System.out.println("🔍 Looking for single table ID: " + form.getTableId());
            RestaurantTable table = restaurantTableRepository.findById(form.getTableId())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));
            depositAmount = table.getDepositAmount();
            System.out.println("✅ Table found, deposit amount: " + depositAmount);
        } else if (form.getDepositAmount() != null) {
            depositAmount = form.getDepositAmount();
            System.out.println("✅ Using form deposit amount: " + depositAmount);
        }
        booking.setDepositAmount(depositAmount);
        booking.setNote(form.getNote()); // Set note from form
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

        // Assign tables if specified
        if (form.getTableIds() != null && !form.getTableIds().trim().isEmpty()) {
            System.out.println("🔍 Assigning multiple tables to booking...");
            System.out.println("   Booking ID: " + booking.getBookingId());
            System.out.println("   Table IDs: " + form.getTableIds());
            try {
                assignMultipleTablesToBooking(booking, form.getTableIds());
                System.out.println("✅ Tables assigned successfully");

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
                System.err.println("❌ Error assigning tables: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } else if (form.getTableId() != null) {
            // Backward compatibility - single table
            System.out.println("🔍 Assigning single table to booking...");
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

        // Apply deposit rule: deposit = 10% of total amount
        try {
            BigDecimal tenPercent = new BigDecimal("0.10");
            if (totalAmount != null) {
                BigDecimal computedDeposit = totalAmount.multiply(tenPercent);
                // Round to nearest VND
                computedDeposit = computedDeposit.setScale(0, java.math.RoundingMode.HALF_UP);
                booking.setDepositAmount(computedDeposit);
                System.out.println("✅ Deposit set to 10%: " + computedDeposit);
            } else {
                booking.setDepositAmount(BigDecimal.ZERO);
                System.out.println("⚠️ Total amount is null, deposit set to 0");
            }
            booking = bookingRepository.save(booking);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to apply deposit rule: " + e.getMessage());
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

        // Validate booking time FIRST
        validateBookingTime(form.getBookingTime());

        // Validate guest count
        validateGuestCount(form.getGuestCount());

        // Validate table capacity BEFORE conflict validation
        validateTableCapacity(form);

        // Validate conflicts for update AFTER basic validations
        System.out.println("🔍 Validating booking update conflicts...");
        try {
            conflictService.validateBookingUpdateConflicts(bookingId, form, customerId);
            System.out.println("✅ No conflicts found, proceeding with booking update");
        } catch (BookingConflictException e) {
            System.err.println("❌ Booking update conflict detected: " + e.getMessage());
            throw e; // Re-throw to be handled by controller
        }

        // Update booking fields
        booking.setBookingTime(form.getBookingTime());
        booking.setNumberOfGuests(form.getGuestCount());
        booking.setNote(form.getNote()); // Update note

        // Update restaurant if changed
        if (form.getRestaurantId() != null
                && !form.getRestaurantId().equals(booking.getRestaurant().getRestaurantId())) {
            RestaurantProfile restaurant = restaurantProfileRepository.findById(form.getRestaurantId())
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
            booking.setRestaurant(restaurant);
        }

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
     * Lấy danh sách booking của restaurant
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByRestaurant(Integer restaurantId) {
        System.out.println("🔍 BookingService.getBookingsByRestaurant() called for restaurant ID: " + restaurantId);

        // Validate restaurant exists
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        System.out.println("✅ Restaurant found: " + restaurant.getRestaurantName());

        List<Booking> bookings = bookingRepository.findByRestaurantOrderByBookingTimeDesc(restaurant);
        System.out.println("📋 Repository returned " + bookings.size() + " bookings for restaurant");

        // Log each booking details
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            System.out.println("   Booking " + (i + 1) + ": ID=" + booking.getBookingId() +
                    ", Time=" + booking.getBookingTime() +
                    ", Status=" + booking.getStatus() +
                    ", Customer="
                    + (booking.getCustomer() != null ? booking.getCustomer().getUser().getFullName() : "null") +
                    ", Guests=" + booking.getNumberOfGuests());
        }

        return bookings;
    }

    /**
     * Lấy booking theo ID với thông tin đầy đủ
     */
    @Transactional(readOnly = true)
    public Optional<Booking> getBookingDetailById(Integer bookingId) {
        System.out.println("🔍 BookingService.getBookingDetailById() called for booking ID: " + bookingId);

        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isPresent()) {
            Booking b = booking.get();
            System.out.println("✅ Booking found: ID=" + b.getBookingId() +
                    ", Time=" + b.getBookingTime() +
                    ", Status=" + b.getStatus() +
                    ", Restaurant=" + (b.getRestaurant() != null ? b.getRestaurant().getRestaurantName() : "null") +
                    ", Customer=" + (b.getCustomer() != null ? b.getCustomer().getUser().getFullName() : "null"));
        } else {
            System.out.println("❌ Booking not found");
        }

        return booking;
    }

    /**
     * Lấy booking theo ID với tất cả relationships được load (cho edit form)
     */
    @Transactional(readOnly = true)
    public Optional<Booking> getBookingWithDetailsById(Integer bookingId) {
        System.out.println("🔍 BookingService.getBookingWithDetailsById() called for booking ID: " + bookingId);

        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isPresent()) {
            Booking b = booking.get();
            System.out.println("✅ Booking found: ID=" + b.getBookingId() +
                    ", Time=" + b.getBookingTime() +
                    ", Status=" + b.getStatus() +
                    ", Restaurant=" + (b.getRestaurant() != null ? b.getRestaurant().getRestaurantName() : "null") +
                    ", Customer=" + (b.getCustomer() != null ? b.getCustomer().getUser().getFullName() : "null"));

            // Force load relationships to avoid lazy loading issues
            if (b.getBookingDishes() != null) {
                System.out.println("   📋 BookingDishes loaded: " + b.getBookingDishes().size() + " items");
                b.getBookingDishes().forEach(
                        bd -> System.out.println("      - " + bd.getDish().getName() + " x" + bd.getQuantity()));
            } else {
                System.out.println("   ❌ BookingDishes is null");
            }

            if (b.getBookingServices() != null) {
                System.out.println("   🔧 BookingServices loaded: " + b.getBookingServices().size() + " items");
                b.getBookingServices().forEach(bs -> System.out.println("      - " + bs.getService().getName()));
            } else {
                System.out.println("   ❌ BookingServices is null");
            }

            if (b.getBookingTables() != null) {
                System.out.println("   🪑 BookingTables loaded: " + b.getBookingTables().size() + " items");
                b.getBookingTables().forEach(bt -> System.out.println("      - " + bt.getTable().getTableName()));
            } else {
                System.out.println("   ❌ BookingTables is null");
            }
        } else {
            System.out.println("❌ Booking not found");
        }

        return booking;
    }

    /**
     * Confirm booking when restaurant owner confirms the booking
     * CONFIRMED = đã xác nhận, chờ thanh toán
     */
    @Transactional
    public Booking confirmBooking(Integer bookingId) {
        System.out.println("🔍 BookingService.confirmBooking() called for booking ID: " + bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate booking can be confirmed
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking cannot be confirmed in current status: " + booking.getStatus());
        }
        
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking confirmedBooking = bookingRepository.save(booking);
        
        System.out.println("✅ Booking confirmed successfully: " + confirmedBooking.getBookingId());
        
        // Create notification for customer
        createBookingConfirmationNotification(confirmedBooking);
        
        return confirmedBooking;
    }
    
    /**
     * Complete booking when payment is successful
     * COMPLETED = thanh toán thành công, chờ đến
     */
    @Transactional
    public Booking completeBooking(Integer bookingId) {
        System.out.println("🔍 BookingService.completeBooking() called for booking ID: " + bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Validate booking can be completed
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Booking cannot be completed in current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        Booking completedBooking = bookingRepository.save(booking);

        System.out.println("✅ Booking completed successfully: " + completedBooking.getBookingId());

        return completedBooking;
    }

    /**
     * Create notification for booking confirmation
     */
    private void createBookingConfirmationNotification(Booking booking) {
        System.out.println("🔍 Creating booking confirmation notification for booking ID: " + booking.getBookingId());
        try {
            Notification notification = new Notification();
            notification.setRecipientUserId(booking.getCustomer().getUser().getId());
            notification.setType(NotificationType.BOOKING_CONFIRMED);
            notification.setTitle("Đặt bàn đã được xác nhận");
            notification.setContent(String.format(
                    "Đặt bàn của bạn đã được xác nhận thành công! Booking ID: %d, Thời gian: %s, Số khách: %d",
                    booking.getBookingId(),
                    booking.getBookingTime().toString(),
                    booking.getNumberOfGuests()));
            notification.setLinkUrl("/booking/my");
            notification.setStatus(NotificationStatus.SENT);
            notification.setPriority(1);
            notification.setPublishAt(LocalDateTime.now());

            System.out.println("🔍 Saving confirmation notification...");
            notificationRepository.save(notification);
            System.out.println("✅ Created booking confirmation notification for customer: " + booking.getCustomer().getCustomerId());
        } catch (Exception e) {
            System.err.println("❌ Error creating booking confirmation notification: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception to avoid breaking booking confirmation
        }
    }

    /**
     * Cập nhật trạng thái booking
     */
    @Transactional
    public Booking updateBookingStatus(Integer bookingId, BookingStatus newStatus) {
        System.out.println("🔍 BookingService.updateBookingStatus() called for booking ID: " + bookingId +
                ", new status: " + newStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        BookingStatus oldStatus = booking.getStatus();
        System.out.println("✅ Booking found: ID=" + booking.getBookingId() +
                ", old status: " + oldStatus +
                ", new status: " + newStatus);

        // Validate status transition
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }

        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);

        System.out.println("✅ Booking status updated successfully");
        return updatedBooking;
    }

    /**
     * Kiểm tra tính hợp lệ của việc chuyển đổi trạng thái
     */
    private boolean isValidStatusTransition(BookingStatus from, BookingStatus to) {
        // Define valid transitions
        switch (from) {
            case PENDING:
                return to == BookingStatus.CONFIRMED || to == BookingStatus.CANCELLED;
            case CONFIRMED:
                return to == BookingStatus.COMPLETED || to == BookingStatus.CANCELLED || to == BookingStatus.NO_SHOW;
            case COMPLETED:
                return false; // Cannot change from completed
            case CANCELLED:
                return false; // Cannot change from cancelled
            case NO_SHOW:
                return false; // Cannot change from no show
            default:
                return false;
        }
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

    private void validateGuestCount(Integer guestCount) {
        System.out.println("🔍 Validating guest count: " + guestCount);

        if (guestCount == null) {
            System.err.println("❌ Guest count is null");
            throw new IllegalArgumentException("Guest count cannot be null");
        }

        if (guestCount < 1) {
            System.err.println("❌ Guest count too small: " + guestCount);
            throw new IllegalArgumentException("Guest count must be at least 1");
        }

        if (guestCount > 20) {
            System.err.println("❌ Guest count too large: " + guestCount);
            throw new IllegalArgumentException("Guest count cannot exceed 20 people");
        }

        System.out.println("✅ Guest count validation passed");
    }

    private void validateTableCapacity(BookingForm form) {
        System.out.println("🔍 VALIDATING TABLE CAPACITY - STARTING...");
        System.out.println("   Guest count: " + form.getGuestCount());
        System.out.println("   Table ID: " + form.getTableId());
        System.out.println("   Table IDs: " + form.getTableIds());

        if (form.getTableId() != null) {
            // Single table validation
            System.out.println("🔍 SINGLE TABLE VALIDATION...");
            RestaurantTable table = restaurantTableRepository.findById(form.getTableId())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));

            System.out.println("   Table capacity: " + table.getCapacity());

            if (form.getGuestCount() > table.getCapacity()) {
                System.err.println("❌ Guest count exceeds table capacity");
                System.err.println("   Guest count: " + form.getGuestCount());
                System.err.println("   Table capacity: " + table.getCapacity());
                throw new IllegalArgumentException("Số khách (" + form.getGuestCount() +
                        ") vượt quá sức chứa của bàn " + table.getTableName() + " (" + table.getCapacity() + " người)");
            }
            System.out.println("✅ Single table capacity validation passed");

        } else if (form.getTableIds() != null && !form.getTableIds().trim().isEmpty()) {
            // Multiple tables validation
            System.out.println("🔍 MULTIPLE TABLES VALIDATION...");
            String[] tableIdArray = form.getTableIds().split(",");
            int totalCapacity = 0;
            List<String> tableNames = new ArrayList<>();

            for (String tableIdStr : tableIdArray) {
                try {
                    Integer tableId = Integer.parseInt(tableIdStr.trim());
                    RestaurantTable table = restaurantTableRepository.findById(tableId)
                            .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableId));

                    totalCapacity += table.getCapacity();
                    tableNames.add(table.getTableName());

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid table ID format: " + tableIdStr);
                }
            }

            System.out.println("   Total capacity: " + totalCapacity);
            System.out.println("   Table names: " + String.join(", ", tableNames));

            if (form.getGuestCount() > totalCapacity) {
                System.err.println("❌ Guest count exceeds total table capacity");
                System.err.println("   Guest count: " + form.getGuestCount());
                System.err.println("   Total capacity: " + totalCapacity);
                throw new IllegalArgumentException("Số khách (" + form.getGuestCount() +
                        ") vượt quá tổng sức chứa của các bàn đã chọn (" + totalCapacity + " người)");
            }
            System.out.println("✅ Multiple tables capacity validation passed");

        } else {
            System.err.println("❌ No table selected");
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một bàn");
        }

        System.out.println("🔍 VALIDATING TABLE CAPACITY - COMPLETED!");
    }

    /**
     * Assign multiple tables to booking
     */
    private void assignMultipleTablesToBooking(Booking booking, String tableIds) {
        System.out.println("🔍 assignMultipleTablesToBooking called with tableIds: " + tableIds);
        System.out.println("   Booking ID: " + booking.getBookingId());
        System.out.println("   Booking status: " + booking.getStatus());

        String[] tableIdArray = tableIds.split(",");
        System.out.println("   Found " + tableIdArray.length + " table IDs to assign");

        // Calculate total capacity of all tables
        int totalCapacity = 0;
        List<RestaurantTable> tables = new ArrayList<>();

        for (String tableIdStr : tableIdArray) {
            try {
                Integer tableId = Integer.parseInt(tableIdStr.trim());
                System.out.println("🔍 Processing table ID: " + tableId);

                RestaurantTable table = restaurantTableRepository.findById(tableId)
                        .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableId));
                System.out.println("✅ Table found: " + table.getTableName());
                System.out.println("   Table ID: " + table.getTableId());
                System.out.println("   Table status: " + table.getStatus());
                System.out.println("   Table capacity: " + table.getCapacity());

                tables.add(table);
                totalCapacity += table.getCapacity();

            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid table ID format: " + tableIdStr);
                throw new IllegalArgumentException("Invalid table ID format: " + tableIdStr);
            }
        }

        System.out.println("   Total capacity of all tables: " + totalCapacity);
        System.out.println("   Booking guest count: " + booking.getNumberOfGuests());

        // Validate total capacity
        if (booking.getNumberOfGuests() > totalCapacity) {
            System.err.println("❌ Guest count exceeds total table capacity");
            System.err.println("   Guest count: " + booking.getNumberOfGuests());
            System.err.println("   Total capacity: " + totalCapacity);
            throw new IllegalArgumentException("Số khách (" + booking.getNumberOfGuests() +
                    ") vượt quá tổng sức chứa của các bàn đã chọn (" + totalCapacity + " người)");
        }
        System.out.println("✅ Total table capacity validation passed");

        // Now assign all tables
        for (RestaurantTable table : tables) {
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
                System.err.println(
                        "❌ Error saving BookingTable for table " + table.getTableName() + ": " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        System.out.println("✅ Multiple table assignment completed - status will be managed automatically");
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
        System.out.println("   Table capacity: " + table.getCapacity());
        System.out.println("   Booking guest count: " + booking.getNumberOfGuests());

        // Validate table capacity
        if (booking.getNumberOfGuests() > table.getCapacity()) {
            System.err.println("❌ Guest count exceeds table capacity");
            System.err.println("   Guest count: " + booking.getNumberOfGuests());
            System.err.println("   Table capacity: " + table.getCapacity());
            throw new IllegalArgumentException("Số khách (" + booking.getNumberOfGuests() +
                    ") vượt quá sức chứa của bàn " + table.getTableName() + " (" + table.getCapacity() + " người)");
        }
        System.out.println("✅ Table capacity validation passed");

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
    public Booking updateBookingWithItems(Integer bookingId, BookingForm form) {
        System.out.println("🔍 Updating booking with items: " + bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.canBeEdited()) {
            throw new IllegalArgumentException("Booking cannot be updated in current status");
        }

        validateBookingTime(form.getBookingTime());
        validateGuestCount(form.getGuestCount());
        validateTableCapacity(form);

        System.out.println("🔍 Validating booking update conflicts...");
        try {
            conflictService.validateBookingUpdateConflicts(bookingId, form, booking.getCustomer().getCustomerId());
            System.out.println("✅ No conflicts found, proceeding with booking update");
        } catch (BookingConflictException e) {
            System.err.println("❌ Booking update conflict detected: " + e.getMessage());
            throw e;
        }

        booking.setBookingTime(form.getBookingTime());
        booking.setNumberOfGuests(form.getGuestCount());
        booking.setNote(form.getNote());

        if (form.getRestaurantId() != null
                && !form.getRestaurantId().equals(booking.getRestaurant().getRestaurantId())) {
            RestaurantProfile restaurant = restaurantProfileRepository.findById(form.getRestaurantId())
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
            booking.setRestaurant(restaurant);
        }

        bookingTableRepository.deleteByBooking(booking);
        if (form.getTableIds() != null && !form.getTableIds().trim().isEmpty()) {
            assignMultipleTablesToBooking(booking, form.getTableIds());
        } else if (form.getTableId() != null) {
            assignTableToBooking(booking, form.getTableId());
        }

        bookingDishRepository.deleteByBooking(booking);
        if (form.getDishIds() != null && !form.getDishIds().trim().isEmpty()) {
            assignDishesToBooking(booking, form.getDishIds());
        }

        bookingServiceRepository.deleteByBooking(booking);
        if (form.getServiceIds() != null && !form.getServiceIds().trim().isEmpty()) {
            assignServicesToBooking(booking, form.getServiceIds());
        }

        Booking saved = bookingRepository.save(booking);
        System.out.println("✅ Booking updated successfully: " + saved.getBookingId());
        return saved;
    }

    /**
     * Update booking for restaurant owner (with restaurant ownership validation)
     */
    public Booking updateBookingForRestaurantOwner(Integer bookingId, BookingForm form,
            Set<Integer> ownerRestaurantIds) {
        System.out.println(
                "🔍 Updating booking for restaurant owner: " + bookingId + " with restaurants: " + ownerRestaurantIds);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (ownerRestaurantIds == null || ownerRestaurantIds.isEmpty()) {
            throw new IllegalArgumentException("Owner does not have any restaurants assigned");
        }

        if (!ownerRestaurantIds.contains(booking.getRestaurant().getRestaurantId())) {
            throw new IllegalArgumentException("You can only edit bookings for your own restaurant");
        }

        if (form.getRestaurantId() != null
                && !ownerRestaurantIds.contains(form.getRestaurantId())) {
            throw new IllegalArgumentException("Cannot move booking to a restaurant you do not own");
        }

        Booking updated = updateBookingWithItems(bookingId, form);

        if (form.getRestaurantId() != null
                && !form.getRestaurantId().equals(booking.getRestaurant().getRestaurantId())) {
            System.out.println("🔍 Reassigning tables for new restaurant...");
            bookingTableRepository.deleteByBooking(updated);
            if (form.getTableIds() != null && !form.getTableIds().trim().isEmpty()) {
                assignMultipleTablesToBooking(updated, form.getTableIds());
            } else if (form.getTableId() != null) {
                assignTableToBooking(updated, form.getTableId());
            }
        }

        System.out.println("✅ Booking updated successfully for restaurant owner: " + updated.getBookingId());
        return updated;
    }

    // Helper methods
    private Integer getCurrentTableId(Booking booking) {
        if (booking.getBookingTables() != null && !booking.getBookingTables().isEmpty()) {
            return booking.getBookingTables().get(0).getTable().getTableId();
        }
        return null;
    }

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
}