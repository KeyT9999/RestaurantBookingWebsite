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
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
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

    // ========== Refund Error Cases - Covering HashMap Initializations ==========

    @Test
    @DisplayName("shouldReturnUnauthorized_whenProcessFullRefundWithoutAuth")
    void shouldReturnUnauthorized_whenProcessFullRefundWithoutAuth() {
        // Given
        Authentication nullAuth = null;

        // When
        ResponseEntity<?> response = paymentController.processFullRefund(1, "reason", nullAuth);

        // Then
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnBadRequest_whenProcessFullRefundWithIllegalArgument")
    void shouldReturnBadRequest_whenProcessFullRefundWithIllegalArgument() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(refundService.processFullRefund(eq(1), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid payment"));

        // When
        ResponseEntity<?> response = paymentController.processFullRefund(1, "reason", authentication);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturn500_whenProcessFullRefundWithException")
    void shouldReturn500_whenProcessFullRefundWithException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(refundService.processFullRefund(eq(1), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = paymentController.processFullRefund(1, "reason", authentication);

        // Then
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnUnauthorized_whenProcessPartialRefundWithoutAuth")
    void shouldReturnUnauthorized_whenProcessPartialRefundWithoutAuth() {
        // Given
        Authentication nullAuth = null;

        // When
        ResponseEntity<?> response = paymentController.processPartialRefund(1, "200000", "reason", nullAuth);

        // Then
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnBadRequest_whenProcessPartialRefundWithInvalidAmount")
    void shouldReturnBadRequest_whenProcessPartialRefundWithInvalidAmount() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);

        // When
        ResponseEntity<?> response = paymentController.processPartialRefund(1, "invalid", "reason", authentication);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnBadRequest_whenProcessPartialRefundWithIllegalArgument")
    void shouldReturnBadRequest_whenProcessPartialRefundWithIllegalArgument() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(refundService.processPartialRefund(eq(1), any(BigDecimal.class), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid refund amount"));

        // When
        ResponseEntity<?> response = paymentController.processPartialRefund(1, "200000", "reason", authentication);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturn500_whenProcessPartialRefundWithException")
    void shouldReturn500_whenProcessPartialRefundWithException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(refundService.processPartialRefund(eq(1), any(BigDecimal.class), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = paymentController.processPartialRefund(1, "200000", "reason", authentication);

        // Then
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnUnauthorized_whenCheckRefundEligibilityWithoutAuth")
    void shouldReturnUnauthorized_whenCheckRefundEligibilityWithoutAuth() {
        // Given
        Authentication nullAuth = null;

        // When
        ResponseEntity<?> response = paymentController.checkRefundEligibility(1, nullAuth);

        // Then
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturn404_whenCheckRefundEligibilityPaymentNotFound")
    void shouldReturn404_whenCheckRefundEligibilityPaymentNotFound() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = paymentController.checkRefundEligibility(999, authentication);

        // Then
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturn500_whenCheckRefundEligibilityWithException")
    void shouldReturn500_whenCheckRefundEligibilityWithException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(paymentRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = paymentController.checkRefundEligibility(1, authentication);

        // Then
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnUnauthorized_whenGetRefundablePaymentsWithoutAuth")
    void shouldReturnUnauthorized_whenGetRefundablePaymentsWithoutAuth() {
        // Given
        Authentication nullAuth = null;

        // When
        ResponseEntity<?> response = paymentController.getRefundablePayments(nullAuth);

        // Then
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturn500_whenGetRefundablePaymentsWithException")
    void shouldReturn500_whenGetRefundablePaymentsWithException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(refundService.getRefundablePayments()).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = paymentController.getRefundablePayments(authentication);

        // Then
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnUnauthorized_whenGetRefundInfoWithoutAuth")
    void shouldReturnUnauthorized_whenGetRefundInfoWithoutAuth() {
        // Given
        Authentication nullAuth = null;

        // When
        ResponseEntity<?> response = paymentController.getRefundInfo(1, nullAuth);

        // Then
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturn404_whenGetRefundInfoNotFound")
    void shouldReturn404_whenGetRefundInfoNotFound() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(refundService.getRefundInfo(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = paymentController.getRefundInfo(999, authentication);

        // Then
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturn500_whenGetRefundInfoWithException")
    void shouldReturn500_whenGetRefundInfoWithException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(refundService.getRefundInfo(1)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = paymentController.getRefundInfo(1, authentication);

        // Then
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
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

    // ========== Additional PaymentController Endpoints - Batch Coverage ==========

    @Test
    @DisplayName("testCreatePayOSLink - should create test link successfully")
    void testCreatePayOSLink_ShouldReturnSuccess() {
        // Given
        PayOsService.CreateLinkResponse linkResponse = mock(PayOsService.CreateLinkResponse.class);
        PayOsService.CreateLinkResponse.Data linkData = mock(PayOsService.CreateLinkResponse.Data.class);
        
        when(linkResponse.getData()).thenReturn(linkData);
        when(linkData.getCheckoutUrl()).thenReturn("https://payos.vn/test");
        when(linkData.getQrCode()).thenReturn("QR_CODE_DATA");
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(linkResponse);

        // When
        ResponseEntity<?> response = paymentController.testCreatePayOSLink(123L, 500000L, "Test payment");

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("testCreatePayOSLink - should handle null response")
    void testCreatePayOSLink_WithNullResponse_ShouldReturnError() {
        // Given
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(null);

        // When
        ResponseEntity<?> response = paymentController.testCreatePayOSLink(123L, 500000L, "Test payment");

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("testCreatePayOSLink - should handle exception")
    void testCreatePayOSLink_WithException_ShouldReturnError() {
        // Given
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString()))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = paymentController.testCreatePayOSLink(123L, 500000L, "Test payment");

        // Then
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("confirmWebhook - should confirm webhook successfully")
    void confirmWebhook_WithValidUrl_ShouldReturnSuccess() throws Exception {
        // Given
        PayOsService.WebhookConfirmResponse confirmResponse = mock(PayOsService.WebhookConfirmResponse.class);
        PayOsService.WebhookConfirmResponse.WebhookConfirmData confirmData = mock(PayOsService.WebhookConfirmResponse.WebhookConfirmData.class);
        
        when(confirmResponse.getCode()).thenReturn("00");
        when(confirmResponse.getData()).thenReturn(confirmData);
        when(payOsService.confirmWebhook(anyString())).thenReturn(confirmResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        ResponseEntity<?> response = paymentController.confirmWebhook("https://example.com/webhook");

        // Then
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("confirmWebhook - should reject empty URL")
    void confirmWebhook_WithEmptyUrl_ShouldReturnError() {
        // When
        ResponseEntity<?> response = paymentController.confirmWebhook("");

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("confirmWebhook - should handle invalid URL")
    void confirmWebhook_WithInvalidUrl_ShouldReturnError() throws Exception {
        // Given
        when(payOsService.confirmWebhook(anyString()))
            .thenThrow(new IllegalArgumentException("Invalid URL"));

        // When
        ResponseEntity<?> response = paymentController.confirmWebhook("invalid-url");

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("confirmWebhook - should handle failed confirmation")
    void confirmWebhook_WithFailedConfirmation_ShouldReturnError() {
        // Given
        PayOsService.WebhookConfirmResponse confirmResponse = mock(PayOsService.WebhookConfirmResponse.class);
        when(confirmResponse.getCode()).thenReturn("01");
        when(confirmResponse.getDesc()).thenReturn("Failed");
        when(payOsService.confirmWebhook(anyString())).thenReturn(confirmResponse);

        // When
        ResponseEntity<?> response = paymentController.confirmWebhook("https://example.com/webhook");

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("confirmWebhook - should handle exception")
    void confirmWebhook_WithException_ShouldReturnError() {
        // Given
        when(payOsService.confirmWebhook(anyString()))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = paymentController.confirmWebhook("https://example.com/webhook");

        // Then
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("handlePayOSWebhook - should handle null signature")
    void handlePayOSWebhook_WithNullSignature_ShouldReturnError() {
        // Given
        String webhookBody = "{\"code\":\"00\"}";

        // When
        ResponseEntity<?> response = paymentController.handlePayOSWebhook(webhookBody, null);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("handlePayOSWebhook - should handle invalid signature")
    void handlePayOSWebhook_WithInvalidSignature_ShouldReturnError() {
        // Given
        String webhookBody = "{\"code\":\"00\"}";
        when(payOsService.verifyWebhook(anyString(), anyString())).thenReturn(false);

        // When
        ResponseEntity<?> response = paymentController.handlePayOSWebhook(webhookBody, "invalid");

        // Then
        assertEquals(400, response.getStatusCode().value());
        verify(paymentService, never()).handlePayOsWebhook(anyString());
    }

    @Test
    @DisplayName("handlePayOSWebhook - should handle processing failure")
    void handlePayOSWebhook_WithProcessingFailure_ShouldReturnError() {
        // Given
        String webhookBody = "{\"code\":\"00\"}";
        when(payOsService.verifyWebhook(anyString(), anyString())).thenReturn(true);
        when(paymentService.handlePayOsWebhook(anyString())).thenReturn(false);

        // When
        ResponseEntity<?> response = paymentController.handlePayOSWebhook(webhookBody, "valid");

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("handlePayOSWebhook - should handle exception")
    void handlePayOSWebhook_WithException_ShouldReturnError() {
        // Given
        String webhookBody = "{\"code\":\"00\"}";
        when(payOsService.verifyWebhook(anyString(), anyString())).thenReturn(true);
        when(paymentService.handlePayOsWebhook(anyString()))
            .thenThrow(new RuntimeException("Processing error"));

        // When
        ResponseEntity<?> response = paymentController.handlePayOSWebhook(webhookBody, "valid");

        // Then
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadPayOSInvoice - should download invoice successfully")
    void downloadPayOSInvoice_WithValidData_ShouldReturnPDF() {
        // Given
        byte[] pdfData = "PDF_CONTENT".getBytes();
        when(paymentService.findByOrderCode(123456L)).thenReturn(Optional.of(payment));
        when(payOsService.getInvoiceInfo(anyLong())).thenReturn(createMockInvoiceResponse());
        when(payOsService.downloadInvoice(anyLong(), anyString())).thenReturn(pdfData);

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(123456L, authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadPayOSInvoice - should return 404 when payment not found")
    void downloadPayOSInvoice_WithPaymentNotFound_ShouldReturn404() {
        // Given
        when(paymentService.findByOrderCode(999999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(999999L, authentication);

        // Then
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadPayOSInvoice - should return 403 when unauthorized")
    void downloadPayOSInvoice_WithUnauthorized_ShouldReturn403() {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(differentCustomerId);
        Payment differentPayment = new Payment();
        differentPayment.setCustomer(differentCustomer);
        
        when(paymentService.findByOrderCode(123456L)).thenReturn(Optional.of(differentPayment));

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(123456L, authentication);

        // Then
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadPayOSInvoice - should return 400 when payment not completed")
    void downloadPayOSInvoice_WithNotCompleted_ShouldReturn400() {
        // Given
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentService.findByOrderCode(123456L)).thenReturn(Optional.of(payment));

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(123456L, authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("syncPayOSStatus - should sync successfully when PAID")
    void syncPayOSStatus_WithPaidStatus_ShouldUpdatePayment() {
        // Given
        PayOsService.PaymentInfoResponse payOSResponse = mock(PayOsService.PaymentInfoResponse.class);
        PayOsService.PaymentInfoResponse.PaymentInfoData paymentData = mock(PayOsService.PaymentInfoResponse.PaymentInfoData.class);
        
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(payOsService.getPaymentInfo(anyLong())).thenReturn(payOSResponse);
        when(payOSResponse.getCode()).thenReturn("00");
        when(payOSResponse.getData()).thenReturn(paymentData);
        when(paymentData.getStatus()).thenReturn("PAID");
        when(paymentData.getId()).thenReturn("link-id");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bookingService).completeBooking(anyInt());

        // When
        ResponseEntity<String> response = paymentController.syncPayOSStatus(1);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("COMPLETED"));
    }

    @Test
    @DisplayName("syncPayOSStatus - should handle payment not found")
    void syncPayOSStatus_WithPaymentNotFound_ShouldReturnMessage() {
        // Given
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> response = paymentController.syncPayOSStatus(999);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not found"));
    }

    @Test
    @DisplayName("syncPayOSStatus - should handle non-PayOS payment")
    void syncPayOSStatus_WithNonPayOSPayment_ShouldReturnMessage() {
        // Given
        payment.setPaymentMethod(PaymentMethod.CASH);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        ResponseEntity<String> response = paymentController.syncPayOSStatus(1);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not PayOS"));
    }

    @Test
    @DisplayName("syncPayOSStatus - should handle already completed")
    void syncPayOSStatus_WithAlreadyCompleted_ShouldReturnMessage() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        ResponseEntity<String> response = paymentController.syncPayOSStatus(1);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("already COMPLETED"));
    }

    @Test
    @DisplayName("debugBookingPayment - should return debug info")
    void debugBookingPayment_WithValidBooking_ShouldReturnInfo() {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(paymentService.findByBooking(booking)).thenReturn(Optional.of(payment));

        // When
        ResponseEntity<String> response = paymentController.debugBookingPayment(1);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Booking ID"));
    }

    @Test
    @DisplayName("debugBookingPayment - should handle booking not found")
    void debugBookingPayment_WithBookingNotFound_ShouldReturnMessage() {
        // Given
        when(bookingService.findBookingById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> response = paymentController.debugBookingPayment(999);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not found"));
    }

    @Test
    @DisplayName("getPayOSInvoices - should return invoices successfully")
    void getPayOSInvoices_WithValidPayment_ShouldReturnInvoices() throws Exception {
        // Given
        PayOsService.InvoiceInfoResponse invoiceResponse = mock(PayOsService.InvoiceInfoResponse.class);
        PayOsService.InvoiceInfoResponse.InvoiceData data = mock(PayOsService.InvoiceInfoResponse.InvoiceData.class);
        PayOsService.InvoiceInfoResponse.InvoiceData.Invoice invoice = mock(PayOsService.InvoiceInfoResponse.InvoiceData.Invoice.class);
        
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(payOsService.getInvoiceInfo(anyLong())).thenReturn(invoiceResponse);
        when(invoiceResponse.getCode()).thenReturn("00");
        when(invoiceResponse.getData()).thenReturn(data);
        when(data.getInvoices()).thenReturn(java.util.List.of(invoice));
        when(objectMapper.writeValueAsString(any())).thenReturn("[{\"invoiceId\":\"123\"}]");

        // When
        ResponseEntity<?> response = paymentController.getPayOSInvoices(1, authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("getPayOSInvoices - should return 400 when payment not found")
    void getPayOSInvoices_WithPaymentNotFound_ShouldReturn400() {
        // Given
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = paymentController.getPayOSInvoices(999, authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("getPayOSInvoices - should return 400 when unauthorized")
    void getPayOSInvoices_WithUnauthorized_ShouldReturn400() {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(differentCustomerId);
        Payment differentPayment = new Payment();
        differentPayment.setCustomer(differentCustomer);
        
        when(paymentService.findById(1)).thenReturn(Optional.of(differentPayment));

        // When
        ResponseEntity<?> response = paymentController.getPayOSInvoices(1, authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("getPayOSInvoices - should return 400 when not PayOS")
    void getPayOSInvoices_WithNonPayOS_ShouldReturn400() {
        // Given
        payment.setPaymentMethod(PaymentMethod.CASH);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        ResponseEntity<?> response = paymentController.getPayOSInvoices(1, authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("getPayOSInvoices - should handle failed response")
    void getPayOSInvoices_WithFailedResponse_ShouldReturnError() {
        // Given
        PayOsService.InvoiceInfoResponse invoiceResponse = mock(PayOsService.InvoiceInfoResponse.class);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(payOsService.getInvoiceInfo(anyLong())).thenReturn(invoiceResponse);
        when(invoiceResponse.getCode()).thenReturn("01");

        // When
        ResponseEntity<?> response = paymentController.getPayOSInvoices(1, authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("downloadPayOSInvoice by ID - should download successfully")
    void downloadPayOSInvoiceById_WithValidData_ShouldReturnPDF() {
        // Given
        byte[] pdfData = "PDF_CONTENT".getBytes();
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(payOsService.getInvoiceInfo(anyLong())).thenReturn(createMockInvoiceResponse());
        when(payOsService.downloadInvoice(anyLong(), anyString())).thenReturn(pdfData);
        payment.setStatus(PaymentStatus.COMPLETED);

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(1, "inv-123", authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadPayOSInvoice by ID - should return 400 when payment not found")
    void downloadPayOSInvoiceById_WithPaymentNotFound_ShouldReturn400() {
        // Given
        when(paymentService.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(999, "inv-123", authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadPayOSInvoice by ID - should return 400 when unauthorized")
    void downloadPayOSInvoiceById_WithUnauthorized_ShouldReturn400() {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(differentCustomerId);
        Payment differentPayment = new Payment();
        differentPayment.setCustomer(differentCustomer);
        
        when(paymentService.findById(1)).thenReturn(Optional.of(differentPayment));

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(1, "inv-123", authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadPayOSInvoice by ID - should return 400 when not PayOS")
    void downloadPayOSInvoiceById_WithNonPayOS_ShouldReturn400() {
        // Given
        payment.setPaymentMethod(PaymentMethod.CASH);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(1, "inv-123", authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadPayOSInvoice by ID - should return 400 when download fails")
    void downloadPayOSInvoiceById_WithDownloadFailure_ShouldReturn400() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(payOsService.getInvoiceInfo(anyLong())).thenReturn(createMockInvoiceResponse());
        when(payOsService.downloadInvoice(anyLong(), anyString())).thenReturn(null);

        // When
        ResponseEntity<?> response = paymentController.downloadPayOSInvoice(1, "inv-123", authentication);

        // Then
        assertEquals(400, response.getStatusCode().value());
    }

    // Helper method
    private PayOsService.InvoiceInfoResponse createMockInvoiceResponse() {
        PayOsService.InvoiceInfoResponse response = mock(PayOsService.InvoiceInfoResponse.class);
        PayOsService.InvoiceInfoResponse.InvoiceData data = mock(PayOsService.InvoiceInfoResponse.InvoiceData.class);
        PayOsService.InvoiceInfoResponse.InvoiceData.Invoice invoice = mock(PayOsService.InvoiceInfoResponse.InvoiceData.Invoice.class);
        
        when(response.getCode()).thenReturn("00");
        when(response.getData()).thenReturn(data);
        when(data.getInvoices()).thenReturn(java.util.List.of(invoice));
        when(invoice.getInvoiceId()).thenReturn("inv-123");
        
        return response;
    }

}

