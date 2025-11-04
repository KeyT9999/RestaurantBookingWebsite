package com.example.booking.web.controller;

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.*;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.VoucherRedemptionRepository;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.EmailService;
import com.example.booking.service.PaymentService;
import com.example.booking.service.PayOsService;
import com.example.booking.service.RefundService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PaymentController WebMvc Integration Tests")
class PaymentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private PayOsService payOsService;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private RefundService refundService;

    @MockBean
    private VoucherRedemptionRepository voucherRedemptionRepository;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

    @MockBean
    private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

    private Booking booking;
    private Customer customer;
    private Payment payment;
    private User user;
    private RestaurantProfile restaurant;
    private RestaurantOwner owner;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("customer");
        user.setEmail("customer@test.com");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);
        customer.setFullName("Test Customer");

        User ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setEmail("owner@test.com");

        owner = new RestaurantOwner();
        owner.setOwnerId(UUID.randomUUID());
        owner.setUser(ownerUser);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setDepositAmount(new BigDecimal("500000"));
        booking.setBookingTime(LocalDateTime.now().plusHours(2));

        payment = new Payment();
        payment.setPaymentId(1);
        payment.setBooking(booking);
        payment.setCustomer(customer);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.PAYOS);
        payment.setPaymentType(PaymentType.DEPOSIT);
        payment.setOrderCode(123456L);

        when(customerService.findByUsername(anyString())).thenReturn(Optional.of(customer));
        when(voucherRedemptionRepository.findByBooking_BookingId(anyInt())).thenReturn(Collections.emptyList());
    }

    // ========== GET /payment/{bookingId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/1 - should show payment form successfully")
    void testShowPaymentForm_Success() throws Exception {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(paymentService.findByBooking(booking)).thenReturn(Optional.empty());
        when(bookingService.calculateTotalAmount(booking)).thenReturn(new BigDecimal("1000000"));

        // When & Then
        mockMvc.perform(get("/payment/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("payment/form"))
            .andExpect(model().attributeExists("booking"))
            .andExpect(model().attributeExists("fullTotalAmount"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/1 - should redirect if already paid")
    void testShowPaymentForm_AlreadyPaid() throws Exception {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(paymentService.findByBooking(booking)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(get("/payment/1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/booking/my?success=already_paid"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/999 - booking not found should return 404")
    void testShowPaymentForm_NotFound() throws Exception {
        // Given
        when(bookingService.findBookingById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/payment/999"))
            .andExpect(status().isOk())
            .andExpect(view().name("error/404"));
    }

    // ========== POST /payment/process ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/process - CASH full payment should succeed")
    void testProcessPayment_Cash_Success() throws Exception {
        // Given
        when(paymentService.createPayment(eq(1), any(), eq(PaymentMethod.CASH), 
            eq(PaymentType.FULL_PAYMENT), isNull())).thenReturn(payment);
        when(bookingService.calculateTotalAmount(any())).thenReturn(new BigDecimal("1000000"));

        // When & Then
        mockMvc.perform(post("/payment/process")
                .param("bookingId", "1")
                    .param("paymentMethod", "CASH")
                .param("paymentType", "FULL_PAYMENT")
                .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/booking/my"));

        verify(paymentService).processCashPayment(anyInt());
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/process - CASH deposit should be rejected")
    void testProcessPayment_CashDeposit_Rejected() throws Exception {
        // Given
        when(paymentService.createPayment(eq(1), any(), eq(PaymentMethod.CASH), 
            eq(PaymentType.DEPOSIT), isNull())).thenReturn(payment);

        // When & Then
        mockMvc.perform(post("/payment/process")
                .param("bookingId", "1")
                .param("paymentMethod", "CASH")
                .param("paymentType", "DEPOSIT")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/payment/1"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/process - PAYOS should redirect to checkout")
    void testProcessPayment_PayOS_Success() throws Exception {
        // Given
        when(paymentService.createPayment(eq(1), any(), eq(PaymentMethod.PAYOS), 
            eq(PaymentType.DEPOSIT), isNull())).thenReturn(payment);
        
        PayOsService.CreateLinkResponse mockResponse = mock(PayOsService.CreateLinkResponse.class);
        PayOsService.CreateLinkResponse.Data mockData = mock(PayOsService.CreateLinkResponse.Data.class);
        when(mockResponse.getData()).thenReturn(mockData);
        when(mockData.getCheckoutUrl()).thenReturn("https://pay.payos.vn/checkout/123456");
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/payment/process")
                .param("bookingId", "1")
                    .param("paymentMethod", "PAYOS")
                .param("paymentType", "DEPOSIT")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("https://pay.payos.vn/checkout/123456"));
    }

    // ========== GET /payment/result/{paymentId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/result/1 - should show payment result")
    void testShowPaymentResult_Success() throws Exception {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        when(bookingService.calculateTotalAmount(booking)).thenReturn(new BigDecimal("1000000"));

        // When & Then
        mockMvc.perform(get("/payment/result/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("payment/result"))
            .andExpect(model().attributeExists("payment"))
            .andExpect(model().attributeExists("totalAmount"));
    }

    // ========== GET /payment/payos/return ==========

    @Test
    @DisplayName("GET /payment/payos/return - with orderCode should process return")
    void testHandlePayOsReturn_WithOrderCode() throws Exception {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findByOrderCode(123456L)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(get("/payment/payos/return")
                .param("orderCode", "123456")
                .param("status", "PAID"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/payment/result/1"));
    }

    @Test
    @DisplayName("GET /payment/payos/return - null orderCode should redirect with error")
    void testHandlePayOsReturn_NullOrderCode() throws Exception {
        mockMvc.perform(get("/payment/payos/return"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/booking/my"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    // ========== GET /payment/payos/cancel ==========

    @Test
    @DisplayName("GET /payment/payos/cancel - should cancel payment")
    void testHandlePayOsCancel_Success() throws Exception {
        // Given
        when(paymentService.findByOrderCode(123456L)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(get("/payment/payos/cancel")
                .param("orderCode", "123456"))
                    .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/payment/1"));

        verify(paymentRepository).save(payment);
    }

    // ========== GET /payment/api/status/{paymentId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/api/status/1 - should return payment status")
    void testGetPaymentStatus_Success() throws Exception {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(get("/payment/api/status/1"))
            .andExpect(status().isOk());
    }

    // ========== POST /payment/api/cancel/{paymentId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/api/cancel/1 - should cancel payment successfully")
    void testCancelPayment_Success() throws Exception {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(post("/payment/api/cancel/1").with(csrf()))
            .andExpect(status().isOk());

        verify(paymentService).cancelPayment(1);
    }

    // ========== GET /payment/debug/booking/{bookingId} ==========

    @Test
    @DisplayName("GET /payment/debug/booking/1 - should return debug info")
    void testDebugBookingPayment() throws Exception {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(paymentService.findByBooking(booking)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(get("/payment/debug/booking/1"))
            .andExpect(status().isOk());
    }

    // ========== POST /payment/api/payos/create/{paymentId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/api/payos/create/1 - should create PayOS link")
    void testCreatePayOSLink_Success() throws Exception {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        PayOsService.CreateLinkResponse mockResponse = mock(PayOsService.CreateLinkResponse.class);
        PayOsService.CreateLinkResponse.Data mockData = mock(PayOsService.CreateLinkResponse.Data.class);
        when(mockResponse.getData()).thenReturn(mockData);
        when(mockData.getCheckoutUrl()).thenReturn("https://payos.vn/checkout");
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/payment/api/payos/create/1").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ========== POST /payment/api/cash/confirm/{paymentId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/api/cash/confirm/1 - should confirm cash payment")
    void testConfirmCashPayment_Success() throws Exception {
        // Given
        payment.setPaymentMethod(PaymentMethod.CASH);
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(post("/payment/api/cash/confirm/1").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(paymentService).processCashPayment(1);
    }

    // ========== GET /payment/api/payos/status/{paymentId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/api/payos/status/1 - should check PayOS status")
    void testCheckPayOSStatus_Success() throws Exception {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        PayOsService.PaymentInfoResponse mockResponse = mock(PayOsService.PaymentInfoResponse.class);
        when(mockResponse.getCode()).thenReturn("00");
        when(payOsService.getPaymentInfo(anyLong())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/payment/api/payos/status/1"))
            .andExpect(status().isOk());
        }

    // ========== POST /payment/api/payos/webhook ==========

    @Test
    @DisplayName("POST /payment/api/payos/webhook - should handle webhook")
    void testHandlePayOSWebhook_Success() throws Exception {
        // Given
        String webhookBody = "{\"orderCode\":123456}";
        when(payOsService.verifyWebhook(anyString(), anyString())).thenReturn(true);
        when(paymentService.handlePayOsWebhook(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/payment/api/payos/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookBody)
                .header("x-payos-signature", "test-signature")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ========== POST /payment/api/payos/confirm-webhook ==========

    @Test
    @DisplayName("POST /payment/api/payos/confirm-webhook - should confirm webhook")
    void testConfirmWebhook_Success() throws Exception {
        // Given
        PayOsService.WebhookConfirmResponse mockResponse = mock(PayOsService.WebhookConfirmResponse.class);
        when(mockResponse.getCode()).thenReturn("00");
        when(payOsService.confirmWebhook(anyString())).thenReturn(mockResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When & Then
        mockMvc.perform(post("/payment/api/payos/confirm-webhook")
                .param("webhookUrl", "https://example.com/webhook")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ========== GET/POST /payment/debug/sync-payos/{paymentId} ==========

    @Test
    @DisplayName("GET /payment/debug/sync-payos/1 - should sync PayOS status")
    void testSyncPayOSStatus_Success() throws Exception {
        // Given
        when(paymentService.findById(1)).thenReturn(Optional.of(payment));
        PayOsService.PaymentInfoResponse mockResponse = mock(PayOsService.PaymentInfoResponse.class);
        PayOsService.PaymentInfoResponse.PaymentInfoData mockData = mock(PayOsService.PaymentInfoResponse.PaymentInfoData.class);
        when(mockResponse.getCode()).thenReturn("00");
        when(mockResponse.getData()).thenReturn(mockData);
        when(mockData.getStatus()).thenReturn("PAID");
        when(mockData.getAmountPaid()).thenReturn(500000L);
        when(mockData.getAmountRemaining()).thenReturn(0L);
        when(mockData.getId()).thenReturn("payos-id");
        when(payOsService.getPaymentInfo(anyLong())).thenReturn(mockResponse);
        when(paymentRepository.save(any())).thenReturn(payment);

        // When & Then
        mockMvc.perform(get("/payment/debug/sync-payos/1"))
            .andExpect(status().isOk());
    }

    // ========== GET /payment/api/payos/invoice/{orderCode}/download ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/api/payos/invoice/123456/download - should download invoice")
    void testDownloadPayOSInvoice_Success() throws Exception {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.findByOrderCode(123456L)).thenReturn(Optional.of(payment));
        PayOsService.InvoiceInfoResponse mockInvoiceInfo = mock(PayOsService.InvoiceInfoResponse.class);
        PayOsService.InvoiceInfoResponse.InvoiceData mockInvoiceData = mock(PayOsService.InvoiceInfoResponse.InvoiceData.class);
        PayOsService.InvoiceInfoResponse.InvoiceData.Invoice mockInvoice = mock(PayOsService.InvoiceInfoResponse.InvoiceData.Invoice.class);
        when(mockInvoiceInfo.getData()).thenReturn(mockInvoiceData);
        when(mockInvoiceData.getInvoices()).thenReturn(Arrays.asList(mockInvoice));
        when(mockInvoice.getInvoiceId()).thenReturn("invoice-123");
        when(payOsService.getInvoiceInfo(anyLong())).thenReturn(mockInvoiceInfo);
        when(payOsService.downloadInvoice(anyLong(), anyString())).thenReturn("pdf content".getBytes());

        // When & Then
        mockMvc.perform(get("/payment/api/payos/invoice/123456/download"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/pdf"));
    }

    // ========== POST /payment/1/refund ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/1/refund - should process full refund")
    void testProcessFullRefund_Success() throws Exception {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(refundService.processFullRefund(eq(1), anyString())).thenReturn(payment);

        // When & Then
        mockMvc.perform(post("/payment/1/refund")
                .param("reason", "Customer request")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /payment/1/refund - should return 401 when unauthenticated")
    void testProcessFullRefund_Unauthorized() throws Exception {
        // When & Then - This should trigger the new HashMap(){} for 401 error
        mockMvc.perform(post("/payment/1/refund")
                .param("reason", "Customer request")
                .with(csrf()))
            .andExpect(status().is(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/1/refund - should return 400 on IllegalArgumentException")
    void testProcessFullRefund_IllegalArgument() throws Exception {
        // Given
        when(refundService.processFullRefund(eq(1), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid refund"));

        // When & Then - This should trigger the new HashMap(){} for 400 error
        mockMvc.perform(post("/payment/1/refund")
                .param("reason", "Invalid")
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/1/refund - should return 500 on general exception")
    void testProcessFullRefund_Exception() throws Exception {
        // Given
        when(refundService.processFullRefund(eq(1), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then - This should trigger the new HashMap(){} for 500 error
        mockMvc.perform(post("/payment/1/refund")
                .param("reason", "Error")
                .with(csrf()))
            .andExpect(status().is(500))
            .andExpect(jsonPath("$.error").exists());
    }

    // ========== POST /payment/1/refund/partial ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/1/refund/partial - should process partial refund")
    void testProcessPartialRefund_Success() throws Exception {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(refundService.processPartialRefund(eq(1), any(), anyString())).thenReturn(payment);

        // When & Then
        mockMvc.perform(post("/payment/1/refund/partial")
                .param("amount", "100000")
                .param("reason", "Partial refund")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /payment/1/refund/partial - should return 401 when unauthenticated")
    void testProcessPartialRefund_Unauthorized() throws Exception {
        // When & Then - This should trigger the new HashMap(){} for 401 error
        mockMvc.perform(post("/payment/1/refund/partial")
                .param("amount", "100000")
                .param("reason", "Partial refund")
                .with(csrf()))
            .andExpect(status().is(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/1/refund/partial - should return 400 on invalid amount")
    void testProcessPartialRefund_InvalidAmount() throws Exception {
        // When & Then - This should trigger the new HashMap(){} for 400 error (invalid amount format)
        mockMvc.perform(post("/payment/1/refund/partial")
                .param("amount", "invalid")
                .param("reason", "Partial refund")
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid amount format"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/1/refund/partial - should return 400 on IllegalArgumentException")
    void testProcessPartialRefund_IllegalArgument() throws Exception {
        // Given
        when(refundService.processPartialRefund(eq(1), any(), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid refund"));

        // When & Then - This should trigger the new HashMap(){} for 400 error
        mockMvc.perform(post("/payment/1/refund/partial")
                .param("amount", "100000")
                .param("reason", "Invalid")
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /payment/1/refund/partial - should return 500 on general exception")
    void testProcessPartialRefund_Exception() throws Exception {
        // Given
        when(refundService.processPartialRefund(eq(1), any(), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then - This should trigger the new HashMap(){} for 500 error
        mockMvc.perform(post("/payment/1/refund/partial")
                .param("amount", "100000")
                .param("reason", "Error")
                .with(csrf()))
            .andExpect(status().is(500))
            .andExpect(jsonPath("$.error").exists());
    }

    // ========== GET /payment/1/refund/check ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/1/refund/check - should check refund eligibility")
    void testCheckRefundEligibility_Success() throws Exception {
        // Given
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(refundService.canRefund(1)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/payment/1/refund/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.canRefund").value(true));
    }

    @Test
    @DisplayName("GET /payment/1/refund/check - should return 401 when unauthenticated")
    void testCheckRefundEligibility_Unauthorized() throws Exception {
        // When & Then - This should trigger the new HashMap(){} for 401 error
        mockMvc.perform(get("/payment/1/refund/check"))
            .andExpect(status().is(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/1/refund/check - should return 500 on exception")
    void testCheckRefundEligibility_Exception() throws Exception {
        // Given
        when(paymentRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        // When & Then - This should trigger the new HashMap(){} for 500 error
        mockMvc.perform(get("/payment/1/refund/check"))
            .andExpect(status().is(500))
            .andExpect(jsonPath("$.error").exists());
    }

    // ========== GET /payment/refundable ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/refundable - should get refundable payments")
    void testGetRefundablePayments_Success() throws Exception {
        // Given
        when(refundService.getRefundablePayments()).thenReturn(Collections.singletonList(payment));

        // When & Then
        mockMvc.perform(get("/payment/refundable"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /payment/refundable - should return 401 when unauthenticated")
    void testGetRefundablePayments_Unauthorized() throws Exception {
        // When & Then - This should trigger the new HashMap(){} for 401 error
        mockMvc.perform(get("/payment/refundable"))
            .andExpect(status().is(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/refundable - should return 500 on exception")
    void testGetRefundablePayments_Exception() throws Exception {
        // Given
        when(refundService.getRefundablePayments()).thenThrow(new RuntimeException("Database error"));

        // When & Then - This should trigger the new HashMap(){} for 500 error
        mockMvc.perform(get("/payment/refundable"))
            .andExpect(status().is(500))
            .andExpect(jsonPath("$.error").exists());
    }

    // ========== GET /payment/1/refund/info ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/1/refund/info - should get refund info")
    void testGetRefundInfo_Success() throws Exception {
        // Given
        when(refundService.getRefundInfo(1)).thenReturn(Optional.of(payment));

        // When & Then
        mockMvc.perform(get("/payment/1/refund/info"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /payment/1/refund/info - should return 401 when unauthenticated")
    void testGetRefundInfo_Unauthorized() throws Exception {
        // When & Then - This should trigger the new HashMap(){} for 401 error
        mockMvc.perform(get("/payment/1/refund/info"))
            .andExpect(status().is(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/1/refund/info - should return 500 on exception")
    void testGetRefundInfo_Exception() throws Exception {
        // Given
        when(refundService.getRefundInfo(1)).thenThrow(new RuntimeException("Database error"));

        // When & Then - This should trigger the new HashMap(){} for 500 error
        mockMvc.perform(get("/payment/1/refund/info"))
            .andExpect(status().is(500))
            .andExpect(jsonPath("$.error").exists());
    }

    // ========== GET /payment/api/history ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /payment/api/history - should get payment history")
    void testGetPaymentHistory_Success() throws Exception {
        // Given
        when(customerService.findById(any())).thenReturn(Optional.of(customer));
        when(paymentService.findByCustomer(any(Customer.class))).thenReturn(Collections.singletonList(payment));

        // When & Then
        mockMvc.perform(get("/payment/api/history"))
            .andExpect(status().isOk());
    }

    // ========== POST /payment/api/payos/test-create ==========

    @Test
    @DisplayName("POST /payment/api/payos/test-create - should test create PayOS link")
    void testTestCreatePayOSLink() throws Exception {
        // Given
        PayOsService.CreateLinkResponse mockResponse = mock(PayOsService.CreateLinkResponse.class);
        PayOsService.CreateLinkResponse.Data mockData = mock(PayOsService.CreateLinkResponse.Data.class);
        when(mockResponse.getData()).thenReturn(mockData);
        when(mockData.getCheckoutUrl()).thenReturn("https://payos.vn/test");
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/payment/api/payos/test-create")
                .param("orderCode", "999999")
                .param("amount", "100000")
                .param("description", "Test")
                .with(csrf()))
            .andExpect(status().isOk());
    }
}
