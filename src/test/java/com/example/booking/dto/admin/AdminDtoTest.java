package com.example.booking.dto.admin;

import com.example.booking.common.enums.CommissionType;
import com.example.booking.common.enums.WithdrawalAuditAction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for all admin DTOs to boost coverage
 */
class AdminDtoTest {

    // ========== CommissionSettingsDto Tests ==========
    @Test
    void testCommissionSettingsDto_DefaultConstructor() {
        CommissionSettingsDto dto = new CommissionSettingsDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testCommissionSettingsDto_ParameterizedConstructor() {
        BigDecimal rate = new BigDecimal("0.30");
        BigDecimal fixed = new BigDecimal("5000");
        BigDecimal minimum = new BigDecimal("100000");

        CommissionSettingsDto dto = new CommissionSettingsDto(
            CommissionType.PERCENTAGE, 
            rate, 
            fixed, 
            minimum
        );

        assertThat(dto.getCommissionType()).isEqualTo(CommissionType.PERCENTAGE);
        assertThat(dto.getCommissionRate()).isEqualByComparingTo(rate);
        assertThat(dto.getCommissionFixedAmount()).isEqualByComparingTo(fixed);
        assertThat(dto.getMinimumWithdrawalAmount()).isEqualByComparingTo(minimum);
    }

    @Test
    void testCommissionSettingsDto_Setters() {
        CommissionSettingsDto dto = new CommissionSettingsDto();
        BigDecimal rate = new BigDecimal("0.25");
        BigDecimal fixed = new BigDecimal("10000");
        BigDecimal minimum = new BigDecimal("200000");

        dto.setCommissionType(CommissionType.FIXED);
        dto.setCommissionRate(rate);
        dto.setCommissionFixedAmount(fixed);
        dto.setMinimumWithdrawalAmount(minimum);

        assertThat(dto.getCommissionType()).isEqualTo(CommissionType.FIXED);
        assertThat(dto.getCommissionRate()).isEqualByComparingTo(rate);
        assertThat(dto.getCommissionFixedAmount()).isEqualByComparingTo(fixed);
        assertThat(dto.getMinimumWithdrawalAmount()).isEqualByComparingTo(minimum);
    }

    // ========== AuditLogDto Tests ==========
    @Test
    void testAuditLogDto_DefaultConstructor() {
        AuditLogDto dto = new AuditLogDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testAuditLogDto_AllFields() {
        AuditLogDto dto = new AuditLogDto();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        dto.setLogId(1);
        dto.setWithdrawalRequestId(100);
        dto.setPerformedByUserId(userId);
        dto.setPerformedByUsername("admin");
        dto.setAction(WithdrawalAuditAction.MARK_PAID);
        dto.setNotes("Test notes");
        dto.setIpAddress("192.168.1.1");
        dto.setUserAgent("Test Agent");
        dto.setPerformedAt(now);
        dto.setRestaurantName("Test Restaurant");
        dto.setWithdrawalStatus("APPROVED");

        assertThat(dto.getLogId()).isEqualTo(1);
        assertThat(dto.getWithdrawalRequestId()).isEqualTo(100);
        assertThat(dto.getPerformedByUserId()).isEqualTo(userId);
        assertThat(dto.getPerformedByUsername()).isEqualTo("admin");
        assertThat(dto.getAction()).isEqualTo(WithdrawalAuditAction.MARK_PAID);
        assertThat(dto.getNotes()).isEqualTo("Test notes");
        assertThat(dto.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(dto.getUserAgent()).isEqualTo("Test Agent");
        assertThat(dto.getPerformedAt()).isEqualTo(now);
        assertThat(dto.getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(dto.getWithdrawalStatus()).isEqualTo("APPROVED");
    }

    // ========== RestaurantBalanceInfoDto Tests ==========
    @Test
    void testRestaurantBalanceInfoDto_DefaultConstructor() {
        RestaurantBalanceInfoDto dto = new RestaurantBalanceInfoDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testRestaurantBalanceInfoDto_AllFields() {
        RestaurantBalanceInfoDto dto = new RestaurantBalanceInfoDto();
        LocalDateTime now = LocalDateTime.now();

        dto.setRestaurantId(1);
        dto.setRestaurantName("Test Restaurant");
        dto.setOwnerEmail("owner@test.com");
        dto.setOwnerPhone("0123456789");
        dto.setTotalRevenue(new BigDecimal("10000000"));
        dto.setAvailableBalance(new BigDecimal("5000000"));
        dto.setPendingWithdrawal(new BigDecimal("1000000"));
        dto.setTotalWithdrawn(new BigDecimal("3000000"));
        dto.setTotalCommission(new BigDecimal("1000000"));
        dto.setTotalBookingsCompleted(100L);
        dto.setTotalWithdrawalRequests(10L);
        dto.setLastWithdrawalAt(now);
        dto.setLastCalculatedAt(now);

        assertThat(dto.getRestaurantId()).isEqualTo(1);
        assertThat(dto.getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(dto.getOwnerEmail()).isEqualTo("owner@test.com");
        assertThat(dto.getOwnerPhone()).isEqualTo("0123456789");
        assertThat(dto.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("10000000"));
        assertThat(dto.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("5000000"));
        assertThat(dto.getPendingWithdrawal()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(dto.getTotalWithdrawn()).isEqualByComparingTo(new BigDecimal("3000000"));
        assertThat(dto.getTotalCommission()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(dto.getTotalBookingsCompleted()).isEqualTo(100L);
        assertThat(dto.getTotalWithdrawalRequests()).isEqualTo(10L);
        assertThat(dto.getLastWithdrawalAt()).isEqualTo(now);
        assertThat(dto.getLastCalculatedAt()).isEqualTo(now);
    }

    // ========== WithdrawalStatsDto Tests ==========
    @Test
    void testWithdrawalStatsDto_DefaultConstructor() {
        WithdrawalStatsDto dto = new WithdrawalStatsDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testWithdrawalStatsDto_ParameterizedConstructor() {
        WithdrawalStatsDto dto = new WithdrawalStatsDto(
            5L, 3L, 10L, 1L, 2L,
            new BigDecimal("1000000"), new BigDecimal("500000"), 
            new BigDecimal("5000000"), new BigDecimal("500000"),
            24.5, 80.0
        );

        assertThat(dto.getPendingCount()).isEqualTo(5L);
        assertThat(dto.getProcessingCount()).isEqualTo(3L);
        assertThat(dto.getSucceededCount()).isEqualTo(10L);
        assertThat(dto.getFailedCount()).isEqualTo(1L);
        assertThat(dto.getRejectedCount()).isEqualTo(2L);
        assertThat(dto.getPendingAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(dto.getProcessingAmount()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(dto.getSucceededAmount()).isEqualByComparingTo(new BigDecimal("5000000"));
        assertThat(dto.getTotalCommission()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(dto.getAverageProcessingTimeHours()).isEqualTo(24.5);
        assertThat(dto.getSuccessRate()).isEqualTo(80.0);
    }

    @Test
    void testWithdrawalStatsDto_Setters() {
        WithdrawalStatsDto dto = new WithdrawalStatsDto();
        
        dto.setPendingCount(5L);
        dto.setProcessingCount(3L);
        dto.setSucceededCount(10L);
        dto.setFailedCount(1L);
        dto.setRejectedCount(2L);
        dto.setPendingAmount(new BigDecimal("1000000"));
        dto.setProcessingAmount(new BigDecimal("500000"));
        dto.setSucceededAmount(new BigDecimal("5000000"));
        dto.setTotalCommission(new BigDecimal("500000"));
        dto.setAverageProcessingTimeHours(24.5);
        dto.setSuccessRate(80.0);

        assertThat(dto.getPendingCount()).isEqualTo(5L);
        assertThat(dto.getProcessingCount()).isEqualTo(3L);
        assertThat(dto.getSucceededCount()).isEqualTo(10L);
        assertThat(dto.getFailedCount()).isEqualTo(1L);
        assertThat(dto.getRejectedCount()).isEqualTo(2L);
    }

    // ========== FavoriteStatisticsDto Tests ==========
    @Test
    void testFavoriteStatisticsDto_DefaultConstructor() {
        FavoriteStatisticsDto dto = new FavoriteStatisticsDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testFavoriteStatisticsDto_ParameterizedConstructor() {
        FavoriteStatisticsDto dto = new FavoriteStatisticsDto(
            1, "Test Restaurant", 100L, 4.5, 50L, 
            new BigDecimal("200000"), "Vietnamese"
        );

        assertThat(dto.getRestaurantId()).isEqualTo(1);
        assertThat(dto.getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(dto.getFavoriteCount()).isEqualTo(100L);
        assertThat(dto.getAverageRating()).isEqualTo(4.5);
        assertThat(dto.getReviewCount()).isEqualTo(50L);
        assertThat(dto.getAveragePrice()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(dto.getCuisineType()).isEqualTo("Vietnamese");
    }

    @Test
    void testFavoriteStatisticsDto_Setters() {
        FavoriteStatisticsDto dto = new FavoriteStatisticsDto();
        
        dto.setRestaurantId(1);
        dto.setRestaurantName("Test Restaurant");
        dto.setFavoriteCount(100L);
        dto.setAverageRating(4.5);
        dto.setReviewCount(50L);
        dto.setAveragePrice(new BigDecimal("200000"));
        dto.setCuisineType("Vietnamese");

        assertThat(dto.getRestaurantId()).isEqualTo(1);
        assertThat(dto.getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(dto.getFavoriteCount()).isEqualTo(100L);
        assertThat(dto.getAverageRating()).isEqualTo(4.5);
        assertThat(dto.getReviewCount()).isEqualTo(50L);
        assertThat(dto.getAveragePrice()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(dto.getCuisineType()).isEqualTo("Vietnamese");
    }

    // ========== UserCreateForm Tests ==========
    @Test
    void testUserCreateForm_DefaultConstructor() {
        UserCreateForm form = new UserCreateForm();
        assertThat(form).isNotNull();
    }

    @Test
    void testUserCreateForm_AllFields() {
        UserCreateForm form = new UserCreateForm();
        
        form.setUsername("testuser");
        form.setEmail("test@example.com");
        form.setFullName("Test User");
        form.setPassword("password123");
        form.setPhoneNumber("0123456789");
        form.setRole(com.example.booking.domain.UserRole.CUSTOMER);
        form.setActive(true);

        assertThat(form.getUsername()).isEqualTo("testuser");
        assertThat(form.getEmail()).isEqualTo("test@example.com");
        assertThat(form.getFullName()).isEqualTo("Test User");
        assertThat(form.getPassword()).isEqualTo("password123");
        assertThat(form.getPhoneNumber()).isEqualTo("0123456789");
        assertThat(form.getRole()).isEqualTo(com.example.booking.domain.UserRole.CUSTOMER);
        assertThat(form.isActive()).isTrue();
    }

    // ========== UserEditForm Tests ==========
    @Test
    void testUserEditForm_DefaultConstructor() {
        UserEditForm form = new UserEditForm();
        assertThat(form).isNotNull();
    }

    @Test
    void testUserEditForm_AllFields() {
        UserEditForm form = new UserEditForm();
        
        form.setFullName("Updated User");
        form.setEmail("updated@example.com");
        form.setPhoneNumber("0987654321");
        form.setRole(com.example.booking.domain.UserRole.ADMIN);
        form.setActive(false);
        form.setNewPassword("newpassword");

        assertThat(form.getFullName()).isEqualTo("Updated User");
        assertThat(form.getEmail()).isEqualTo("updated@example.com");
        assertThat(form.getPhoneNumber()).isEqualTo("0987654321");
        assertThat(form.getRole()).isEqualTo(com.example.booking.domain.UserRole.ADMIN);
        assertThat(form.isActive()).isFalse();
        assertThat(form.getNewPassword()).isEqualTo("newpassword");
    }

    // ========== VoucherCreateForm Tests ==========
    @Test
    void testVoucherCreateForm_DefaultConstructor() {
        VoucherCreateForm form = new VoucherCreateForm();
        assertThat(form).isNotNull();
    }

    @Test
    void testVoucherCreateForm_AllFields() {
        VoucherCreateForm form = new VoucherCreateForm();
        java.time.LocalDate startDate = java.time.LocalDate.now();
        java.time.LocalDate endDate = startDate.plusDays(30);
        
        form.setCode("VOUCHER2024");
        form.setDescription("Test Voucher");
        form.setDiscountType("PERCENT");
        form.setDiscountValue(new BigDecimal("50000"));
        form.setMinOrderAmount(new BigDecimal("200000"));
        form.setGlobalUsageLimit(100);
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setStatus(com.example.booking.domain.VoucherStatus.ACTIVE);

        assertThat(form.getCode()).isEqualTo("VOUCHER2024");
        assertThat(form.getDescription()).isEqualTo("Test Voucher");
        assertThat(form.getDiscountType()).isEqualTo("PERCENT");
        assertThat(form.getDiscountValue()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(form.getMinOrderAmount()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(form.getGlobalUsageLimit()).isEqualTo(100);
        assertThat(form.getStartDate()).isEqualTo(startDate);
        assertThat(form.getEndDate()).isEqualTo(endDate);
        assertThat(form.getStatus()).isEqualTo(com.example.booking.domain.VoucherStatus.ACTIVE);
    }

    // ========== VoucherEditForm Tests ==========
    @Test
    void testVoucherEditForm_DefaultConstructor() {
        VoucherEditForm form = new VoucherEditForm();
        assertThat(form).isNotNull();
    }

    @Test
    void testVoucherEditForm_AllFields() {
        VoucherEditForm form = new VoucherEditForm();
        java.time.LocalDate startDate = java.time.LocalDate.now();
        java.time.LocalDate endDate = startDate.plusDays(60);
        
        form.setVoucherId(1);
        form.setCode("VOUCHER2024");
        form.setDescription("Updated Voucher");
        form.setDiscountType("PERCENT");
        form.setDiscountValue(new BigDecimal("100000"));
        form.setMinOrderAmount(new BigDecimal("300000"));
        form.setGlobalUsageLimit(200);
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setStatus("ACTIVE");

        assertThat(form.getVoucherId()).isEqualTo(1);
        assertThat(form.getCode()).isEqualTo("VOUCHER2024");
        assertThat(form.getDescription()).isEqualTo("Updated Voucher");
        assertThat(form.getDiscountType()).isEqualTo("PERCENT");
        assertThat(form.getDiscountValue()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(form.getMinOrderAmount()).isEqualByComparingTo(new BigDecimal("300000"));
        assertThat(form.getGlobalUsageLimit()).isEqualTo(200);
        assertThat(form.getStartDate()).isEqualTo(startDate);
        assertThat(form.getEndDate()).isEqualTo(endDate);
        assertThat(form.getStatus()).isEqualTo("ACTIVE");
    }

    // ========== VoucherAssignForm Tests ==========
    @Test
    void testVoucherAssignForm_DefaultConstructor() {
        VoucherAssignForm form = new VoucherAssignForm();
        assertThat(form).isNotNull();
    }

    @Test
    void testVoucherAssignForm_AllFields() {
        VoucherAssignForm form = new VoucherAssignForm();
        UUID customerId1 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();
        
        form.setVoucherId(1);
        form.setCustomerIds(java.util.Arrays.asList(customerId1, customerId2));

        assertThat(form.getVoucherId()).isEqualTo(1);
        assertThat(form.getCustomerIds()).isNotNull();
        assertThat(form.getCustomerIds().size()).isEqualTo(2);
        assertThat(form.getCustomerIds()).contains(customerId1, customerId2);
    }
}

