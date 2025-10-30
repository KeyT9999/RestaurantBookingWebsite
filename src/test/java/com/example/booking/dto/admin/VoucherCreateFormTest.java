package com.example.booking.dto.admin;

import com.example.booking.domain.VoucherStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for VoucherCreateForm.
 * Coverage Target: 100%
 * Test Cases: 5
 *
 * @author Professional Test Engineer
 */
@DisplayName("VoucherCreateForm Tests")
class VoucherCreateFormTest {

    @Test
    @DisplayName("Should create form with default values")
    void constructor_SetsDefaults() {
        // When
        VoucherCreateForm form = new VoucherCreateForm();

        // Then
        assertThat(form).isNotNull();
        assertThat(form.getPerCustomerLimit()).isEqualTo(1);
        assertThat(form.getStatus()).isEqualTo(VoucherStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should set and get all basic fields")
    void settersGetters_BasicFields_WorkCorrectly() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();

        // When
        form.setCode("SUMMER2024");
        form.setDescription("Summer sale voucher");
        form.setDiscountType("PERCENTAGE");
        form.setDiscountValue(new BigDecimal("20.00"));

        // Then
        assertThat(form.getCode()).isEqualTo("SUMMER2024");
        assertThat(form.getDescription()).isEqualTo("Summer sale voucher");
        assertThat(form.getDiscountType()).isEqualTo("PERCENTAGE");
        assertThat(form.getDiscountValue()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("Should set and get date fields")
    void settersGetters_DateFields_WorkCorrectly() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setCreatedAt(createdAt);
        form.setUpdatedAt(updatedAt);

        // Then
        assertThat(form.getStartDate()).isEqualTo(startDate);
        assertThat(form.getEndDate()).isEqualTo(endDate);
        assertThat(form.getCreatedAt()).isEqualTo(createdAt);
        assertThat(form.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should set and get limit and amount fields")
    void settersGetters_LimitAndAmountFields_WorkCorrectly() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();

        // When
        form.setGlobalUsageLimit(1000);
        form.setPerCustomerLimit(5);
        form.setMinOrderAmount(new BigDecimal("100000"));
        form.setMaxDiscountAmount(new BigDecimal("50000"));

        // Then
        assertThat(form.getGlobalUsageLimit()).isEqualTo(1000);
        assertThat(form.getPerCustomerLimit()).isEqualTo(5);
        assertThat(form.getMinOrderAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(form.getMaxDiscountAmount()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should handle all voucher statuses")
    void setStatus_AllStatuses_WorkCorrectly() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();

        // When & Then - ACTIVE
        form.setStatus(VoucherStatus.ACTIVE);
        assertThat(form.getStatus()).isEqualTo(VoucherStatus.ACTIVE);

        // When & Then - INACTIVE
        form.setStatus(VoucherStatus.INACTIVE);
        assertThat(form.getStatus()).isEqualTo(VoucherStatus.INACTIVE);

        // When & Then - EXPIRED
        form.setStatus(VoucherStatus.EXPIRED);
        assertThat(form.getStatus()).isEqualTo(VoucherStatus.EXPIRED);
    }
}

