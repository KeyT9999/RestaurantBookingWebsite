package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.PaymentMethod;

/**
 * Unit tests for Payment domain entity
 */
@DisplayName("Payment Domain Entity Tests")
public class PaymentTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setPaymentId(1);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.PAYOS);
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetAmount_successfully")
    void shouldSetAndGetAmount_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");

        // When
        payment.setAmount(amount);

        // Then
        assertEquals(amount, payment.getAmount());
    }

    @Test
    @DisplayName("shouldSetAndGetStatus_successfully")
    void shouldSetAndGetStatus_successfully() {
        // Given
        PaymentStatus status = PaymentStatus.COMPLETED;

        // When
        payment.setStatus(status);

        // Then
        assertEquals(status, payment.getStatus());
    }

    @Test
    @DisplayName("shouldSetAndGetPaymentMethod_successfully")
    void shouldSetAndGetPaymentMethod_successfully() {
        // Given
        PaymentMethod method = PaymentMethod.PAYOS;

        // When
        payment.setPaymentMethod(method);

        // Then
        assertEquals(method, payment.getPaymentMethod());
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreatePayment_withConstructor")
    void shouldCreatePayment_withConstructor() {
        // Given
        Customer customer = new Customer();
        Booking booking = new Booking();
        BigDecimal amount = new BigDecimal("1000000");
        PaymentMethod method = PaymentMethod.PAYOS;
        PaymentStatus status = PaymentStatus.PENDING;
        Voucher voucher = new Voucher();

        // When
        Payment newPayment = new Payment(customer, booking, amount, method, status, voucher);

        // Then
        assertEquals(customer, newPayment.getCustomer());
        assertEquals(booking, newPayment.getBooking());
        assertEquals(amount, newPayment.getAmount());
        assertEquals(method, newPayment.getPaymentMethod());
        assertEquals(status, newPayment.getStatus());
        assertEquals(voucher, newPayment.getVoucher());
        assertNotNull(newPayment.getPaidAt());
    }

    @Test
    @DisplayName("shouldUseDefaultStatus_whenStatusIsNull")
    void shouldUseDefaultStatus_whenStatusIsNull() {
        // Given
        Customer customer = new Customer();
        Booking booking = new Booking();
        BigDecimal amount = new BigDecimal("1000000");
        PaymentMethod method = PaymentMethod.PAYOS;

        // When
        Payment newPayment = new Payment(customer, booking, amount, method, null, null);

        // Then
        assertEquals(PaymentStatus.PENDING, newPayment.getStatus());
    }

    // ========== Relationship Tests ==========

    @Test
    @DisplayName("shouldSetAndGetCustomer_successfully")
    void shouldSetAndGetCustomer_successfully() {
        // Given
        Customer customer = new Customer();
        customer.setCustomerId(java.util.UUID.randomUUID());

        // When
        payment.setCustomer(customer);

        // Then
        assertEquals(customer, payment.getCustomer());
    }

    @Test
    @DisplayName("shouldSetAndGetBooking_successfully")
    void shouldSetAndGetBooking_successfully() {
        // Given
        Booking booking = new Booking();
        booking.setBookingId(1);

        // When
        payment.setBooking(booking);

        // Then
        assertEquals(booking, payment.getBooking());
    }

    @Test
    @DisplayName("shouldSetAndGetVoucher_successfully")
    void shouldSetAndGetVoucher_successfully() {
        // Given
        Voucher voucher = new Voucher();
        voucher.setVoucherId(1);

        // When
        payment.setVoucher(voucher);

        // Then
        assertEquals(voucher, payment.getVoucher());
    }

    @Test
    @DisplayName("shouldSetAndGetPaymentId_successfully")
    void shouldSetAndGetPaymentId_successfully() {
        // Given
        Integer paymentId = 123;

        // When
        payment.setPaymentId(paymentId);

        // Then
        assertEquals(paymentId, payment.getPaymentId());
    }

    // ========== PayOS Fields Tests ==========

    @Test
    @DisplayName("shouldSetAndGetPayOSFields_successfully")
    void shouldSetAndGetPayOSFields_successfully() {
        // Given
        String linkId = "link-123";
        String checkoutUrl = "https://pay.os.vn/checkout";
        String payosCode = "code-123";
        String payosDesc = "Payment description";
        Long orderCode = 123456L;
        String payUrl = "https://pay.os.vn/pay";

        // When
        payment.setPayosPaymentLinkId(linkId);
        payment.setPayosCheckoutUrl(checkoutUrl);
        payment.setPayosCode(payosCode);
        payment.setPayosDesc(payosDesc);
        payment.setOrderCode(orderCode);
        payment.setPayUrl(payUrl);

        // Then
        assertEquals(linkId, payment.getPayosPaymentLinkId());
        assertEquals(checkoutUrl, payment.getPayosCheckoutUrl());
        assertEquals(payosCode, payment.getPayosCode());
        assertEquals(payosDesc, payment.getPayosDesc());
        assertEquals(orderCode, payment.getOrderCode());
        assertEquals(payUrl, payment.getPayUrl());
    }

    @Test
    @DisplayName("shouldSetAndGetIPNFields_successfully")
    void shouldSetAndGetIPNFields_successfully() {
        // Given
        String ipnRaw = "{\"status\":\"success\"}";
        String redirectRaw = "{\"code\":\"00\"}";

        // When
        payment.setIpnRaw(ipnRaw);
        payment.setRedirectRaw(redirectRaw);

        // Then
        assertEquals(ipnRaw, payment.getIpnRaw());
        assertEquals(redirectRaw, payment.getRedirectRaw());
    }

    // ========== Refund Fields Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRefundFields_successfully")
    void shouldSetAndGetRefundFields_successfully() {
        // Given
        java.time.LocalDateTime refundedAt = java.time.LocalDateTime.now();
        BigDecimal refundAmount = new BigDecimal("500000");
        String refundReason = "Customer request";
        Integer refundRequestId = 1;

        // When
        payment.setRefundedAt(refundedAt);
        payment.setRefundAmount(refundAmount);
        payment.setRefundReason(refundReason);
        payment.setRefundRequestId(refundRequestId);

        // Then
        assertEquals(refundedAt, payment.getRefundedAt());
        assertEquals(refundAmount, payment.getRefundAmount());
        assertEquals(refundReason, payment.getRefundReason());
        assertEquals(refundRequestId, payment.getRefundRequestId());
    }

    // ========== Payment Type Tests ==========

    @Test
    @DisplayName("shouldSetAndGetPaymentType_successfully")
    void shouldSetAndGetPaymentType_successfully() {
        // Given
        com.example.booking.common.enums.PaymentType paymentType = com.example.booking.common.enums.PaymentType.DEPOSIT;

        // When
        payment.setPaymentType(paymentType);

        // Then
        assertEquals(paymentType, payment.getPaymentType());
    }

    // ========== Timestamp Tests ==========

    @Test
    @DisplayName("shouldSetAndGetPaidAt_successfully")
    void shouldSetAndGetPaidAt_successfully() {
        // Given
        java.time.LocalDateTime paidAt = java.time.LocalDateTime.now();

        // When
        payment.setPaidAt(paidAt);

        // Then
        assertEquals(paidAt, payment.getPaidAt());
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("shouldAcceptZeroAmount")
    void shouldAcceptZeroAmount() {
        // Given
        BigDecimal amount = BigDecimal.ZERO;

        // When
        payment.setAmount(amount);

        // Then
        assertEquals(amount, payment.getAmount());
    }

    @Test
    @DisplayName("shouldHandleNullVoucher")
    void shouldHandleNullVoucher() {
        // When
        payment.setVoucher(null);

        // Then
        assertNull(payment.getVoucher());
    }

    @Test
    @DisplayName("shouldHandleNullRefundAmount")
    void shouldHandleNullRefundAmount() {
        // When
        payment.setRefundAmount(null);

        // Then
        assertNull(payment.getRefundAmount());
    }

    @Test
    @DisplayName("shouldHandleNullPaymentMethod")
    void shouldHandleNullPaymentMethod() {
        // When
        payment.setPaymentMethod(null);

        // Then
        assertNull(payment.getPaymentMethod());
    }
}

