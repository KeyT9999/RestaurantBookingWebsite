package com.example.booking.dto.admin;

import com.example.booking.domain.VoucherStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VoucherCreateForm Test")
class VoucherCreateFormTest {

    @Test
    @DisplayName("Should create VoucherCreateForm and set/get all fields")
    void testVoucherCreateForm_ShouldSetAndGetFields() {
        VoucherCreateForm form = new VoucherCreateForm();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        LocalDateTime createdAt = LocalDateTime.now();

        form.setCode("NEWCODE");
        form.setDescription("New voucher");
        form.setDiscountType("PERCENTAGE");
        form.setDiscountValue(BigDecimal.valueOf(15));
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setGlobalUsageLimit(200);
        form.setPerCustomerLimit(2);
        form.setMinOrderAmount(BigDecimal.valueOf(200000));
        form.setMaxDiscountAmount(BigDecimal.valueOf(100000));
        form.setStatus(VoucherStatus.ACTIVE);
        form.setCreatedAt(createdAt);
        form.setUpdatedAt(createdAt);

        assertEquals("NEWCODE", form.getCode());
        assertEquals("New voucher", form.getDescription());
        assertEquals("PERCENTAGE", form.getDiscountType());
        assertEquals(BigDecimal.valueOf(15), form.getDiscountValue());
        assertEquals(startDate, form.getStartDate());
        assertEquals(endDate, form.getEndDate());
        assertEquals(200, form.getGlobalUsageLimit());
        assertEquals(2, form.getPerCustomerLimit());
        assertEquals(BigDecimal.valueOf(200000), form.getMinOrderAmount());
        assertEquals(BigDecimal.valueOf(100000), form.getMaxDiscountAmount());
        assertEquals(VoucherStatus.ACTIVE, form.getStatus());
        assertEquals(createdAt, form.getCreatedAt());
        assertEquals(createdAt, form.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create VoucherCreateForm with default values")
    void testVoucherCreateForm_DefaultValues() {
        VoucherCreateForm form = new VoucherCreateForm();
        assertNotNull(form);
        assertEquals(1, form.getPerCustomerLimit());
        assertEquals(VoucherStatus.ACTIVE, form.getStatus());
    }
}

