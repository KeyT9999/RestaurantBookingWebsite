package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@Service
@Transactional
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

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

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundService refundService;

    @Autowired
    private BookingNotificationService bookingNotificationService;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Tạo booking mới
     */
    public Booking createBooking(BookingForm form, UUID customerId) {
        if (form == null) {
            throw new IllegalArgumentException("BookingForm cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }

        try {
            System.out.println("🚨🚨🚨 BOOKING SERVICE CREATE BOOKING CALLED! 🚨🚨🚨");
            System.out.println("🔍 BookingService.createBooking() called - Transaction started");
            System.out.println("   Customer ID: " + customerId);
            System.out.println("   Restaurant ID: " + form.getRestaurantId());
            System.out.println("   Table ID: " + form.getTableId());
            System.out.println("   Guest Count: " + form.getGuestCount());
            System.out.println("   Booking Time: " + form.getBookingTime());

            validateBookingTime(form.getBookingTime());
            System.out.println("✅ Booking time validated");
            validateGuestCount(form.getGuestCount());
            System.out.println("✅ Guest count validated");
            validateDepositAmount(form.getDepositAmount());
            System.out.println("✅ Deposit amount validated");

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
                
                VoucherService.ValidationResult validation = voucherService != null
                        ? voucherService.validate(validationReq)
                        : null;
                if (validation != null && validation.valid() && validation.calculatedDiscount() != null) {
                    voucherDiscount = validation.calculatedDiscount();
                    voucherCodeToApply = form.getVoucherCode();
                } else if (validation != null && !validation.valid()) {
                    throw new IllegalArgumentException("Invalid voucher: " + validation.reason());
                } else {
                    System.out.println("⚠️ Voucher validation returned no result, skipping voucher application");
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

        // Deposit will be calculated later = 10% of finalTotal (after voucher discount)
        // Don't set deposit here, it will be calculated after we have finalTotal
        booking.setDepositAmount(BigDecimal.ZERO); // Temporary, will be recalculated
        booking.setNote(form.getNote()); // Set note from form
        booking.setStatus(BookingStatus.PENDING);
        System.out.println("✅ Booking object created with status: " + booking.getStatus());

        // Save booking first
        System.out.println("🔍 Saving booking to database...");
        try {
            System.out.println("🚨🚨🚨 BOOKING REPOSITORY SAVE CALLED! 🚨🚨🚨");
            booking = bookingRepository.save(booking);
            System.out.println("✅ Booking saved successfully! ID: " + booking.getBookingId());
            LocalDateTime auditTimestamp = LocalDateTime.now();
            if (booking.getCreatedAt() == null) {
                booking.setCreatedAt(auditTimestamp);
            }
            booking.setUpdatedAt(auditTimestamp);

            // Force flush to ensure booking is persisted before creating BookingTable
            System.out.println("🔍 Flushing booking to database...");
            entityManager.flush();
            System.out.println("✅ Booking flushed successfully");
        } catch (Exception e) {
            System.err.println("❌ Error saving booking: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Apply voucher if valid - will be applied after subtotal is calculated
        // Store voucher info for later use
        boolean hasVoucher = voucherDiscount.compareTo(BigDecimal.ZERO) > 0 && voucherCodeToApply != null;

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

        // Calculate subtotal = table fees + dishes + services (KHÔNG bao gồm deposit)
        BigDecimal subtotal = calculateSubtotal(booking);
        System.out.println("💰 Subtotal (table fees + dishes + services): " + subtotal);

        // Calculate deposit = 10% of subtotal (BEFORE voucher discount)
        // Deposit is NOT affected by voucher discount
        BigDecimal depositAmount = BigDecimal.ZERO;
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            depositAmount = subtotal.multiply(new BigDecimal("0.10"));
            depositAmount = depositAmount.setScale(0, java.math.RoundingMode.HALF_UP);
            booking.setDepositAmount(depositAmount);
            System.out.println("✅ Deposit calculated as 10% of subtotal (before voucher): " + depositAmount);
        } else {
            booking.setDepositAmount(BigDecimal.ZERO);
            System.out.println("⚠️ Subtotal is zero or negative, deposit set to 0");
        }

        // Apply voucher discount to subtotal (voucher only affects totalAmount, NOT
        // deposit)
        // Apply voucher AFTER subtotal is calculated to get accurate discount
        BigDecimal finalTotal = subtotal;
        BigDecimal actualVoucherDiscount = BigDecimal.ZERO;

        if (hasVoucher && voucherCodeToApply != null) {
            try {
                System.out.println("🔍 Applying voucher: " + voucherCodeToApply + " with subtotal: " + subtotal);
                VoucherService.ApplyRequest applyReq = new VoucherService.ApplyRequest(
                        voucherCodeToApply,
                        form.getRestaurantId(),
                        customerId,
                        subtotal, // Use actual subtotal instead of calculateOrderAmount(form)
                        booking.getBookingId());

                VoucherService.ApplyResult applyResult = voucherService != null
                        ? voucherService.applyToBooking(applyReq)
                        : null;
                if (applyResult == null) {
                    System.out.println("⚠️ Voucher application returned no result, skipping apply step");
                } else if (!applyResult.success()) {
                    throw new IllegalArgumentException("Failed to apply voucher: " + applyResult.reason());
                } else {
                    // Use discount from VoucherRedemption (actual applied discount)
                    actualVoucherDiscount = applyResult.discountApplied() != null
                            ? applyResult.discountApplied()
                            : voucherDiscount;
                    finalTotal = subtotal.subtract(actualVoucherDiscount);
                    // Ensure finalTotal is not negative
                    if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
                        System.out.println("⚠️ Voucher discount exceeds subtotal, setting finalTotal to 0");
                        finalTotal = BigDecimal.ZERO;
                    }
                    System.out.println("✅ Voucher applied successfully! Redemption ID: " + applyResult.redemptionId());
                    System.out.println(
                            "💰 After voucher discount: " + finalTotal + " (discount: " + actualVoucherDiscount + ")");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Voucher application failed: " + e.getMessage());
            }
        }

        // Save totalAmount to database (after voucher discount)
        // Deposit is already set above and is NOT affected by voucher
        booking.setTotalAmount(finalTotal);
        System.out.println("✅ Total amount saved to database: " + finalTotal + " (after voucher discount)");
        System.out.println("✅ Deposit amount: " + booking.getDepositAmount() + " (NOT affected by voucher)");

        // Save booking with deposit and totalAmount
        try {
            booking = bookingRepository.save(booking);
            System.out.println("✅ Booking saved with deposit: " + booking.getDepositAmount() + ", total: "
                    + booking.getTotalAmount());
        } catch (Exception e) {
            System.err.println("⚠️ Failed to save booking: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to see the actual error
        }

        // Create notification for customer and restaurant owner
        System.out.println("🔍 Creating notification...");
        try {
            createBookingNotification(booking);
            bookingNotificationService.notifyNewBookingToRestaurant(booking);
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

        // Recalculate and update totalAmount after items changed
        BigDecimal subtotal = calculateSubtotal(booking);
        booking.setTotalAmount(subtotal);

        // Recalculate deposit = 10% of subtotal
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal computedDeposit = subtotal.multiply(new BigDecimal("0.10"));
            computedDeposit = computedDeposit.setScale(0, java.math.RoundingMode.HALF_UP);
            booking.setDepositAmount(computedDeposit);
            System.out.println("✅ Deposit recalculated as 10% of subtotal: " + computedDeposit);
        } else {
            booking.setDepositAmount(BigDecimal.ZERO);
        }

        return bookingRepository.save(booking);
    }

    /**
     * Hủy booking (Customer)
     */
    @Transactional
    public Booking cancelBooking(Integer bookingId, UUID customerId, String cancelReason,
            String bankCode, String accountNumber) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Check if customer owns this booking
        if (!booking.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only cancel your own bookings");
        }

        // Không cần kiểm tra booking status nữa, chỉ cần kiểm tra payment status
        // COMPLETED
        // Process refund
        processRefundForCancelledBooking(booking, cancelReason, bankCode, accountNumber);

        // Update booking status: mark as CANCELLED immediately after initiating refund
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(cancelReason);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(customerId);

        Booking cancelledBooking = bookingRepository.save(booking);

        // Send notifications
        try {
            bookingNotificationService.notifyBookingCancelledToCustomer(cancelledBooking, cancelReason);
            bookingNotificationService.notifyBookingCancelledToRestaurant(cancelledBooking, cancelReason);
        } catch (Exception e) {
            logger.error("Failed to send booking cancellation notifications", e);
        }

        return cancelledBooking;
    }

    /**
     * Hủy booking (Restaurant Owner) với thông tin ngân hàng
     */
    @Transactional
    public Booking cancelBookingByRestaurant(Integer bookingId, UUID restaurantOwnerId, String cancelReason,
            String bankCode, String accountNumber) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Validate restaurant ownership
        if (!booking.getRestaurant().getOwner().getUser().getId().equals(restaurantOwnerId)) {
            throw new IllegalArgumentException("You can only cancel bookings for your restaurant");
        }

        // Process refund with bank account info
        processRefundForCancelledBooking(booking, cancelReason, bankCode, accountNumber);

        // Update booking status: mark as CANCELLED immediately after initiating refund
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(cancelReason);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(restaurantOwnerId);

        Booking cancelledBooking = bookingRepository.save(booking);

        // Send notifications
        try {
            bookingNotificationService.notifyBookingCancelledToCustomer(cancelledBooking, cancelReason);
            bookingNotificationService.notifyBookingCancelledToRestaurant(cancelledBooking, cancelReason);
        } catch (Exception e) {
            logger.error("Failed to send booking cancellation notifications", e);
        }

        return cancelledBooking;
    }

    /**
     * Hủy booking (Restaurant Owner) - legacy method
     */
    @Transactional
    public Booking cancelBookingByRestaurant(Integer bookingId, UUID restaurantOwnerId, String cancelReason) {
        return cancelBookingByRestaurant(bookingId, restaurantOwnerId, cancelReason, "", "");
    }

    /**
     * Process refund for cancelled booking - legacy method
     */
    private void processRefundForCancelledBooking(Booking booking, String cancelReason,
            String bankCode, String accountNumber) {
        logger.info("🔄 Processing refund for cancelled booking: {}", booking.getBookingId());

        // Tìm payment của booking
        Optional<Payment> paymentOpt = paymentRepository.findByBooking(booking);

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            logger.info("✅ Payment found: ID={}, Status={}, Amount={}",
                    payment.getPaymentId(), payment.getStatus(), payment.getAmount());

            // Chỉ refund khi payment đã completed (không cần kiểm tra booking status)
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                logger.info("🔄 Creating refund request for completed payment: {}", payment.getPaymentId());

                // Tạo refund request với manual transfer và thông tin bank account
                refundService.processRefundWithManualTransfer(
                        payment.getPaymentId(),
                        "Booking cancelled: " + cancelReason,
                        bankCode,
                        accountNumber);

                logger.info("✅ Refund request created successfully");
            } else {
                logger.warn("⚠️ Payment status is not COMPLETED: {}, skipping refund", payment.getStatus());
            }
        } else {
            logger.warn("⚠️ No payment found for booking: {}, skipping refund", booking.getBookingId());
        }
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

        // Filter out DELETED bookings
        bookings = bookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.DELETED)
                .toList();
        System.out.println("📋 After filtering DELETED: " + bookings.size() + " bookings");

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

        // Use eager fetch to load bookingTables and table status
        List<Booking> bookings = bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId);
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
        try {
            bookingNotificationService.notifyBookingConfirmedToCustomer(confirmedBooking);
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation notification", e);
        }
        
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

        // Send notifications
        try {
            bookingNotificationService.notifyBookingCompletedToCustomer(completedBooking);
            bookingNotificationService.notifyBookingCompletedToRestaurant(completedBooking);
        } catch (Exception e) {
            logger.error("Failed to send booking completion notifications", e);
        }

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

        // Send notifications based on status change
        try {
            if (newStatus == BookingStatus.CONFIRMED && oldStatus != BookingStatus.CONFIRMED) {
                // Owner confirmed the booking
                bookingNotificationService.notifyBookingConfirmedToCustomer(updatedBooking);
                logger.info("✅ Sent booking confirmation notification to customer for booking: {}", bookingId);
            } else if (newStatus == BookingStatus.CANCELLED && oldStatus != BookingStatus.CANCELLED) {
                // Booking was cancelled
                String cancelReason = "Được hủy bởi nhà hàng";
                bookingNotificationService.notifyBookingCancelledToCustomer(updatedBooking, cancelReason);
                bookingNotificationService.notifyBookingCancelledToRestaurant(updatedBooking, cancelReason);
                logger.info("✅ Sent booking cancellation notifications for booking: {}", bookingId);
            } else if (newStatus == BookingStatus.COMPLETED && oldStatus != BookingStatus.COMPLETED) {
                // Booking was completed
                bookingNotificationService.notifyBookingCompletedToCustomer(updatedBooking);
                bookingNotificationService.notifyBookingCompletedToRestaurant(updatedBooking);
                logger.info("✅ Sent booking completion notifications for booking: {}", bookingId);
            }
        } catch (Exception e) {
            logger.error("❌ Failed to send booking status change notifications for booking: {}", bookingId, e);
            // Don't throw exception to avoid breaking status update
        }

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
                return to == BookingStatus.DELETED; // Can delete cancelled bookings
            case NO_SHOW:
                return false; // Cannot change from no show
            case DELETED:
                return false; // Cannot change from deleted
            default:
                return false;
        }
    }
    
    /**
     * Xóa booking (đổi status thành DELETED)
     * Chỉ có thể xóa các booking đã CANCELLED
     */
    @Transactional
    public Booking deleteBooking(Integer bookingId, UUID customerId) {
        System.out.println("🔍 BookingService.deleteBooking() called for booking ID: " + bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate customer ownership
        if (!booking.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only delete your own bookings");
        }
        
        // Only allow deleting CANCELLED bookings
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Only cancelled bookings can be deleted");
        }
        
        booking.setStatus(BookingStatus.DELETED);
        Booking deletedBooking = bookingRepository.save(booking);
        
        System.out.println("✅ Booking deleted successfully: " + deletedBooking.getBookingId());
        
        return deletedBooking;
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
        if (bookingTime.isBefore(now)) {
            System.err.println("❌ Booking time is in the past");
            throw new IllegalArgumentException("Booking time cannot be in the past");
        }

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
            throw new IllegalArgumentException("Guest count must be greater than 0");
        }

        if (guestCount > 100) {
            System.err.println("❌ Guest count too large: " + guestCount);
            throw new IllegalArgumentException("Guest count cannot exceed 100 people");
        }

        System.out.println("✅ Guest count validation passed");
    }

    private void validateDepositAmount(BigDecimal depositAmount) {
        System.out.println("🔍 Validating deposit amount: " + depositAmount);
        if (depositAmount != null && depositAmount.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("❌ Deposit amount is negative: " + depositAmount);
            throw new IllegalArgumentException("Deposit amount cannot be negative");
        }
        System.out.println("✅ Deposit amount validation passed");
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
            UUID recipientUserId = null;
            
            if (booking.getCustomer() != null) {
                Customer customer = booking.getCustomer();
                
                // Try to get User from Customer (may be lazy loaded)
                User user = customer.getUser();
                
                // If user is null (lazy loading not triggered), try to load it
                if (user == null) {
                    System.out.println("⚠️ Customer.getUser() is null, trying to load User from database...");
                    
                    // Option 1: Reload customer with user relationship (force fetch)
                    Customer loadedCustomer = customerRepository.findById(customer.getCustomerId())
                        .orElse(null);
                    if (loadedCustomer != null) {
                        user = loadedCustomer.getUser();
                        System.out.println("   Reloaded customer, user: " + (user != null ? user.getId() : "null"));
                    }
                    
                    // Option 2: If still null, query User directly using EntityManager
                    // Customer table has user_id foreign key, so we can query User by joining
                    if (user == null) {
                        System.out.println("   Trying to query User directly from database...");
                        try {
                            // Query: SELECT u FROM User u WHERE u.id IN 
                            // (SELECT c.user.id FROM Customer c WHERE c.customerId = :customerId)
                            String jpql = "SELECT c.user FROM Customer c WHERE c.customerId = :customerId";
                            user = entityManager.createQuery(jpql, User.class)
                                .setParameter("customerId", customer.getCustomerId())
                                .getSingleResult();
                            System.out.println("   Found User via JPQL: " + (user != null ? user.getId() : "null"));
                        } catch (Exception e) {
                            System.err.println("   ❌ Error querying User: " + e.getMessage());
                        }
                    }
                }
                
                // If we still have user, use its ID
                if (user != null) {
                    recipientUserId = user.getId();
                    System.out.println("✅ Found User ID from Customer: " + recipientUserId);
                } else {
                    // Last resort: query User by customer's user_id foreign key
                    // We need to get user_id from customer table
                    // Since Customer has @OneToOne with User, we can query User where customer exists
                    // But this is complex, so let's just throw an error
                    System.err.println("❌ CRITICAL: Cannot find User for Customer: " + customer.getCustomerId());
                    throw new IllegalArgumentException("Customer does not have an associated User. Customer ID: " + customer.getCustomerId());
                }
            } else {
                throw new IllegalArgumentException("Booking does not have a Customer");
            }

            if (recipientUserId == null) {
                throw new IllegalArgumentException("Unable to determine notification recipient");
            }

            notification.setRecipientUserId(recipientUserId);
            notification.setType(NotificationType.BOOKING_CONFIRMED);
            notification.setTitle("Đặt bàn thành công");
            
            // Get restaurant name
            String restaurantName = "Nhà hàng";
            if (booking.getRestaurant() != null && booking.getRestaurant().getRestaurantName() != null) {
                restaurantName = booking.getRestaurant().getRestaurantName();
            }
            
            notification.setContent(String.format(
                    "Bạn đã đặt bàn thành công! Nhà hàng: %s, Thời gian: %s, Số khách: %d",
                    restaurantName,
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
     * Calculate subtotal = table fees + dishes + services (excluding deposit)
     * This is a PUBLIC method for use by PaymentService
     */
    public BigDecimal calculateSubtotal(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        // 1. Tính tổng phí bàn từ BookingTable (snapshot)
        List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
        if (!bookingTables.isEmpty()) {
            BigDecimal tableFeeTotal = BigDecimal.ZERO;
            for (BookingTable bookingTable : bookingTables) {
                tableFeeTotal = tableFeeTotal.add(bookingTable.getTableFee());
            }
            subtotal = subtotal.add(tableFeeTotal);
            System.out.println("💰 Table fees total: " + tableFeeTotal);
        }

        // 2. Cộng dishes total
        List<BookingDish> bookingDishes = bookingDishRepository.findByBooking(booking);
        if (!bookingDishes.isEmpty()) {
            BigDecimal dishesTotal = BigDecimal.ZERO;
            for (BookingDish bookingDish : bookingDishes) {
                dishesTotal = dishesTotal.add(bookingDish.getTotalPrice());
                System.out.println("🍽️ Dish: " + bookingDish.getDish().getName() +
                        " x" + bookingDish.getQuantity() +
                        " = " + bookingDish.getTotalPrice());
            }
            subtotal = subtotal.add(dishesTotal);
            System.out.println("💰 Dishes total: " + dishesTotal);
        }

        // 3. Cộng services total
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
            subtotal = subtotal.add(servicesTotal);
            System.out.println("💰 Services total: " + servicesTotal);
        }

        System.out.println("💰 SUBTOTAL (table fees + dishes + services): " + subtotal);
        return subtotal;
    }

    /**
     * Calculate total amount for booking
     * Total = subtotal (table fees + dishes + services)
     * Note: Deposit is calculated separately as 10% of subtotal, not added to total
     * 
     * This method returns the cached totalAmount from database if available,
     * otherwise calculates it from items (for backward compatibility with old
     * bookings)
     */
    public BigDecimal calculateTotalAmount(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        // If totalAmount is already stored in database and > 0, use it (performance
        // optimization)
        if (booking.getTotalAmount() != null && booking.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("💰 Using cached total amount from database: " + booking.getTotalAmount());
            return booking.getTotalAmount();
        }

        // Otherwise, calculate from items (for old bookings that don't have totalAmount
        // stored)
        BigDecimal subtotal = calculateSubtotal(booking);
        System.out.println("💰 Calculated total amount from items: " + subtotal);

        // Update database with calculated value for future queries
        if (booking.getBookingId() != null) {
            booking.setTotalAmount(subtotal);
            bookingRepository.save(booking);
            System.out.println("✅ Updated total amount in database for future queries");
        }

        return subtotal;
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

        // Recalculate and update totalAmount after items changed
        BigDecimal subtotal = calculateSubtotal(booking);
        booking.setTotalAmount(subtotal);
        System.out.println("✅ Total amount updated in database: " + subtotal);

        // Recalculate deposit = 10% of subtotal
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal computedDeposit = subtotal.multiply(new BigDecimal("0.10"));
            computedDeposit = computedDeposit.setScale(0, java.math.RoundingMode.HALF_UP);
            booking.setDepositAmount(computedDeposit);
            System.out.println("✅ Deposit recalculated as 10% of subtotal: " + computedDeposit);
        } else {
            booking.setDepositAmount(BigDecimal.ZERO);
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
