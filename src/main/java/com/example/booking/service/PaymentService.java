package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.Voucher;
import com.example.booking.common.enums.PaymentType;
import com.example.booking.dto.MoMoCreateRequest;
import com.example.booking.dto.MoMoCreateResponse;
import com.example.booking.dto.MoMoIpnRequest;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.VoucherRepository;

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
    private VoucherRepository voucherRepository;
    
    @Autowired
    private MoMoService moMoService;
    
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
        
        // Validate payment method and type combination
        validatePaymentMethodAndType(paymentMethod, paymentType);
        
        // Check if payment already exists for this booking and type
        Optional<Payment> existingPayment = findExistingPayment(booking, paymentType);
        if (existingPayment.isPresent()) {
            logger.warn("Payment already exists for bookingId: {} and type: {}", bookingId, paymentType);
            return existingPayment.get();
        }
        
        // Calculate total amount based on payment type
        BigDecimal totalAmount = calculateTotalAmount(booking, paymentType, voucherCode);
        
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
        
        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        
        logger.info("Payment created successfully. PaymentId: {}, Amount: {}", 
            savedPayment.getPaymentId(), savedPayment.getAmount());
        
        return savedPayment;
    }
    
    /**
     * Process MoMo payment
     * @param request The MoMo create request
     * @param customerId The customer ID
     * @return MoMo create response
     */
    public MoMoCreateResponse processMoMoPayment(MoMoCreateRequest request, UUID customerId) {
        logger.info("Processing MoMo payment for bookingId: {}", request.getBookingId());
        
        // Create payment first
        Payment payment = createPayment(request.getBookingId(), customerId, 
                                      PaymentMethod.MOMO, PaymentType.DEPOSIT, request.getVoucherCode());
        
        // Convert BigDecimal amount to Long for MoMo API (amount is already in VND)
        Long amountInVND = request.getAmount().longValue();
        request.setAmount(amountInVND);
        
        // Create MoMo payment
        MoMoCreateResponse response = moMoService.createPayment(request, payment);
        
        logger.info("MoMo payment processed successfully. OrderId: {}", response.getOrderId());
        
        return response;
    }
    
    /**
     * Handle MoMo IPN
     * @param ipnRequest The IPN request
     * @return true if processed successfully
     */
    public boolean handleMoMoIpn(MoMoIpnRequest ipnRequest) {
        logger.info("Handling MoMo IPN for orderId: {}", ipnRequest.getOrderId());
        
        boolean success = moMoService.handleIpn(ipnRequest);
        
        if (success) {
            logger.info("MoMo IPN processed successfully for orderId: {}", ipnRequest.getOrderId());
        } else {
            logger.error("Failed to process MoMo IPN for orderId: {}", ipnRequest.getOrderId());
        }
        
        return success;
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
     * Get payment by MoMo order ID
     * @param orderId The MoMo order ID
     * @return Payment if found
     */
    public Optional<Payment> findByMoMoOrderId(String orderId) {
        return paymentRepository.findByMomoOrderId(orderId);
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
     * Get pending MoMo payments that need querying
     * @return List of pending payments
     */
    public List<Payment> getPendingMoMoPayments() {
        return paymentRepository.findPendingMoMoPayments(PaymentStatus.PENDING);
    }
    
    /**
     * Get processing MoMo payments that need querying
     * @return List of processing payments
     */
    public List<Payment> getProcessingMoMoPayments() {
        return paymentRepository.findPendingMoMoPayments(PaymentStatus.PROCESSING);
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
     * @param booking The booking
     * @param paymentType The payment type
     * @param voucherCode The voucher code
     * @return Total amount
     */
    private BigDecimal calculateTotalAmount(Booking booking, PaymentType paymentType, String voucherCode) {
        BigDecimal totalAmount;
        
        if (paymentType == PaymentType.DEPOSIT) {
            // For deposit, use the deposit amount from booking
            totalAmount = booking.getDepositAmount();
        } else {
            // For full payment, calculate total amount including dishes, services, etc.
            totalAmount = calculateFullPaymentAmount(booking);
        }
        
        // Apply voucher discount
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            Customer customer = booking.getCustomer();
            Voucher voucher = findValidVoucher(voucherCode, customer);
            if (voucher != null) {
                totalAmount = applyVoucherDiscount(totalAmount, voucher);
            }
        }
        
        return totalAmount;
    }
    
    /**
     * Calculate full payment amount including all items
     * @param booking The booking
     * @return Full payment amount
     */
    private BigDecimal calculateFullPaymentAmount(Booking booking) {
        BigDecimal totalAmount = booking.getDepositAmount();
        
        // TODO: Add logic to calculate dish prices, service prices, etc.
        // For now, just use deposit amount as base
        // In the future, this should include:
        // - Dish prices from bookingDishes
        // - Service prices from bookingServices
        // - Table fees
        // - Any additional charges
        
        return totalAmount;
    }
    
    /**
     * Find valid voucher for customer
     * @param voucherCode The voucher code
     * @param customer The customer
     * @return Voucher if valid
     */
    private Voucher findValidVoucher(String voucherCode, Customer customer) {
        // TODO: Implement voucher validation logic
        // Check if voucher exists, is active, not expired, and available for customer
        return null;
    }
    
    /**
     * Apply voucher discount
     * @param amount The original amount
     * @param voucher The voucher
     * @return Discounted amount
     */
    private BigDecimal applyVoucherDiscount(BigDecimal amount, Voucher voucher) {
        // TODO: Implement voucher discount logic
        // Apply percentage or fixed discount based on voucher type
        // For now, return the original amount
        return amount;
    }
    
    /**
     * Scheduled task to query pending MoMo payments (fallback mechanism)
     * Runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void queryPendingMoMoPayments() {
        try {
            List<Payment> pendingPayments = getPendingMoMoPayments();
            List<Payment> processingPayments = getProcessingMoMoPayments();
            
            logger.info("Querying {} pending and {} processing MoMo payments", 
                pendingPayments.size(), processingPayments.size());
            
            // Query pending payments
            for (Payment payment : pendingPayments) {
                if (payment.getMomoOrderId() != null) {
                    moMoService.queryPaymentStatus(payment.getMomoOrderId());
                }
            }
            
            // Query processing payments
            for (Payment payment : processingPayments) {
                if (payment.getMomoOrderId() != null) {
                    moMoService.queryPaymentStatus(payment.getMomoOrderId());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in scheduled MoMo payment query", e);
        }
    }
    
    /**
     * Validate payment method and type combination
     * @param paymentMethod The payment method
     * @param paymentType The payment type
     */
    private void validatePaymentMethodAndType(PaymentMethod paymentMethod, PaymentType paymentType) {
        if (paymentType == PaymentType.DEPOSIT && paymentMethod == PaymentMethod.CASH) {
            throw new IllegalArgumentException("Đặt cọc không được phép thanh toán bằng tiền mặt. Vui lòng chọn MoMo.");
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
        // TODO: Implement query to find payment by booking and type
        // For now, just check if any payment exists for the booking
        // In the future, this should query by both booking and paymentType
        return paymentRepository.findByBooking(booking);
    }
}
