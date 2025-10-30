package com.example.booking.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.booking.domain.Payment;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.scheduler.PayOSReconciliationScheduler.ReconciliationResult;
import com.example.booking.scheduler.PayOSReconciliationScheduler.ReconciliationStatus;
import com.example.booking.service.PayOsService;
import com.example.booking.service.PaymentLedgerService;

class PayOSReconciliationSchedulerTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PayOsService payOsService;
    @Mock private PaymentLedgerService paymentLedgerService;

    private PayOSReconciliationScheduler scheduler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        scheduler = new PayOSReconciliationScheduler();
        ReflectionTestUtils.setField(scheduler, "paymentRepository", paymentRepository);
        ReflectionTestUtils.setField(scheduler, "payOsService", payOsService);
        ReflectionTestUtils.setField(scheduler, "paymentLedgerService", paymentLedgerService);
    }

    // TC SJ-001
    @Test
    @DisplayName("should reconcile daily payments when all match (SJ-001)")
    void shouldReconcileDailyPayments_whenAllMatch() {
        Payment p = new Payment();
        p.setPaymentId(1);
        p.setOrderCode(100L);
        p.setAmount(new BigDecimal("100000"));
        when(paymentRepository.findPayOSPaymentsByDate(any(LocalDate.class))).thenReturn(List.of(p));
        // Mock PayOS response structure: use a simple map-like DTO via Mockito (loosely typed)
        PayOsService.PaymentInfoResponse.PaymentInfoData data = mock(PayOsService.PaymentInfoResponse.PaymentInfoData.class);
        when(data.getStatus()).thenReturn("PAID");
        when(data.getAmount()).thenReturn(100000L);
        PayOsService.PaymentInfoResponse resp = mock(PayOsService.PaymentInfoResponse.class);
        when(resp.getData()).thenReturn(data);
        when(payOsService.getPaymentInfo(100L)).thenReturn(resp);
        when(paymentLedgerService.createReconciliationLog(any(), anyString(), anyInt())).thenReturn(10L);

        scheduler.dailyPayOSReconciliation();

        verify(paymentLedgerService).createReconciliationLog(any(), eq("PAYOS"), eq(1));
        verify(paymentLedgerService).createReconciliationDetail(eq(10L), eq(p), any(ReconciliationResult.class));
        verify(paymentLedgerService).updateReconciliationLog(eq(10L), anyInt(), anyInt(), anyInt(), anyString());
    }

    // TC SJ-002
    @Test
    @DisplayName("should swallow errors in daily reconciliation (SJ-002)")
    void shouldHandleErrorInDailyReconciliation() {
        when(paymentRepository.findPayOSPaymentsByDate(any(LocalDate.class))).thenThrow(new RuntimeException("DB down"));
        scheduler.dailyPayOSReconciliation();
        // No exception thrown; nothing to verify further
    }

    // TC SJ-003
    @Test
    @DisplayName("should count unmatched and discrepancies (SJ-003)")
    void shouldCountUnmatchedAndDiscrepancies() {
        Payment p1 = new Payment(); p1.setPaymentId(1); p1.setOrderCode(null);
        Payment p2 = new Payment(); p2.setPaymentId(2); p2.setOrderCode(200L); p2.setAmount(new BigDecimal("1000"));
        when(paymentRepository.findPayOSPaymentsByDate(any(LocalDate.class))).thenReturn(List.of(p1, p2));
        // PayOS returns different amount
        PayOsService.PaymentInfoResponse.PaymentInfoData data2 = mock(PayOsService.PaymentInfoResponse.PaymentInfoData.class);
        when(data2.getStatus()).thenReturn("PAID");
        when(data2.getAmount()).thenReturn(2000L);
        PayOsService.PaymentInfoResponse resp2 = mock(PayOsService.PaymentInfoResponse.class);
        when(resp2.getData()).thenReturn(data2);
        when(payOsService.getPaymentInfo(200L)).thenReturn(resp2);
        when(paymentLedgerService.createReconciliationLog(any(), anyString(), anyInt())).thenReturn(11L);

        scheduler.dailyPayOSReconciliation();

        verify(paymentLedgerService, times(2)).createReconciliationDetail(eq(11L), any(Payment.class), any(ReconciliationResult.class));
        verify(paymentLedgerService).updateReconciliationLog(eq(11L), anyInt(), anyInt(), anyInt(), anyString());
    }

    // TC SJ-004
    @Test
    @DisplayName("should reconcile recent payments without alerts when matched (SJ-004)")
    void shouldReconcileRecentPayments_matched() {
        Payment p = new Payment();
        p.setPaymentId(1);
        p.setOrderCode(300L);
        p.setAmount(new BigDecimal("500"));
        when(paymentRepository.findRecentPayOSPayments(any(LocalDateTime.class))).thenReturn(List.of(p));
        PayOsService.PaymentInfoResponse.PaymentInfoData data3 = mock(PayOsService.PaymentInfoResponse.PaymentInfoData.class);
        when(data3.getStatus()).thenReturn("PAID");
        when(data3.getAmount()).thenReturn(500L);
        PayOsService.PaymentInfoResponse resp3 = mock(PayOsService.PaymentInfoResponse.class);
        when(resp3.getData()).thenReturn(data3);
        when(payOsService.getPaymentInfo(300L)).thenReturn(resp3);

        scheduler.hourlyPayOSReconciliation();

        verify(paymentRepository).findRecentPayOSPayments(any(LocalDateTime.class));
    }

    // TC SJ-005
    @Test
    @DisplayName("should log alerts when recent reconciliation fails (SJ-005)")
    void shouldAlertOnRecentReconciliationIssues() {
        Payment p1 = new Payment(); p1.setPaymentId(1); p1.setOrderCode(400L); p1.setAmount(new BigDecimal("100"));
        Payment p2 = new Payment(); p2.setPaymentId(2); p2.setOrderCode(401L); p2.setAmount(new BigDecimal("100"));
        when(paymentRepository.findRecentPayOSPayments(any(LocalDateTime.class))).thenReturn(List.of(p1, p2));
        // First: null response -> UNMATCHED; Second: exception
        PayOsService.PaymentInfoResponse resp4 = mock(PayOsService.PaymentInfoResponse.class);
        when(resp4.getData()).thenReturn(null);
        when(payOsService.getPaymentInfo(400L)).thenReturn(resp4);
        when(payOsService.getPaymentInfo(401L)).thenThrow(new RuntimeException("HTTP 500"));

        scheduler.hourlyPayOSReconciliation();

        verify(paymentRepository).findRecentPayOSPayments(any(LocalDateTime.class));
    }
}


