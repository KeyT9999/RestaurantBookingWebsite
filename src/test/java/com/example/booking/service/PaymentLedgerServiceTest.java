package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.Payment;
import com.example.booking.scheduler.PayOSReconciliationScheduler.ReconciliationResult;

/**
 * Unit tests for PaymentLedgerService
 * Note: This service has placeholder methods that need implementation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentLedgerService Tests")
public class PaymentLedgerServiceTest {

    @InjectMocks
    private PaymentLedgerService paymentLedgerService;

    private Integer paymentId;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentId = 1;
        payment = new Payment();
        payment.setPaymentId(paymentId);
    }

    // ========== createPaymentLedgerEntry() Tests ==========

    @Test
    @DisplayName("shouldCreatePaymentLedgerEntry_successfully")
    void shouldCreatePaymentLedgerEntry_successfully() {
        // When
        Long result = paymentLedgerService.createPaymentLedgerEntry(
            paymentId, "PAYMENT", new BigDecimal("1000000"), "SUCCESS", "Test payment");

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("shouldCreatePaymentLedgerEntry_withDifferentTypes")
    void shouldCreatePaymentLedgerEntry_withDifferentTypes() {
        // When
        Long result1 = paymentLedgerService.createPaymentLedgerEntry(
            paymentId, "REFUND", new BigDecimal("500000"), "SUCCESS", "Test refund");
        Long result2 = paymentLedgerService.createPaymentLedgerEntry(
            paymentId, "CHARGEBACK", new BigDecimal("100000"), "PENDING", "Test chargeback");

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
    }

    // ========== createReconciliationLog() Tests ==========

    @Test
    @DisplayName("shouldCreateReconciliationLog_successfully")
    void shouldCreateReconciliationLog_successfully() {
        // When
        Long result = paymentLedgerService.createReconciliationLog(
            LocalDate.now(), "MOMO", 10);

        // Then
        assertNotNull(result);
    }

    // ========== createReconciliationDetail() Tests ==========

    @Test
    @DisplayName("shouldCreateReconciliationDetail_successfully")
    void shouldCreateReconciliationDetail_successfully() {
        // Given
        ReconciliationResult result = new ReconciliationResult(
            com.example.booking.scheduler.PayOSReconciliationScheduler.ReconciliationStatus.MATCHED,
            null,
            "Matched successfully"
        );

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            paymentLedgerService.createReconciliationDetail(1L, payment, result);
        });
    }

    // ========== updateReconciliationLog() Tests ==========

    @Test
    @DisplayName("shouldUpdateReconciliationLog_successfully")
    void shouldUpdateReconciliationLog_successfully() {
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            paymentLedgerService.updateReconciliationLog(1L, 5, 2, 1, "COMPLETED");
        });
    }

    // ========== getReconciliationHistory() Tests ==========

    @Test
    @DisplayName("shouldGetReconciliationHistory_successfully")
    void shouldGetReconciliationHistory_successfully() {
        // When
        var result = paymentLedgerService.getReconciliationHistory(
            LocalDate.now().minusDays(7), LocalDate.now());

        // Then
        assertNotNull(result);
    }

    // ========== getPaymentLedger() Tests ==========

    @Test
    @DisplayName("shouldGetPaymentLedger_successfully")
    void shouldGetPaymentLedger_successfully() {
        // When
        var result = paymentLedgerService.getPaymentLedger(paymentId);

        // Then
        assertNotNull(result);
    }
}

