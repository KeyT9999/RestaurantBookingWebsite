package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentService paymentService;

    private Customer customer;
    private Booking booking;
    private RestaurantProfile restaurant;
    private User user;
    private UUID customerId;
    private Integer bookingId;

    @BeforeEach
    void setUp() {
        // Create test data
        customerId = UUID.randomUUID();
        bookingId = 1;

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(user);
        customer.setFullName("Test User");

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusDays(1));
        booking.setNumberOfGuests(4);
        booking.setDepositAmount(new BigDecimal("1000000")); // 1M VND
        booking.setStatus(com.example.booking.common.enums.BookingStatus.PENDING);
    }

    // ==================== CREATE PAYMENT TESTS ====================

    @Test
    @DisplayName("testCreatePayment_WithValidPayOSDeposit_ShouldCreatePayment")
    void testCreatePayment_WithValidPayOSDeposit_ShouldCreatePayment() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(paymentRepository.findByBookingAndPaymentType(booking, PaymentType.DEPOSIT))
                .thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderCode(any())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = paymentService.createPayment(
                bookingId, customerId, PaymentMethod.PAYOS, PaymentType.DEPOSIT, null);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(PaymentMethod.PAYOS, result.getPaymentMethod());
        assertEquals(PaymentType.DEPOSIT, result.getPaymentType());
        assertNotNull(result.getOrderCode());
        assertEquals(new BigDecimal("100000.0"), result.getAmount()); // 10% of 1M
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("testCreatePayment_WithValidZaloPayDeposit_ShouldCreatePayment")
    void testCreatePayment_WithValidZaloPayDeposit_ShouldCreatePayment() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(paymentRepository.findByBookingAndPaymentType(booking, PaymentType.DEPOSIT))
                .thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderCode(any())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = paymentService.createPayment(
                bookingId, customerId, PaymentMethod.ZALOPAY, PaymentType.DEPOSIT, null);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(PaymentMethod.ZALOPAY, result.getPaymentMethod());
        assertEquals(PaymentType.DEPOSIT, result.getPaymentType());
        assertNotNull(result.getOrderCode());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("testCreatePayment_WithFullPaymentCash_ShouldCreatePayment")
    void testCreatePayment_WithFullPaymentCash_ShouldCreatePayment() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(paymentRepository.findByBookingAndPaymentType(booking, PaymentType.FULL_PAYMENT))
                .thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderCode(any())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = paymentService.createPayment(
                bookingId, customerId, PaymentMethod.CASH, PaymentType.FULL_PAYMENT, null);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(PaymentMethod.CASH, result.getPaymentMethod());
        assertEquals(PaymentType.FULL_PAYMENT, result.getPaymentType());
        assertEquals(new BigDecimal("1000000"), result.getAmount()); // Full amount
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("testCreatePayment_WithExistingPendingPayment_ShouldReuse")
    void testCreatePayment_WithExistingPendingPayment_ShouldReuse() {
        // Given
        Payment existingPayment = new Payment();
        existingPayment.setPaymentId(1);
        existingPayment.setStatus(PaymentStatus.PENDING);
        existingPayment.setAmount(new BigDecimal("100000"));
        existingPayment.setOrderCode(123456L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(paymentRepository.findByBookingAndPaymentType(booking, PaymentType.DEPOSIT))
                .thenReturn(Optional.of(existingPayment));

        // When
        Payment result = paymentService.createPayment(
                bookingId, customerId, PaymentMethod.PAYOS, PaymentType.DEPOSIT, null);

        // Then
        assertNotNull(result);
        assertEquals(existingPayment.getPaymentId(), result.getPaymentId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("testCreatePayment_WithCompletedPayment_ShouldReuseExisting")
    void testCreatePayment_WithCompletedPayment_ShouldReuseExisting() {
        // Given
        Payment existingPayment = new Payment();
        existingPayment.setPaymentId(1);
        existingPayment.setStatus(PaymentStatus.COMPLETED);
        existingPayment.setAmount(new BigDecimal("100000"));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(paymentRepository.findByBookingAndPaymentType(booking, PaymentType.DEPOSIT))
                .thenReturn(Optional.of(existingPayment));

        // When
        Payment result = paymentService.createPayment(
                bookingId, customerId, PaymentMethod.PAYOS, PaymentType.DEPOSIT, null);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("testCreatePayment_WithBookingNotFound_ShouldThrowException")
    void testCreatePayment_WithBookingNotFound_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(bookingId, customerId, PaymentMethod.PAYOS, PaymentType.DEPOSIT, null);
        });

        assertEquals("Booking not found: " + bookingId, exception.getMessage());
    }

    @Test
    @DisplayName("testCreatePayment_WithCustomerNotFound_ShouldThrowException")
    void testCreatePayment_WithCustomerNotFound_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(bookingId, customerId, PaymentMethod.PAYOS, PaymentType.DEPOSIT, null);
        });

        assertEquals("Customer not found: " + customerId, exception.getMessage());
    }

    @Test
    @DisplayName("testCreatePayment_WithFullPaymentPayOS_ShouldForceDeposit")
    void testCreatePayment_WithFullPaymentPayOS_ShouldForceDeposit() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(paymentRepository.findByBookingAndPaymentType(eq(booking), eq(PaymentType.DEPOSIT)))
                .thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderCode(any())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = paymentService.createPayment(
                bookingId, customerId, PaymentMethod.PAYOS, PaymentType.FULL_PAYMENT, null);

        // Then
        assertNotNull(result);
        assertEquals(PaymentType.DEPOSIT, result.getPaymentType()); // Forced to DEPOSIT
        assertEquals(new BigDecimal("100000.0"), result.getAmount()); // 10% deposit
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("testCreatePayment_WithDepositCashMethod_ShouldThrowException")
    void testCreatePayment_WithDepositCashMethod_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(bookingId, customerId, PaymentMethod.CASH, PaymentType.DEPOSIT, null);
        });

        assertTrue(exception.getMessage().contains("Đặt cọc không được phép thanh toán bằng tiền mặt"));
    }

    // ==================== HANDLE PAYOS WEBHOOK TESTS ====================

    @Test
    @DisplayName("testHandlePayOsWebhook_WithSuccessfulPayment_ShouldUpdateStatus")
    void testHandlePayOsWebhook_WithSuccessfulPayment_ShouldUpdateStatus() throws Exception {
        // Given
        String rawBody = "{\"success\":true,\"data\":{\"orderCode\":123456,\"code\":\"00\",\"reference\":\"REF123\"}}";
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setBooking(booking);
        payment.setAmount(new BigDecimal("100000"));

        // Create mock using ObjectMapper since WebhookRequest has no setters
        ObjectMapper realMapper = new ObjectMapper();
        PayOsService.WebhookRequest mockRequest = realMapper.readValue(rawBody, PayOsService.WebhookRequest.class);

        when(objectMapper.readValue(rawBody, PayOsService.WebhookRequest.class)).thenReturn(mockRequest);
        when(paymentRepository.findByOrderCode(123456L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean result = paymentService.handlePayOsWebhook(rawBody);

        // Then
        assertTrue(result);
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(payment.getPaidAt());
        assertEquals("00", payment.getPayosCode());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("testHandlePayOsWebhook_WithFailedPayment_ShouldUpdateToFailed")
    void testHandlePayOsWebhook_WithFailedPayment_ShouldUpdateToFailed() throws Exception {
        // Given
        String rawBody = "{\"success\":false,\"data\":{\"orderCode\":123456,\"code\":\"01\"}}";
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setStatus(PaymentStatus.PENDING);

        // Create mock using ObjectMapper since WebhookRequest has no setters
        ObjectMapper realMapper = new ObjectMapper();
        PayOsService.WebhookRequest mockRequest = realMapper.readValue(rawBody, PayOsService.WebhookRequest.class);

        when(objectMapper.readValue(rawBody, PayOsService.WebhookRequest.class)).thenReturn(mockRequest);
        when(paymentRepository.findByOrderCode(123456L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean result = paymentService.handlePayOsWebhook(rawBody);

        // Then
        assertTrue(result);
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("testHandlePayOsWebhook_WithInvalidJson_ShouldReturnFalse")
    void testHandlePayOsWebhook_WithInvalidJson_ShouldReturnFalse() throws Exception {
        // Given
        String rawBody = "invalid json";

        when(objectMapper.readValue(rawBody, PayOsService.WebhookRequest.class))
                .thenThrow(new RuntimeException("Invalid JSON"));

        // When
        boolean result = paymentService.handlePayOsWebhook(rawBody);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("testHandlePayOsWebhook_WithNullData_ShouldReturnFalse")
    void testHandlePayOsWebhook_WithNullData_ShouldReturnFalse() throws Exception {
        // Given
        String rawBody = "{\"success\":true,\"data\":null}";

        // Create mock using ObjectMapper since WebhookRequest has no setters
        ObjectMapper realMapper = new ObjectMapper();
        PayOsService.WebhookRequest mockRequest = realMapper.readValue(rawBody, PayOsService.WebhookRequest.class);

        when(objectMapper.readValue(rawBody, PayOsService.WebhookRequest.class)).thenReturn(mockRequest);

        // When
        boolean result = paymentService.handlePayOsWebhook(rawBody);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("testHandlePayOsWebhook_WithOrderCodeNotFound_ShouldReturnFalse")
    void testHandlePayOsWebhook_WithOrderCodeNotFound_ShouldReturnFalse() throws Exception {
        // Given
        String rawBody = "{\"success\":true,\"data\":{\"orderCode\":999999,\"code\":\"00\"}}";

        // Create mock using ObjectMapper since WebhookRequest has no setters
        ObjectMapper realMapper = new ObjectMapper();
        PayOsService.WebhookRequest mockRequest = realMapper.readValue(rawBody, PayOsService.WebhookRequest.class);

        when(objectMapper.readValue(rawBody, PayOsService.WebhookRequest.class)).thenReturn(mockRequest);
        when(paymentRepository.findByOrderCode(999999L)).thenReturn(Optional.empty());

        // When
        boolean result = paymentService.handlePayOsWebhook(rawBody);

        // Then
        assertFalse(result);
    }

    // ==================== PROCESS CASH PAYMENT TESTS ====================

    @Test
    @DisplayName("testProcessCashPayment_WithValidCashPayment_ShouldComplete")
    void testProcessCashPayment_WithValidCashPayment_ShouldComplete() {
        // Given
        Integer paymentId = 1;
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setBooking(booking);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = paymentService.processCashPayment(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getPaidAt());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("testProcessCashPayment_WithNonExistentPayment_ShouldThrowException")
    void testProcessCashPayment_WithNonExistentPayment_ShouldThrowException() {
        // Given
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processCashPayment(999);
        });

        assertEquals("Payment not found: 999", exception.getMessage());
    }

    @Test
    @DisplayName("testProcessCashPayment_WithNonCashMethod_ShouldThrowException")
    void testProcessCashPayment_WithNonCashMethod_ShouldThrowException() {
        // Given
        Integer paymentId = 1;
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setPaymentMethod(PaymentMethod.PAYOS);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processCashPayment(paymentId);
        });

        assertEquals("Payment method is not cash", exception.getMessage());
    }

    // ==================== PROCESS CARD PAYMENT TESTS ====================

    @Test
    @DisplayName("testProcessCardPayment_WithValidCardPayment_ShouldComplete")
    void testProcessCardPayment_WithValidCardPayment_ShouldComplete() {
        // Given
        Integer paymentId = 1;
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setBooking(booking);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = paymentService.processCardPayment(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getPaidAt());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("testProcessCardPayment_WithNonExistentPayment_ShouldThrowException")
    void testProcessCardPayment_WithNonExistentPayment_ShouldThrowException() {
        // Given
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processCardPayment(999);
        });

        assertEquals("Payment not found: 999", exception.getMessage());
    }

    @Test
    @DisplayName("testProcessCardPayment_WithNonCardMethod_ShouldThrowException")
    void testProcessCardPayment_WithNonCardMethod_ShouldThrowException() {
        // Given
        Integer paymentId = 1;
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setPaymentMethod(PaymentMethod.CASH);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processCardPayment(paymentId);
        });

        assertEquals("Payment method is not card", exception.getMessage());
    }

    // ==================== CANCEL PAYMENT TESTS ====================

    @Test
    @DisplayName("testCancelPayment_WithPendingPayment_ShouldCancel")
    void testCancelPayment_WithPendingPayment_ShouldCancel() {
        // Given
        Integer paymentId = 1;
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = paymentService.cancelPayment(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.CANCELLED, result.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("testCancelPayment_WithCompletedPayment_ShouldThrowException")
    void testCancelPayment_WithCompletedPayment_ShouldThrowException() {
        // Given
        Integer paymentId = 1;
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.cancelPayment(paymentId);
        });

        assertEquals("Cannot cancel completed payment", exception.getMessage());
    }

    @Test
    @DisplayName("testCancelPayment_WithNonExistentPayment_ShouldThrowException")
    void testCancelPayment_WithNonExistentPayment_ShouldThrowException() {
        // Given
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.cancelPayment(999);
        });

        assertEquals("Payment not found: 999", exception.getMessage());
    }

    // ==================== REFUND PAYMENT TESTS ====================

    @Test
    @DisplayName("testRefundPayment_WithCompletedPayment_ShouldRefund")
    void testRefundPayment_WithCompletedPayment_ShouldRefund() {
        // Given
        Integer paymentId = 1;
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = paymentService.refundPayment(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.REFUNDED, result.getStatus());
        assertNotNull(result.getRefundedAt());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("testRefundPayment_WithNonCompletedPayment_ShouldThrowException")
    void testRefundPayment_WithNonCompletedPayment_ShouldThrowException() {
        // Given
        Integer paymentId = 1;
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.refundPayment(paymentId);
        });

        assertEquals("Only completed payments can be refunded", exception.getMessage());
    }

    @Test
    @DisplayName("testRefundPayment_WithNonExistentPayment_ShouldThrowException")
    void testRefundPayment_WithNonExistentPayment_ShouldThrowException() {
        // Given
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.refundPayment(999);
        });

        assertEquals("Payment not found: 999", exception.getMessage());
    }

    // ==================== FIND BY CUSTOMER TESTS ====================

    @Test
    @DisplayName("testFindByCustomer_WithMultiplePayments_ShouldReturnAll")
    void testFindByCustomer_WithMultiplePayments_ShouldReturnAll() {
        // Given
        Payment payment1 = new Payment();
        payment1.setPaymentId(1);
        payment1.setPaidAt(LocalDateTime.now().minusDays(3));

        Payment payment2 = new Payment();
        payment2.setPaymentId(2);
        payment2.setPaidAt(LocalDateTime.now().minusDays(1));

        Payment payment3 = new Payment();
        payment3.setPaymentId(3);
        payment3.setPaidAt(LocalDateTime.now().minusDays(2));

        when(paymentRepository.findByCustomerOrderByPaidAtDesc(customer))
                .thenReturn(Arrays.asList(payment2, payment3, payment1));

        // When
        List<Payment> result = paymentService.findByCustomer(customer);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(2, result.get(0).getPaymentId()); // Most recent first
    }

    // ==================== FIND BY BOOKING TESTS ====================

    @Test
    @DisplayName("testFindByBooking_WithExistingPayment_ShouldReturnPayment")
    void testFindByBooking_WithExistingPayment_ShouldReturnPayment() {
        // Given
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setBooking(booking);

        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));

        // When
        Optional<Payment> result = paymentService.findByBooking(booking);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getPaymentId());
    }

    @Test
    @DisplayName("testFindByBooking_WithNoPayment_ShouldReturnEmpty")
    void testFindByBooking_WithNoPayment_ShouldReturnEmpty() {
        // Given
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        // When
        Optional<Payment> result = paymentService.findByBooking(booking);

        // Then
        assertFalse(result.isPresent());
    }

    // ==================== FIND BY STATUS TESTS ====================

    @Test
    @DisplayName("testFindByStatus_WithPendingStatus_ShouldReturnPendingPayments")
    void testFindByStatus_WithPendingStatus_ShouldReturnPendingPayments() {
        // Given
        Payment payment1 = new Payment();
        payment1.setPaymentId(1);
        payment1.setStatus(PaymentStatus.PENDING);

        Payment payment2 = new Payment();
        payment2.setPaymentId(2);
        payment2.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByStatus(PaymentStatus.PENDING))
                .thenReturn(Arrays.asList(payment1, payment2));

        // When
        List<Payment> result = paymentService.findByStatus(PaymentStatus.PENDING);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(p -> assertEquals(PaymentStatus.PENDING, p.getStatus()));
    }

    @Test
    @DisplayName("testFindByStatus_WithCompletedStatus_ShouldReturnCompletedPayments")
    void testFindByStatus_WithCompletedStatus_ShouldReturnCompletedPayments() {
        // Given
        Payment payment1 = new Payment();
        payment1.setPaymentId(1);
        payment1.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED))
                .thenReturn(Arrays.asList(payment1));

        // When
        List<Payment> result = paymentService.findByStatus(PaymentStatus.COMPLETED);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PaymentStatus.COMPLETED, result.get(0).getStatus());
    }
}
