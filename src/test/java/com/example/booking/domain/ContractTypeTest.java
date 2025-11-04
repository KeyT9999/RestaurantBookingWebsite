package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ContractType enum
 */
@DisplayName("ContractType Enum Tests")
public class ContractTypeTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(ContractType.STANDARD);
        assertNotNull(ContractType.PREMIUM);
        assertNotNull(ContractType.ENTERPRISE);
        assertNotNull(ContractType.TRIAL);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Hợp đồng tiêu chuẩn", ContractType.STANDARD.getDisplayName());
        assertEquals("Hợp đồng cao cấp", ContractType.PREMIUM.getDisplayName());
        assertEquals("Hợp đồng doanh nghiệp", ContractType.ENTERPRISE.getDisplayName());
        assertEquals("Hợp đồng thử nghiệm", ContractType.TRIAL.getDisplayName());
    }

    // ========== getDefaultCommissionRate() Tests ==========

    @Test
    @DisplayName("shouldGetDefaultCommissionRate_forStandard")
    void shouldGetDefaultCommissionRate_forStandard() {
        BigDecimal rate = ContractType.STANDARD.getDefaultCommissionRate();
        assertEquals(new BigDecimal("5.00"), rate);
    }

    @Test
    @DisplayName("shouldGetDefaultCommissionRate_forPremium")
    void shouldGetDefaultCommissionRate_forPremium() {
        BigDecimal rate = ContractType.PREMIUM.getDefaultCommissionRate();
        assertEquals(new BigDecimal("4.50"), rate);
    }

    @Test
    @DisplayName("shouldGetDefaultCommissionRate_forEnterprise")
    void shouldGetDefaultCommissionRate_forEnterprise() {
        BigDecimal rate = ContractType.ENTERPRISE.getDefaultCommissionRate();
        assertEquals(new BigDecimal("4.00"), rate);
    }

    @Test
    @DisplayName("shouldGetDefaultCommissionRate_forTrial")
    void shouldGetDefaultCommissionRate_forTrial() {
        BigDecimal rate = ContractType.TRIAL.getDefaultCommissionRate();
        assertEquals(new BigDecimal("0.00"), rate);
    }

    // ========== getDefaultMinimumGuarantee() Tests ==========

    @Test
    @DisplayName("shouldGetDefaultMinimumGuarantee_forStandard")
    void shouldGetDefaultMinimumGuarantee_forStandard() {
        BigDecimal guarantee = ContractType.STANDARD.getDefaultMinimumGuarantee();
        assertEquals(new BigDecimal("1000000.00"), guarantee);
    }

    @Test
    @DisplayName("shouldGetDefaultMinimumGuarantee_forPremium")
    void shouldGetDefaultMinimumGuarantee_forPremium() {
        BigDecimal guarantee = ContractType.PREMIUM.getDefaultMinimumGuarantee();
        assertEquals(new BigDecimal("2000000.00"), guarantee);
    }

    @Test
    @DisplayName("shouldGetDefaultMinimumGuarantee_forEnterprise")
    void shouldGetDefaultMinimumGuarantee_forEnterprise() {
        BigDecimal guarantee = ContractType.ENTERPRISE.getDefaultMinimumGuarantee();
        assertEquals(new BigDecimal("5000000.00"), guarantee);
    }

    @Test
    @DisplayName("shouldGetDefaultMinimumGuarantee_forTrial")
    void shouldGetDefaultMinimumGuarantee_forTrial() {
        BigDecimal guarantee = ContractType.TRIAL.getDefaultMinimumGuarantee();
        assertEquals(new BigDecimal("0.00"), guarantee);
    }

    // ========== getDefaultDurationMonths() Tests ==========

    @Test
    @DisplayName("shouldGetDefaultDurationMonths_forStandard")
    void shouldGetDefaultDurationMonths_forStandard() {
        int months = ContractType.STANDARD.getDefaultDurationMonths();
        assertEquals(12, months);
    }

    @Test
    @DisplayName("shouldGetDefaultDurationMonths_forPremium")
    void shouldGetDefaultDurationMonths_forPremium() {
        int months = ContractType.PREMIUM.getDefaultDurationMonths();
        assertEquals(24, months);
    }

    @Test
    @DisplayName("shouldGetDefaultDurationMonths_forEnterprise")
    void shouldGetDefaultDurationMonths_forEnterprise() {
        int months = ContractType.ENTERPRISE.getDefaultDurationMonths();
        assertEquals(36, months);
    }

    @Test
    @DisplayName("shouldGetDefaultDurationMonths_forTrial")
    void shouldGetDefaultDurationMonths_forTrial() {
        int months = ContractType.TRIAL.getDefaultDurationMonths();
        assertEquals(3, months);
    }
}
