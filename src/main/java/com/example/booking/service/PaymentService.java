package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.Voucher;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.PaymentRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for payment processing and management
 * Handles payment creation, processing, and status updates
 */
@Service
@Transactional
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BookingService bookingService;
    
    // Voucher repository is not required for current PayOS flow

    // PayOsService is used indirectly through webhook processing
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Create payment for booking
     * @param bookingId The booking ID
     * @param customerId The customer ID
     * @param paymentMethod The payment method
     * @param paymentType The payment type (DEPOSIT or FULL_PAYMENT)
     * @param voucherCode The voucher code (optional)
     * @return Created payment
     */
    public Payment createPayment(Integer bookingId, UUID customerId, 
                                PaymentMethod paymentMethod, PaymentType paymentType, String voucherCode) {
        
        logger.info("Creating payment for bookingId: {}, customerId: {}, method: {}, type: {}", 
            bookingId, customerId, paymentMethod, paymentType);
        
        // Validate booking
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        
        // Validate customer
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        
        // NEW POLICY: Only DEPOSIT is allowed via web booking (> 500k threshold)
        // FULL_PAYMENT is only for internal/admin use or cash at restaurant
        if (paymentType == PaymentType.FULL_PAYMENT && paymentMethod != PaymentMethod.CASH) {
            logger.warn("⚠️ FULL_PAYMENT rejected for non-CASH method. Forcing DEPOSIT.");
            paymentType = PaymentType.DEPOSIT;
        }
        
        // Validate payment method and type combination
        validatePaymentMethodAndType(paymentMethod, paymentType);
        
        // Calculate total amount based on payment type (do this FIRST)
        BigDecimal totalAmount = calculateTotalAmount(booking, paymentType, voucherCode);
        
        // Check if payment already exists for this booking and type
        Optional<Payment> existingPayment = findExistingPayment(booking, paymentType);
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            
            // If payment is PENDING, update amount with new calculation
            if (payment.getStatus() == PaymentStatus.PENDING) {
                BigDecimal oldAmount = payment.getAmount();
                
                // If amount changed, reset orderCode to create new payment link
                if (oldAmount.compareTo(totalAmount) != 0) {
                    logger.info("Amount changed from {} to {} → Resetting orderCode for new PayOS link", 
                        oldAmount, totalAmount);
                    
                    payment.setAmount(totalAmount);
                    
                    // Reset PayOS fields to create fresh payment link
                    payment.setOrderCode(null);
                    payment.setPayosCheckoutUrl(null);
                    payment.setPayosPaymentLinkId(null);
                    
                    Payment updatedPayment = paymentRepository.save(payment);
                    
                    // Generate new orderCode
                    Long newOrderCode = generateUniqueOrderCode(booking.getBookingId());
                    updatedPayment.setOrderCode(newOrderCode);
                    updatedPayment = paymentRepository.save(updatedPayment);
                    
                    logger.info("Payment updated: PaymentId={}, Amount={}, NewOrderCode={}", 
                        updatedPayment.getPaymentId(), updatedPayment.getAmount(), newOrderCode);
                    
                    return updatedPayment;
                } else {
                    logger.info("Amount unchanged ({}), reusing existing payment", totalAmount);
                    return payment;
                }
            }
            
            // If payment is already COMPLETED or other status, just return it
            logger.warn("Payment already exists for bookingId: {} and type: {} with status: {}", 
                bookingId, paymentType, payment.getStatus());
            return payment;
        }
        
        // Create payment record
        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setBooking(booking);
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentType(paymentType);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(LocalDateTime.now());
        
        // Apply voucher if provided
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            Voucher voucher = findValidVoucher(voucherCode, customer);
            if (voucher != null) {
                payment.setVoucher(voucher);
                totalAmount = applyVoucherDiscount(totalAmount, voucher);
                payment.setAmount(totalAmount);
            }
        }
        
        // Generate unique orderCode for PayOS
        Long orderCode = generateUniqueOrderCode(bookingId);
        payment.setOrderCode(orderCode);
        
        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        
        logger.info("Payment created successfully. PaymentId: {}, Amount: {}", 
            savedPayment.getPaymentId(), savedPayment.getAmount());
        
        return savedPayment;
    }
    
    
    
    /**
     * Handle PayOS webhook
     * @param rawBody Raw JSON body from PayOS
     * @return true if processed successfully
     */
    public boolean handlePayOsWebhook(String rawBody) {
        try {
            logger.info("Processing PayOS webhook: {}", rawBody);
            
            // Parse webhook data using PayOsService.WebhookRequest
            PayOsService.WebhookRequest webhookRequest = objectMapper.readValue(rawBody, PayOsService.WebhookRequest.class);
            
            if (webhookRequest == null || webhookRequest.getData() == null) {
                logger.error("Invalid PayOS webhook payload");
                return false;
            }
            
            PayOsService.WebhookRequest.WebhookData data = webhookRequest.getData();
            Long orderCode = data.getOrderCode().longValue();
            
            logger.info("Processing webhook for orderCode: {}, success: {}, code: {}", 
                orderCode, webhookRequest.getSuccess(), data.getCode());
            
            // Find payment by orderCode
            Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(orderCode);
            if (paymentOpt.isEmpty()) {
                logger.error("Payment not found for orderCode: {}", orderCode);
                return false;
            }
            
            Payment payment = paymentOpt.get();
            
            // Determine if payment was successful
            boolean success = Boolean.TRUE.equals(webhookRequest.getSuccess()) && "00".equals(data.getCode());
            
            if (success) {
                // Payment successful
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.setPayosCode(data.getCode());
                payment.setPayosDesc("PayOS payment successful. Reference: " + data.getReference());
                payment.setPayosPaymentLinkId(data.getPaymentLinkId());
                
                logger.info("Payment {} updated to COMPLETED", payment.getPaymentId());
                
                // Complete booking (thanh toán thành công)
                try {
                    completeBooking(payment.getBooking());
                    logger.info("Booking {} completed after PayOS payment", payment.getBooking().getBookingId());
                } catch (Exception e) {
                    logger.error("Failed to complete booking after PayOS payment. paymentId: {}",
                            payment.getPaymentId(), e);
                }
                
                // Send success emails
                try {
                    Booking booking = payment.getBooking();
                    Customer customer = booking.getCustomer();
                    
                    // Calculate remaining amount
                    java.math.BigDecimal totalAmount = calculateTotalAmount(booking);
                    java.math.BigDecimal remainingAmount = totalAmount.subtract(payment.getAmount());
                    
                    // Send email to customer
                    String customerEmail = customer.getUser().getEmail();
                    String customerName = customer.getFullName();
                    String restaurantName = booking.getRestaurant().getRestaurantName();
                    String bookingTime = booking.getBookingTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    
                    emailService.sendPaymentSuccessEmail(
                        customerEmail,
                        customerName,
                        booking.getBookingId(),
                        restaurantName,
                        bookingTime,
                        booking.getNumberOfGuests(),
                        payment.getAmount(),
                        remainingAmount,
                        payment.getPaymentMethod().getDisplayName()
                    );
                    
                    // Send email to restaurant owner
                    String ownerEmail = booking.getRestaurant().getOwner().getUser().getEmail();
                    emailService.sendPaymentNotificationToRestaurant(
                        ownerEmail,
                        restaurantName,
                        booking.getBookingId(),
                        customerName,
                        bookingTime,
                        booking.getNumberOfGuests(),
                        payment.getAmount(),
                        payment.getPaymentMethod().getDisplayName()
                    );
                    
                    logger.info("✅ Payment success emails sent for payment {}", payment.getPaymentId());
                    
                } catch (Exception e) {
                    logger.error("❌ Failed to send payment success emails for payment {}", payment.getPaymentId(), e);
                    // Don't fail the webhook processing if email fails
                }
                
            } else {
                // Payment failed
                payment.setStatus(PaymentStatus.FAILED);
                payment.setPayosCode(data.getCode());
                payment.setPayosDesc("PayOS payment failed. Code: " + data.getCode() + ", Desc: " + data.getDesc());
                
                logger.info("Payment {} updated to FAILED", payment.getPaymentId());
            }
            
            // Store webhook data
            try { 
                payment.setIpnRaw(rawBody); 
            } catch (Exception ignore) {}
            
            paymentRepository.save(payment);
            
            logger.info("PayOS webhook processed successfully for paymentId: {}", payment.getPaymentId());
            return true;
            
        } catch (Exception e) {
            logger.error("Error processing PayOS webhook", e);
            return false;
        }
    }
    
    /**
     * Process cash payment
     * @param paymentId The payment ID
     * @return Updated payment
     */
    public Payment processCashPayment(Integer paymentId) {
        logger.info("Processing cash payment for paymentId: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (payment.getPaymentMethod() != PaymentMethod.CASH) {
            throw new IllegalArgumentException("Payment method is not cash");
        }
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        logger.info("Cash payment processed successfully. PaymentId: {}", paymentId);
        
        // Confirm booking when payment is successful
        try {
            confirmBooking(payment.getBooking());
            logger.info("Booking confirmed after cash payment. BookingId: {}", payment.getBooking().getBookingId());
        } catch (Exception e) {
            logger.error("Failed to confirm booking after cash payment. BookingId: {}", 
                    payment.getBooking().getBookingId(), e);
        }
        
        return updatedPayment;
    }
    
    /**
     * Process card payment
     * @param paymentId The payment ID
     * @return Updated payment
     */
    public Payment processCardPayment(Integer paymentId) {
        logger.info("Processing card payment for paymentId: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (payment.getPaymentMethod() != PaymentMethod.CARD) {
            throw new IllegalArgumentException("Payment method is not card");
        }
        
        // TODO: Integrate with card payment gateway
        // For now, simulate successful payment
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        logger.info("Card payment processed successfully. PaymentId: {}", paymentId);
        
        // Confirm booking when payment is successful
        try {
            confirmBooking(payment.getBooking());
            logger.info("Booking confirmed after card payment. BookingId: {}", payment.getBooking().getBookingId());
        } catch (Exception e) {
            logger.error("Failed to confirm booking after card payment. BookingId: {}", 
                    payment.getBooking().getBookingId(), e);
        }
        
        return updatedPayment;
    }
    
    /**
     * Get payment by ID
     * @param paymentId The payment ID
     * @return Payment if found
     */
    public Optional<Payment> findById(Integer paymentId) {
        return paymentRepository.findById(paymentId);
    }
    
    
    
    /**
     * Get payments by customer
     * @param customer The customer
     * @return List of payments
     */
    public List<Payment> findByCustomer(Customer customer) {
        return paymentRepository.findByCustomerOrderByPaidAtDesc(customer);
    }
    
    /**
     * Get payments by booking
     * @param booking The booking
     * @return Payment if found
     */
    public Optional<Payment> findByBooking(Booking booking) {
        return paymentRepository.findByBooking(booking);
    }
    
    /**
     * Get payments by status
     * @param status The payment status
     * @return List of payments
     */
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
    
    
    
    /**
     * Cancel payment
     * @param paymentId The payment ID
     * @return Updated payment
     */
    public Payment cancelPayment(Integer paymentId) {
        logger.info("Cancelling payment for paymentId: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel completed payment");
        }
        
        payment.setStatus(PaymentStatus.CANCELLED);
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        logger.info("Payment cancelled successfully. PaymentId: {}", paymentId);
        
        return updatedPayment;
    }
    
    /**
     * Refund payment
     * @param paymentId The payment ID
     * @return Updated payment
     */
    public Payment refundPayment(Integer paymentId) {
        logger.info("Refunding payment for paymentId: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed payments can be refunded");
        }
        
        // TODO: Implement refund logic based on payment method
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        logger.info("Payment refunded successfully. PaymentId: {}", paymentId);
        
        return updatedPayment;
    }
    
    /**
     * Calculate total amount for booking based on payment type
     * NEW LOGIC:
     * - Deposit = 10% of total
     * - Full payment = 100% total
     * 
     * @param booking     The booking
     * @param paymentType The payment type
     * @param voucherCode The voucher code
     * @return Total amount
     */
    private BigDecimal calculateTotalAmount(Booking booking, PaymentType paymentType, String voucherCode) {
        // Calculate FULL total first (deposit + dishes + services)
        BigDecimal fullTotal = calculateFullPaymentAmount(booking);
        
        logger.info("💰 Calculating payment amount - Full total: {}, Type: {}", fullTotal, paymentType);
        
        BigDecimal paymentAmount;
        
        if (paymentType == PaymentType.DEPOSIT) {
            // Deposit = 10% of total
            paymentAmount = fullTotal.multiply(new BigDecimal("0.1"));
            logger.info("   → Deposit (10% of {}): {}", fullTotal, paymentAmount);
        } else {
            // FULL_PAYMENT: use full total
            paymentAmount = fullTotal;
            logger.info("   → Full payment: {}", paymentAmount);
        }
        
        // Apply voucher discount (if applicable)
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            Customer customer = booking.getCustomer();
            Voucher voucher = findValidVoucher(voucherCode, customer);
            if (voucher != null) {
                BigDecimal originalAmount = paymentAmount;
                paymentAmount = applyVoucherDiscount(paymentAmount, voucher);
                logger.info("   → After voucher discount: {} (was: {})", paymentAmount, originalAmount);
            }
        }
        
        logger.info("✅ Final payment amount: {}", paymentAmount);
        return paymentAmount;
    }
    
    /**
     * Calculate full payment amount including all items (excluding deposit)
     * @param booking The booking
     * @return Full payment amount (subtotal = table fees + dishes + services)
     */
    private BigDecimal calculateFullPaymentAmount(Booking booking) {
        // Sử dụng BookingService.calculateSubtotal() để lấy subtotal
        // Subtotal = table fees + dishes + services (KHÔNG bao gồm deposit)
        return bookingService.calculateSubtotal(booking);
    }
    
    /**
     * Find valid voucher for customer
     * @param voucherCode The voucher code
     * @param customer The customer
     * @return Voucher if valid
     */
    @SuppressWarnings("unused")
    private Voucher findValidVoucher(@SuppressWarnings("unused") String voucherCode, @SuppressWarnings("unused") Customer customer) {
        // TODO: Implement voucher validation logic
        // Check if voucher exists, is active, not expired, and available for customer
        // Currently not implemented, so returning null
        return null;
    }
    
    /**
     * Apply voucher discount
     * @param amount The original amount
     * @param voucher The voucher
     * @return Discounted amount
     */
    @SuppressWarnings("unused")
    private BigDecimal applyVoucherDiscount(BigDecimal amount, @SuppressWarnings("unused") Voucher voucher) {
        // TODO: Implement voucher discount logic
        // Apply percentage or fixed discount based on voucher type
        // For now, return the original amount
        return amount;
    }
    
    // Scheduled MoMo polling removed
    
    /**
     * Validate payment method and type combination
     * @param paymentMethod The payment method
     * @param paymentType The payment type
     */
    private void validatePaymentMethodAndType(PaymentMethod paymentMethod, PaymentType paymentType) {
        if (paymentType == PaymentType.DEPOSIT && paymentMethod == PaymentMethod.CASH) {
            throw new IllegalArgumentException("Đặt cọc không được phép thanh toán bằng tiền mặt. Vui lòng chọn PayOS.");
        }
        
        // Additional validation rules can be added here
        // For example: ZaloPay only for full payment, etc.
    }
    
    /**
     * Find existing payment for booking and type
     * @param booking The booking
     * @param paymentType The payment type
     * @return Existing payment if found
     */
    private Optional<Payment> findExistingPayment(Booking booking, PaymentType paymentType) {
        // Find payment by booking and payment type
        // This ensures idempotency - same booking + same payment type = reuse existing payment
        return paymentRepository.findByBookingAndPaymentType(booking, paymentType);
    }

    @SuppressWarnings("unused")
    private static class WebhookPayload {
        public String code;
        public String desc;
        public Boolean success;
        public Data data;
        public String signature;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressWarnings("unused")
    private static class Data {
        public Object orderCode; // may be number or string
        public Long amount;
        public String status;
        public String paymentLinkId;
    }
    
    /**
     * Generate unique orderCode for PayOS
     * Strategy: bookingId * 1000000 + timestamp % 1000000
     * @param bookingId The booking ID
     * @return Unique orderCode
     */
    private Long generateUniqueOrderCode(Integer bookingId) {
        long timestamp = System.currentTimeMillis() % 1000000;
        Long orderCode = bookingId * 1000000L + timestamp;
        
        // Ensure uniqueness by checking database
        while (paymentRepository.existsByOrderCode(orderCode)) {
            timestamp = (timestamp + 1) % 1000000;
            orderCode = bookingId * 1000000L + timestamp;
        }
        
        logger.info("Generated orderCode: {} for bookingId: {}", orderCode, bookingId);
        return orderCode;
    }
    
    /**
     * Find payment by orderCode
     * @param orderCode The PayOS order code
     * @return Optional containing the Payment if found
     */
    public Optional<Payment> findByOrderCode(Long orderCode) {
        return paymentRepository.findByOrderCode(orderCode);
    }

    /**
     * Complete booking after payment
     */
    private void completeBooking(Booking booking) {
        booking.setStatus(com.example.booking.common.enums.BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }

    /**
     * Confirm booking after payment
     */
    private void confirmBooking(Booking booking) {
        booking.setStatus(com.example.booking.common.enums.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    /**
     * Calculate total amount for booking (internal method)
     * Note: This method is kept for backward compatibility but uses BookingService.calculateSubtotal()
     */
    private BigDecimal calculateTotalAmount(Booking booking) {
        // Sử dụng BookingService.calculateSubtotal() thay vì tự tính
        // Để đảm bảo consistency với logic tính toán
        return bookingService.calculateSubtotal(booking);
    }
}
