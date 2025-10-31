package com.example.booking.dto.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VoucherEditForm Test")
class VoucherEditFormTest {

    @Test
    @DisplayName("Should create VoucherEditForm and set/get all fields")
    void testVoucherEditForm_ShouldSetAndGetFields() {
        VoucherEditForm form = new VoucherEditForm();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);

        form.setVoucherId(1);
        form.setCode("DISCOUNT10");
        form.setDescription("10% discount");
        form.setDiscountType("PERCENTAGE");
        form.setDiscountValue(BigDecimal.valueOf(10));
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setGlobalUsageLimit(100);
        form.setPerCustomerLimit(1);
        form.setMinOrderAmount(BigDecimal.valueOf(100000));
        form.setMaxDiscountAmount(BigDecimal.valueOf(50000));
        form.setStatus("ACTIVE");

        assertEquals(1, form.getVoucherId());
        assertEquals("DISCOUNT10", form.getCode());
        assertEquals("10% discount", form.getDescription());
        assertEquals("PERCENTAGE", form.getDiscountType());
        assertEquals(BigDecimal.valueOf(10), form.getDiscountValue());
        assertEquals(startDate, form.getStartDate());
        assertEquals(endDate, form.getEndDate());
        assertEquals(100, form.getGlobalUsageLimit());
        assertEquals(1, form.getPerCustomerLimit());
        assertEquals(BigDecimal.valueOf(100000), form.getMinOrderAmount());
        assertEquals(BigDecimal.valueOf(50000), form.getMaxDiscountAmount());
        assertEquals("ACTIVE", form.getStatus());
    }

    @Test
    @DisplayName("Should create VoucherEditForm with default constructor")
    void testVoucherEditForm_DefaultConstructor() {
        VoucherEditForm form = new VoucherEditForm();
        assertNotNull(form);
        assertNull(form.getVoucherId());
        assertNull(form.getCode());
    }
}

