package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
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
import com.example.booking.domain.UserRole;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.VoucherRedemptionRepository;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.EmailService;
import com.example.booking.service.PaymentService;
import com.example.booking.service.PayOsService;
import com.example.booking.service.RefundService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private ObjectMapper objectMapper;

    @Mock
    private VoucherRedemptionRepository voucherRedemptionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RefundService refundService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PaymentController paymentController;

    private User testUser;
    private Customer testCustomer;
    private Booking testBooking;
    private Payment testPayment;

    @BeforeEach
    public void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("customer@example.com");
        testUser.setEmail("customer@example.com");
        testUser.setFullName("Test Customer");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setEmailVerified(true);

        // Create test customer
        testCustomer = new Customer();
        testCustomer.setCustomerId(testUser.getId());
        testCustomer.setFullName("Test Customer");
        testCustomer.setUser(testUser);

        // Create test booking
        testBooking = new Booking();
        testBooking.setBookingId(1);
        testBooking.setCustomer(testCustomer);
        testBooking.setBookingTime(LocalDateTime.now().plusDays(1));
        testBooking.setNumberOfGuests(4);
        testBooking.setStatus(com.example.booking.common.enums.BookingStatus.PENDING);

        // Create test payment
        testPayment = new Payment();
        testPayment.setPaymentId(1);
        testPayment.setBooking(testBooking);
        testPayment.setCustomer(testCustomer);
        testPayment.setAmount(BigDecimal.valueOf(100000));
        testPayment.setPaymentMethod(PaymentMethod.PAYOS);
        testPayment.setPaymentType(PaymentType.DEPOSIT);
        testPayment.setStatus(PaymentStatus.PENDING);
        testPayment.setOrderCode(123456789L);
    }

    // ========== showPaymentForm() Tests ==========

    @Test
    @DisplayName("showPaymentForm - should return payment form view")
    void showPaymentForm_WithValidBookingId_ShouldReturnForm() {
        // Given
        Integer bookingId = 1;
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookingService.findBookingById(bookingId))
                .thenReturn(Optional.of(testBooking));
        when(paymentService.findByBooking(testBooking)).thenReturn(Optional.empty());
        when(bookingService.calculateTotalAmount(testBooking))
                .thenReturn(BigDecimal.valueOf(500000));
        when(voucherRedemptionRepository.findByBooking_BookingId(bookingId))
                .thenReturn(new ArrayList<>());

        // When
        String result = paymentController.showPaymentForm(bookingId, model, authentication);

        // Then
        assertEquals("payment/form", result);
        verify(model).addAttribute(eq("booking"), any(Booking.class));
    }

    @Test
    @DisplayName("showPaymentForm - should return 404 when booking not found")
    void showPaymentForm_WithInvalidBookingId_ShouldReturn404() {
        // Given
        Integer bookingId = 999;
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookingService.findBookingById(bookingId)).thenReturn(Optional.empty());

        // When
        String result = paymentController.showPaymentForm(bookingId, model, authentication);

        // Then
        assertEquals("error/404", result);
    }

    @Test
    @DisplayName("showPaymentForm - should redirect when already paid")
    void showPaymentForm_WithCompletedPayment_ShouldRedirect() {
        // Given
        Integer bookingId = 1;
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookingService.findBookingById(bookingId))
                .thenReturn(Optional.of(testBooking));
        when(paymentService.findByBooking(testBooking))
                .thenReturn(Optional.of(testPayment));

        // When
        String result = paymentController.showPaymentForm(bookingId, model, authentication);

        // Then
        assertTrue(result.contains("redirect"));
    }

    @Test
    @DisplayName("showPaymentForm - should return 403 when customer doesn't own booking")
    void showPaymentForm_WithUnauthorizedCustomer_ShouldReturn403() {
        // Given
        Integer bookingId = 1;
        UUID differentCustomerId = UUID.randomUUID();
        testBooking.getCustomer().setCustomerId(differentCustomerId);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookingService.findBookingById(bookingId))
                .thenReturn(Optional.of(testBooking));

        // When
        String result = paymentController.showPaymentForm(bookingId, model, authentication);

        // Then
        assertEquals("error/403", result);
    }

    // ========== handlePayOsReturn() Tests ==========

    @Test
    @DisplayName("handlePayOsReturn - should redirect to result when payment completed")
    void handlePayOsReturn_WithCompletedPayment_ShouldRedirectToResult() {
        // Given
        String orderCode = "123456789";
        String status = "PAID";
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findByOrderCode(123456789L))
                .thenReturn(Optional.of(testPayment));

        // When
        String result = paymentController.handlePayOsReturn(orderCode, status, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/payment/result/"));
    }

    @Test
    @DisplayName("handlePayOsReturn - should handle missing orderCode")
    void handlePayOsReturn_WithNullOrderCode_ShouldRedirect() {
        // Given
        String orderCode = null;

        // When
        String result = paymentController.handlePayOsReturn(orderCode, null, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/booking/my"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("handlePayOsReturn - should handle payment not found")
    void handlePayOsReturn_WithInvalidOrderCode_ShouldRedirect() {
        // Given
        String orderCode = "999999999";
        when(paymentService.findByOrderCode(999999999L)).thenReturn(Optional.empty());

        // When
        String result = paymentController.handlePayOsReturn(orderCode, null, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/booking/my"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // ========== handlePayOsCancel() Tests ==========

    @Test
    @DisplayName("handlePayOsCancel - should redirect when cancelled")
    void handlePayOsCancel_WithValidOrderCode_ShouldRedirect() {
        // Given
        String orderCode = "123456789";
        when(paymentService.findByOrderCode(123456789L))
                .thenReturn(Optional.of(testPayment));

        // When
        String result = paymentController.handlePayOsCancel(orderCode, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
    }

    @Test
    @DisplayName("handlePayOsCancel - should handle missing orderCode")
    void handlePayOsCancel_WithNullOrderCode_ShouldRedirect() {
        // Given
        String orderCode = null;

        // When
        String result = paymentController.handlePayOsCancel(orderCode, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/booking/my"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // ========== processPayment() Tests ==========

    @Test
    @DisplayName("processPayment - should process PayOS payment successfully")
    void processPayment_WithPayOSMethod_ShouldProcess() {
        // Given
        Integer bookingId = 1;
        PaymentMethod paymentMethod = PaymentMethod.PAYOS;
        PaymentType paymentType = PaymentType.DEPOSIT;
        String voucherCode = null;
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(paymentService.createPayment(eq(bookingId), eq(testUser.getId()), 
                eq(paymentMethod), eq(paymentType), eq(voucherCode)))
                .thenReturn(testPayment);
        PayOsService.CreateLinkResponse linkResponse = new PayOsService.CreateLinkResponse();
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(linkResponse);

        // When
        String result = paymentController.processPayment(bookingId, paymentMethod, 
                paymentType, voucherCode, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
    }

    @Test
    @DisplayName("processPayment - should process cash payment successfully")
    void processPayment_WithCashMethod_ShouldProcess() {
        // Given
        Integer bookingId = 1;
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        PaymentType paymentType = PaymentType.FULL_PAYMENT;
        String voucherCode = null;
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(paymentService.createPayment(eq(bookingId), eq(testUser.getId()), 
                eq(paymentMethod), eq(paymentType), eq(voucherCode)))
                .thenReturn(testPayment);

        // When
        String result = paymentController.processPayment(bookingId, paymentMethod, 
                paymentType, voucherCode, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
    }

    // ========== showPaymentResult() Tests ==========

    @Test
    @DisplayName("showPaymentResult - should return result view")
    void showPaymentResult_WithValidPaymentId_ShouldReturnView() {
        // Given
        Integer paymentId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(customerService.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testCustomer));
        when(paymentService.findById(paymentId))
                .thenReturn(Optional.of(testPayment));
        when(bookingService.calculateTotalAmount(testBooking))
                .thenReturn(BigDecimal.valueOf(500000));

        // When
        String result = paymentController.showPaymentResult(paymentId, model, authentication);

        // Then
        assertEquals("payment/result", result);
        verify(model).addAttribute(eq("payment"), any(Payment.class));
    }

    @Test
    @DisplayName("showPaymentResult - should return 404 when payment not found")
    void showPaymentResult_WithInvalidPaymentId_ShouldReturn404() {
        // Given
        Integer paymentId = 999;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(customerService.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testCustomer));
        when(paymentService.findById(paymentId)).thenReturn(Optional.empty());

        // When
        String result = paymentController.showPaymentResult(paymentId, model, authentication);

        // Then
        assertEquals("error/404", result);
    }

    // ========== getPaymentStatus() API Tests ==========

    @Test
    @DisplayName("getPaymentStatus - should return payment status")
    void getPaymentStatus_WithValidPaymentId_ShouldReturnStatus() {
        // Given
        Integer paymentId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(customerService.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testCustomer));
        when(paymentService.findById(paymentId))
                .thenReturn(Optional.of(testPayment));

        // When
        ResponseEntity<Payment> result = paymentController.getPaymentStatus(paymentId, authentication);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    @DisplayName("getPaymentStatus - should return 404 when payment not found")
    void getPaymentStatus_WithInvalidPaymentId_ShouldReturn404() {
        // Given
        Integer paymentId = 999;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(customerService.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testCustomer));
        when(paymentService.findById(paymentId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Payment> result = paymentController.getPaymentStatus(paymentId, authentication);

        // Then
        assertNotNull(result);
        assertTrue(result.getStatusCodeValue() >= 400);
    }

    // ========== getPaymentHistory() API Tests ==========

    @Test
    @DisplayName("getPaymentHistory - should return payment history")
    void getPaymentHistory_WithValidCustomer_ShouldReturnHistory() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(customerService.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testCustomer));
        when(customerService.findById(testUser.getId()))
                .thenReturn(Optional.of(testCustomer));
        List<Payment> payments = new ArrayList<>();
        payments.add(testPayment);
        when(paymentService.findByCustomer(testCustomer)).thenReturn(payments);

        // When
        ResponseEntity<List<Payment>> result = paymentController.getPaymentHistory(authentication);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
    }

    // ========== cancelPayment() API Tests ==========

    @Test
    @DisplayName("cancelPayment - should cancel payment successfully")
    void cancelPayment_WithValidPaymentId_ShouldCancel() {
        // Given
        Integer paymentId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(customerService.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testCustomer));
        when(paymentService.findById(paymentId))
                .thenReturn(Optional.of(testPayment));
        doNothing().when(paymentService).cancelPayment(paymentId);

        // When
        ResponseEntity<String> result = paymentController.cancelPayment(paymentId, authentication);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
    }

    // ========== processFullRefund() Tests ==========

    @Test
    @DisplayName("processFullRefund - should process full refund successfully")
    void processFullRefund_WithValidPaymentId_ShouldProcessRefund() {
        // Given
        Integer paymentId = 1;
        String reason = "Customer requested";
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findById(paymentId))
                .thenReturn(Optional.of(testPayment));
        when(refundService.processFullRefund(paymentId, reason))
                .thenReturn(testPayment);

        // When
        ResponseEntity<?> result = paymentController.processFullRefund(paymentId, reason, authentication);

        // Then
        assertNotNull(result);
    }

    // ========== processPartialRefund() Tests ==========

    @Test
    @DisplayName("processPartialRefund - should process partial refund")
    void processPartialRefund_WithValidAmount_ShouldProcessRefund() {
        // Given
        Integer paymentId = 1;
        String amount = "50000";
        String reason = "Partial refund requested";
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findById(paymentId))
                .thenReturn(Optional.of(testPayment));
        when(refundService.processPartialRefund(paymentId, BigDecimal.valueOf(50000), reason))
                .thenReturn(testPayment);

        // When
        ResponseEntity<?> result = paymentController.processPartialRefund(
                paymentId, amount, reason, authentication);

        // Then
        assertNotNull(result);
    }

    // ========== checkRefundEligibility() Tests ==========

    @Test
    @DisplayName("checkRefundEligibility - should return refund eligibility")
    void checkRefundEligibility_WithValidPaymentId_ShouldReturnEligibility() {
        // Given
        Integer paymentId = 1;
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(testPayment));
        when(refundService.canRefund(paymentId))
                .thenReturn(true);

        // When
        ResponseEntity<?> result = paymentController.checkRefundEligibility(paymentId, authentication);

        // Then
        assertNotNull(result);
    }

    // ========== getRefundablePayments() Tests ==========

    @Test
    @DisplayName("getRefundablePayments - should return refundable payments")
    void getRefundablePayments_ShouldReturnPayments() {
        // Given
        List<Payment> refundablePayments = new ArrayList<>();
        refundablePayments.add(testPayment);
        when(refundService.getRefundablePayments())
                .thenReturn(refundablePayments);

        // When
        ResponseEntity<?> result = paymentController.getRefundablePayments(authentication);

        // Then
        assertNotNull(result);
    }

    // ========== getRefundInfo() Tests ==========

    @Test
    @DisplayName("getRefundInfo - should return refund info")
    void getRefundInfo_WithValidPaymentId_ShouldReturnInfo() {
        // Given
        Integer paymentId = 1;
        when(refundService.getRefundInfo(paymentId))
                .thenReturn(Optional.of(testPayment));

        // When
        ResponseEntity<?> result = paymentController.getRefundInfo(paymentId, authentication);

        // Then
        assertNotNull(result);
    }
}

