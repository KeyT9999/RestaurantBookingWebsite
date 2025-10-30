package com.example.booking.scheduler;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.booking.service.VoucherService;

class VoucherSchedulerTest {

    @Mock private VoucherService voucherService;
    private VoucherScheduler scheduler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        scheduler = new VoucherScheduler();
        ReflectionTestUtils.setField(scheduler, "voucherService", voucherService);
    }

    // TC SJ-006
    @Test
    @DisplayName("should activate scheduled vouchers (SJ-006)")
    void shouldActivateScheduledVouchers() {
        scheduler.activateScheduledVouchers();
        verify(voucherService).activateScheduledVouchers();
    }

    // TC SJ-007
    @Test
    @DisplayName("should swallow errors on expire job (SJ-007)")
    void shouldHandleExpireErrors() {
        doThrow(new RuntimeException("DB")).when(voucherService).expireVouchers();
        scheduler.expireVouchers();
        // no exception thrown
    }

    // TC SJ-008
    @Test
    @DisplayName("reminder job is a no-op placeholder (SJ-008)")
    void reminderNoOp() {
        scheduler.sendVoucherExpirationReminders();
    }

    // TC SJ-009
    @Test
    @DisplayName("cleanup job is a no-op placeholder (SJ-009)")
    void cleanupNoOp() {
        scheduler.cleanupOldRedemptions();
    }
}


