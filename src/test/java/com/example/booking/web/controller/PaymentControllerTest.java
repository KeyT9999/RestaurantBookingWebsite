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

        when(authentication.getPrincipal()).thenReturn(user);
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
        assertNotNull(view);
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

