package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.User;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.EmailService;
import com.example.booking.service.PayOsService;
import com.example.booking.service.PaymentService;
import com.example.booking.service.RefundService;
import com.example.booking.repository.VoucherRedemptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import java.util.Arrays;
import java.util.List;
import com.example.booking.domain.VoucherRedemption;
import com.example.booking.domain.Voucher;

/**
 * Unit tests for PaymentController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Tests")
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CustomerService customerService;

    @Mock
    private PayOsService payOsService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RefundService refundService;

    @Mock
    private VoucherRedemptionRepository voucherRedemptionRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PaymentController paymentController;

    private Booking booking;
    private Customer customer;
    private User user;
    private Payment payment;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("customer@test.com");

        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(user);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setDepositAmount(new BigDecimal("500000"));
        booking.setBookingTime(LocalDateTime.now().plusHours(2));

        payment = new Payment();
        payment.setPaymentId(1);
        payment.setBooking(booking);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.PAYOS);
        payment.setOrderCode(12345L);
        payment.setCustomer(customer);

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUsername(anyString())).thenReturn(Optional.of(customer));
    }

    // ========== showPaymentForm() Tests ==========

    @Test
    @DisplayName("shouldShowPaymentForm_successfully")
    void shouldShowPaymentForm_successfully() {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldRedirect_whenBookingNotFound")
    void shouldRedirect_whenBookingNotFound() {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.empty());

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertEquals("error/404", view);
    }

    @Test
    @DisplayName("shouldReturn403_whenUnauthorizedCustomer")
    void shouldReturn403_whenUnauthorizedCustomer() {
        // Given
        Customer otherCustomer = new Customer();
        otherCustomer.setCustomerId(UUID.randomUUID());
        booking.setCustomer(otherCustomer);
        
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertEquals("error/403", view);
    }

    @Test
    @DisplayName("shouldRedirect_whenPaymentAlreadyCompleted")
    void shouldRedirect_whenPaymentAlreadyCompleted() {
        // Given
        Payment completedPayment = new Payment();
        completedPayment.setPaymentId(2);
        completedPayment.setStatus(PaymentStatus.COMPLETED);
        
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(paymentService.findByBooking(any(Booking.class))).thenReturn(Optional.of(completedPayment));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertTrue(view.contains("booking/my"));
    }

    @Test
    @DisplayName("shouldHandleVoucherDiscount_whenVoucherApplied")
    void shouldHandleVoucherDiscount_whenVoucherApplied() {
        // Given
        Voucher voucher = new Voucher();
        voucher.setCode("DISCOUNT10");
        
        VoucherRedemption redemption = new VoucherRedemption();
        redemption.setVoucher(voucher);
        redemption.setDiscountApplied(new BigDecimal("100000"));
        
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(paymentService.findByBooking(any(Booking.class))).thenReturn(Optional.empty());
        when(bookingService.calculateTotalAmount(any(Booking.class))).thenReturn(new BigDecimal("1000000"));
        when(voucherRedemptionRepository.findByBooking_BookingId(1)).thenReturn(Arrays.asList(redemption));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== handlePayOsReturn() Tests ==========

    @Test
    @DisplayName("shouldHandlePayOsReturn_successfully")
    void shouldHandlePayOsReturn_successfully() {
        // Given
        when(paymentService.findByOrderCode(12345L)).thenReturn(Optional.of(payment));

        // When
        String view = paymentController.handlePayOsReturn("12345", "PAID", redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("payment/result") || view.contains("booking/my"));
    }

    @Test
    @DisplayName("shouldHandlePayOsReturn_whenOrderCodeNull")
    void shouldHandlePayOsReturn_whenOrderCodeNull() {
        // When
        String view = paymentController.handlePayOsReturn(null, "PAID", redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("booking/my"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldHandlePayOsReturn_whenPaymentNotFound")
    void shouldHandlePayOsReturn_whenPaymentNotFound() {
        // Given
        when(paymentService.findByOrderCode(12345L)).thenReturn(Optional.empty());

        // When
        String view = paymentController.handlePayOsReturn("12345", "PAID", redirectAttributes);

        // Then
        assertTrue(view.contains("booking/my"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // ========== handlePayOsCancel() Tests ==========

    @Test
    @DisplayName("shouldHandlePayOsCancel_successfully")
    void shouldHandlePayOsCancel_successfully() {
        // Given
        when(paymentService.findByOrderCode(12345L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        String view = paymentController.handlePayOsCancel("12345", redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("payment/") || view.contains("booking/my"));
    }

    // ========== processPayment() Tests ==========

    @Test
    @DisplayName("shouldProcessPayment_Cash_Successfully")
    void shouldProcessPayment_Cash_Successfully() {
        // Given
        Payment cashPayment = new Payment();
        cashPayment.setPaymentId(2);
        cashPayment.setPaymentMethod(PaymentMethod.CASH);
        cashPayment.setBooking(booking);
        
        when(paymentService.createPayment(eq(1), any(UUID.class), eq(PaymentMethod.CASH), eq(PaymentType.FULL_PAYMENT), isNull()))
            .thenReturn(cashPayment);
        doNothing().when(paymentService).processCashPayment(2);
        when(bookingService.calculateTotalAmount(any(Booking.class))).thenReturn(new BigDecimal("1000000"));

        // When
        String view = paymentController.processPayment(1, PaymentMethod.CASH, PaymentType.FULL_PAYMENT, null, 
            authentication, redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("booking/my"));
        verify(paymentService, times(1)).processCashPayment(2);
    }

    @Test
    @DisplayName("shouldProcessPayment_PayOS_Successfully")
    void shouldProcessPayment_PayOS_Successfully() {
        // Given
        PayOsService.CreateLinkResponse linkResponse = mock(PayOsService.CreateLinkResponse.class);
        PayOsService.CreateLinkResponse.Data linkData = mock(PayOsService.CreateLinkResponse.Data.class);
        
        when(linkResponse.getData()).thenReturn(linkData);
        when(linkData.getCheckoutUrl()).thenReturn("https://payos.vn/checkout");
        when(paymentService.createPayment(eq(1), any(UUID.class), eq(PaymentMethod.PAYOS), any(PaymentType.class), isNull()))
            .thenReturn(payment);
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(linkResponse);

        // When
        String view = paymentController.processPayment(1, PaymentMethod.PAYOS, PaymentType.DEPOSIT, null,
            authentication, redirectAttributes);

        // Then
        assertNotNull(view);
        verify(payOsService, times(1)).createPaymentLink(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenCashDepositNotAllowed")
    void shouldReturnError_whenCashDepositNotAllowed() {
        // Given
        Payment cashPayment = new Payment();
        cashPayment.setPaymentId(2);
        cashPayment.setPaymentMethod(PaymentMethod.CASH);
        
        when(paymentService.createPayment(eq(1), any(UUID.class), eq(PaymentMethod.CASH), eq(PaymentType.DEPOSIT), isNull()))
            .thenReturn(cashPayment);

        // When
        String view = paymentController.processPayment(1, PaymentMethod.CASH, PaymentType.DEPOSIT, null,
            authentication, redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("payment/"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), anyString());
        verify(paymentService, never()).processCashPayment(anyInt());
    }

    // ========== showPaymentResult() Tests ==========

    @Test
    @DisplayName("shouldShowPaymentResult_successfully")
    void shouldShowPaymentResult_successfully() {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(bookingService.calculateTotalAmount(any(Booking.class))).thenReturn(new BigDecimal("1000000"));

        // When
        String view = paymentController.showPaymentResult(1, model, authentication);

        // Then
        assertEquals("payment/result", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldReturn404_whenPaymentNotFound")
    void shouldReturn404_whenPaymentNotFound() {
        // Given
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        String view = paymentController.showPaymentResult(999, model, authentication);

        // Then
        assertEquals("error/404", view);
    }

    // ========== createPayOSLink() Tests ==========

    @Test
    @DisplayName("shouldCreatePayOSLink_successfully")
    void shouldCreatePayOSLink_successfully() {
        // Given
        PayOsService.CreateLinkResponse linkResponse = mock(PayOsService.CreateLinkResponse.class);
        PayOsService.CreateLinkResponse.Data linkData = mock(PayOsService.CreateLinkResponse.Data.class);
        
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(linkResponse.getData()).thenReturn(linkData);
        when(linkData.getCheckoutUrl()).thenReturn("https://payos.vn/checkout");
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(linkResponse);

        // When
        ResponseEntity<?> response = paymentController.createPayOSLink(1, authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnError_whenPaymentNotFoundForPayOSLink")
    void shouldReturnError_whenPaymentNotFoundForPayOSLink() {
        // Given
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = paymentController.createPayOSLink(999, authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    // ========== confirmCashPayment() Tests ==========

    @Test
    @DisplayName("shouldConfirmCashPayment_successfully")
    void shouldConfirmCashPayment_successfully() {
        // Given
        Payment cashPayment = new Payment();
        cashPayment.setPaymentId(2);
        cashPayment.setPaymentMethod(PaymentMethod.CASH);
        cashPayment.setStatus(PaymentStatus.PENDING);
        cashPayment.setCustomer(customer);
        
        when(paymentService.findById(2)).thenReturn(Optional.of(cashPayment));
        doNothing().when(paymentService).processCashPayment(2);

        // When
        ResponseEntity<?> response = paymentController.confirmCashPayment(2, authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(paymentService, times(1)).processCashPayment(2);
    }

    // ========== checkPayOSStatus() Tests ==========

    @Test
    @DisplayName("shouldCheckPayOSStatus_successfully")
    void shouldCheckPayOSStatus_successfully() {
        // Given
        PayOsService.PaymentInfoResponse infoResponse = mock(PayOsService.PaymentInfoResponse.class);
        PayOsService.PaymentInfoResponse.PaymentInfoData paymentData = mock(PayOsService.PaymentInfoResponse.PaymentInfoData.class);
        
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(payOsService.getPaymentInfo(anyLong())).thenReturn(infoResponse);
        when(infoResponse.getCode()).thenReturn("00");
        when(infoResponse.getData()).thenReturn(paymentData);
        when(paymentData.getStatus()).thenReturn("PAID");
        when(paymentData.getAmountPaid()).thenReturn(500000L);
        when(paymentData.getAmountRemaining()).thenReturn(0L);

        // When
        ResponseEntity<?> response = paymentController.checkPayOSStatus(1, authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    // ========== cancelPayOSPayment() Tests ==========

    @Test
    @DisplayName("shouldCancelPayOSPayment_successfully")
    void shouldCancelPayOSPayment_successfully() {
        // Given
        PayOsService.CancelPaymentResponse cancelResponse = mock(PayOsService.CancelPaymentResponse.class);
        
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(payOsService.cancelPayment(anyLong(), isNull())).thenReturn(cancelResponse);
        when(cancelResponse.getCode()).thenReturn("00");
        doNothing().when(paymentService).cancelPayment(1);

        // When
        ResponseEntity<?> response = paymentController.cancelPayOSPayment(1, "test reason", authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(paymentService, times(1)).cancelPayment(1);
    }

    // ========== handlePayOSWebhook() Tests ==========

    @Test
    @DisplayName("shouldHandlePayOSWebhook_successfully")
    void shouldHandlePayOSWebhook_successfully() {
        // Given
        String webhookBody = "{\"code\":\"00\",\"data\":{}}";
        when(payOsService.verifyWebhook(anyString(), anyString())).thenReturn(true);
        when(paymentService.handlePayOsWebhook(anyString())).thenReturn(true);

        // When
        ResponseEntity<?> response = paymentController.handlePayOSWebhook(webhookBody, "valid-signature");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(paymentService, times(1)).handlePayOsWebhook(webhookBody);
    }

    @Test
    @DisplayName("shouldReturnError_whenInvalidWebhookSignature")
    void shouldReturnError_whenInvalidWebhookSignature() {
        // Given
        String webhookBody = "{\"code\":\"00\",\"data\":{}}";
        when(payOsService.verifyWebhook(anyString(), anyString())).thenReturn(false);

        // When
        ResponseEntity<?> response = paymentController.handlePayOSWebhook(webhookBody, "invalid-signature");

        // Then
        assertEquals(400, response.getStatusCode().value());
        verify(paymentService, never()).handlePayOsWebhook(anyString());
    }

    // ========== getPaymentStatus() Tests ==========

    @Test
    @DisplayName("shouldGetPaymentStatus_successfully")
    void shouldGetPaymentStatus_successfully() {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        ResponseEntity<Payment> response = paymentController.getPaymentStatus(1, authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Payment responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.getPaymentId());
    }

    @Test
    @DisplayName("shouldReturn404_whenPaymentNotFoundForStatus")
    void shouldReturn404_whenPaymentNotFoundForStatus() {
        // Given
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Payment> response = paymentController.getPaymentStatus(999, authentication);

        // Then
        assertEquals(404, response.getStatusCode().value());
    }

    // ========== getPaymentHistory() Tests ==========

    @Test
    @DisplayName("shouldGetPaymentHistory_successfully")
    void shouldGetPaymentHistory_successfully() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentService.findByCustomer(any(Customer.class))).thenReturn(payments);

        // When
        ResponseEntity<List<Payment>> response = paymentController.getPaymentHistory(authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<Payment> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
    }

    // ========== cancelPayment() Tests ==========

    @Test
    @DisplayName("shouldCancelPayment_successfully")
    void shouldCancelPayment_successfully() {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(paymentService.cancelPayment(1)).thenReturn(payment);

        // When
        ResponseEntity<String> response = paymentController.cancelPayment(1, authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(paymentService, times(1)).cancelPayment(1);
    }

    // ========== processFullRefund() Tests ==========

    @Test
    @DisplayName("shouldProcessFullRefund_successfully")
    void shouldProcessFullRefund_successfully() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(refundService.processFullRefund(eq(1), anyString()))
            .thenReturn(payment);

        // When
        ResponseEntity<?> response = paymentController.processFullRefund(1, "test reason", authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(refundService, times(1)).processFullRefund(eq(1), anyString());
    }

    // ========== processPartialRefund() Tests ==========

    @Test
    @DisplayName("shouldProcessPartialRefund_successfully")
    void shouldProcessPartialRefund_successfully() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(refundService.processPartialRefund(eq(1), any(BigDecimal.class), anyString()))
            .thenReturn(payment);

        // When
        ResponseEntity<?> response = paymentController.processPartialRefund(1, "200000", 
            "test reason", authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(refundService, times(1)).processPartialRefund(eq(1), any(BigDecimal.class), anyString());
    }

    // ========== checkRefundEligibility() Tests ==========

    @Test
    @DisplayName("shouldCheckRefundEligibility_successfully")
    void shouldCheckRefundEligibility_successfully() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(refundService.canRefund(1)).thenReturn(true);

        // When
        ResponseEntity<?> response = paymentController.checkRefundEligibility(1, authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(refundService, times(1)).canRefund(1);
    }

    // ========== getRefundablePayments() Tests ==========

    @Test
    @DisplayName("shouldGetRefundablePayments_successfully")
    void shouldGetRefundablePayments_successfully() {
        // Given
        List<Payment> refundablePayments = Arrays.asList(payment);
        when(refundService.getRefundablePayments()).thenReturn(refundablePayments);

        // When
        ResponseEntity<?> response = paymentController.getRefundablePayments(authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    // ========== getRefundInfo() Tests ==========

    @Test
    @DisplayName("shouldGetRefundInfo_successfully")
    void shouldGetRefundInfo_successfully() {
        // Given
        payment.setRefundedAt(LocalDateTime.now());
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(refundService.getRefundInfo(1)).thenReturn(Optional.of(payment));

        // When
        ResponseEntity<?> response = paymentController.getRefundInfo(1, authentication);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(refundService, times(1)).getRefundInfo(1);
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("shouldHandleException_whenProcessPaymentFails")
    void shouldHandleException_whenProcessPaymentFails() {
        // Given
        when(paymentService.createPayment(eq(1), any(UUID.class), any(PaymentMethod.class), any(PaymentType.class), isNull()))
            .thenThrow(new RuntimeException("Payment creation failed"));

        // When
        String view = paymentController.processPayment(1, PaymentMethod.PAYOS, PaymentType.DEPOSIT, null,
            authentication, redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("payment/"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldHandleException_whenShowPaymentFormFails")
    void shouldHandleException_whenShowPaymentFormFails() {
        // Given
        when(bookingService.findBookingById(1)).thenThrow(new RuntimeException("Database error"));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertEquals("error/500", view);
    }

    @Test
    @DisplayName("shouldShowPaymentForm_withExistingCompletedPayment")
    void shouldShowPaymentForm_withExistingCompletedPayment() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findByBooking(booking)).thenReturn(Optional.of(payment));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertTrue(view.contains("redirect") && view.contains("already_paid"));
    }

    @Test
    @DisplayName("shouldShowPaymentForm_withProcessingPayment")
    void shouldShowPaymentForm_withProcessingPayment() {
        // Given
        payment.setStatus(PaymentStatus.PROCESSING);
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findByBooking(booking)).thenReturn(Optional.of(payment));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertTrue(view.contains("redirect") && view.contains("result"));
    }

    @Test
    @DisplayName("shouldDenyAccess_whenCustomerDoesNotOwnBooking")
    void shouldDenyAccess_whenCustomerDoesNotOwnBooking() {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(differentCustomerId);
        differentCustomer.setUser(user);
        booking.setCustomer(differentCustomer);
        
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertEquals("error/403", view);
    }

    // ========== processPayment() Tests ==========

    @Test
    @DisplayName("shouldProcessCashPayment_successfully")
    void shouldProcessCashPayment_successfully() {
        // Given
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.createPayment(eq(1), any(UUID.class), eq(PaymentMethod.CASH), 
            eq(PaymentType.FULL_PAYMENT), isNull())).thenReturn(payment);
        when(bookingService.calculateTotalAmount(booking)).thenReturn(new BigDecimal("1000000"));

        // When
        String result = paymentController.processPayment(1, PaymentMethod.CASH, PaymentType.FULL_PAYMENT, 
            null, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/booking/my"));
        verify(paymentService).processCashPayment(payment.getPaymentId());
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    @DisplayName("shouldRejectCashPayment_forDeposit")
    void shouldRejectCashPayment_forDeposit() {
        // Given
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.createPayment(eq(1), any(UUID.class), eq(PaymentMethod.CASH), 
            eq(PaymentType.DEPOSIT), isNull())).thenReturn(payment);

        // When
        String result = paymentController.processPayment(1, PaymentMethod.CASH, PaymentType.DEPOSIT, 
            null, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/payment/"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldProcessCardPayment_successfully")
    void shouldProcessCardPayment_successfully() {
        // Given
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        payment.setPaymentMethod(PaymentMethod.CARD);
        when(paymentService.createPayment(eq(1), any(UUID.class), eq(PaymentMethod.CARD), 
            eq(PaymentType.DEPOSIT), isNull())).thenReturn(payment);

        // When
        String result = paymentController.processPayment(1, PaymentMethod.CARD, PaymentType.DEPOSIT, 
            null, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/booking/my"));
        verify(paymentService).processCardPayment(payment.getPaymentId());
    }

    @Test
    @DisplayName("shouldProcessPayOSPayment_successfully")
    void shouldProcessPayOSPayment_successfully() {
        // Given
        payment.setOrderCode(123456L);
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.createPayment(eq(1), any(UUID.class), eq(PaymentMethod.PAYOS), 
            eq(PaymentType.DEPOSIT), isNull())).thenReturn(payment);
        
        PayOsService.CreateLinkResponse mockResponse = mock(PayOsService.CreateLinkResponse.class);
        PayOsService.CreateLinkResponse.Data mockData = mock(PayOsService.CreateLinkResponse.Data.class);
        when(mockResponse.getData()).thenReturn(mockData);
        when(mockData.getCheckoutUrl()).thenReturn("https://payos.vn/checkout/123456");
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(mockResponse);

        // When
        String result = paymentController.processPayment(1, PaymentMethod.PAYOS, PaymentType.DEPOSIT, 
            null, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:https://payos.vn"));
        verify(payOsService).createPaymentLink(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("shouldRejectZaloPay_notSupported")
    void shouldRejectZaloPay_notSupported() {
        // Given
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        payment.setPaymentMethod(PaymentMethod.ZALOPAY);
        when(paymentService.createPayment(eq(1), any(UUID.class), eq(PaymentMethod.ZALOPAY), 
            eq(PaymentType.DEPOSIT), isNull())).thenReturn(payment);

        // When
        String result = paymentController.processPayment(1, PaymentMethod.ZALOPAY, PaymentType.DEPOSIT, 
            null, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/payment/"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // ========== showPaymentResult() Tests ==========

    @Test
    @DisplayName("shouldShowPaymentResult_successfully")
    void shouldShowPaymentResult_successfully() {
        // Given
        payment.setPaymentType(PaymentType.DEPOSIT);
        payment.setCustomer(customer);
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(bookingService.calculateTotalAmount(booking)).thenReturn(new BigDecimal("1000000"));

        // When
        String view = paymentController.showPaymentResult(1, model, authentication);

        // Then
        assertEquals("payment/result", view);
        verify(model).addAttribute(eq("payment"), eq(payment));
        verify(model).addAttribute(eq("isDeposit"), eq(true));
    }

    @Test
    @DisplayName("shouldShowPaymentResult_return404WhenNotFound")
    void shouldShowPaymentResult_return404WhenNotFound() {
        // Given
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        String view = paymentController.showPaymentResult(999, model, authentication);

        // Then
        assertEquals("error/404", view);
    }

    @Test
    @DisplayName("shouldShowPaymentResult_return403ForUnauthorized")
    void shouldShowPaymentResult_return403ForUnauthorized() {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(differentCustomerId);
        payment.setCustomer(differentCustomer);
        
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        String view = paymentController.showPaymentResult(1, model, authentication);

        // Then
        assertEquals("error/403", view);
    }

    // ========== handlePayOsReturn() Tests ==========

    @Test
    @DisplayName("shouldHandlePayOsReturn_withCompletedPayment")
    void shouldHandlePayOsReturn_withCompletedPayment() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setOrderCode(123456L);
        when(paymentService.findByOrderCode(123456L)).thenReturn(Optional.of(payment));

        // When
        String result = paymentController.handlePayOsReturn("123456", "PAID", redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/payment/result/"));
    }

    @Test
    @DisplayName("shouldHandlePayOsReturn_withNullOrderCode")
    void shouldHandlePayOsReturn_withNullOrderCode() {
        // When
        String result = paymentController.handlePayOsReturn(null, "PAID", redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/booking/my"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldHandlePayOsReturn_withPaymentNotFound")
    void shouldHandlePayOsReturn_withPaymentNotFound() {
        // Given
        when(paymentService.findByOrderCode(999999L)).thenReturn(Optional.empty());

        // When
        String result = paymentController.handlePayOsReturn("999999", "PAID", redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/booking/my"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // ========== handlePayOsCancel() Tests ==========

    @Test
    @DisplayName("shouldHandlePayOsCancel_successfully")
    void shouldHandlePayOsCancel_successfully() {
        // Given
        payment.setOrderCode(123456L);
        when(paymentService.findByOrderCode(123456L)).thenReturn(Optional.of(payment));

        // When
        String result = paymentController.handlePayOsCancel("123456", redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/payment/"));
        verify(paymentRepository).save(payment);
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
    }

    @Test
    @DisplayName("shouldHandlePayOsCancel_withNullOrderCode")
    void shouldHandlePayOsCancel_withNullOrderCode() {
        // When
        String result = paymentController.handlePayOsCancel(null, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/booking/my"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // ========== getPaymentStatus() Tests ==========

    @Test
    @DisplayName("shouldGetPaymentStatus_successfully")
    void shouldGetPaymentStatus_successfully() {
        // Given
        payment.setCustomer(customer);
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        var response = paymentController.getPaymentStatus(1, authentication);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(payment, response.getBody());
    }

    @Test
    @DisplayName("shouldGetPaymentStatus_return404WhenNotFound")
    void shouldGetPaymentStatus_return404WhenNotFound() {
        // Given
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        var response = paymentController.getPaymentStatus(999, authentication);

        // Then
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("shouldGetPaymentStatus_return403ForUnauthorized")
    void shouldGetPaymentStatus_return403ForUnauthorized() {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(differentCustomerId);
        payment.setCustomer(differentCustomer);
        
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        var response = paymentController.getPaymentStatus(1, authentication);

        // Then
        assertEquals(403, response.getStatusCodeValue());
    }

    // ========== cancelPayment() Tests ==========

    @Test
    @DisplayName("shouldCancelPayment_successfully")
    void shouldCancelPayment_successfully() {
        // Given
        payment.setCustomer(customer);
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        var response = paymentController.cancelPayment(1, authentication);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(paymentService).cancelPayment(1);
    }

    @Test
    @DisplayName("shouldCancelPayment_return404WhenNotFound")
    void shouldCancelPayment_return404WhenNotFound() {
        // Given
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        var response = paymentController.cancelPayment(999, authentication);

        // Then
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("shouldCancelPayment_return403ForUnauthorized")
    void shouldCancelPayment_return403ForUnauthorized() {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(differentCustomerId);
        payment.setCustomer(differentCustomer);
        
        when(authentication.getName()).thenReturn("customer");
        when(customerService.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        var response = paymentController.cancelPayment(1, authentication);

        // Then
        assertEquals(403, response.getStatusCodeValue());
    }
}

