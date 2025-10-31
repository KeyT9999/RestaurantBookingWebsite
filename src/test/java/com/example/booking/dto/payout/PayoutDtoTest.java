package com.example.booking.dto.payout;

import com.example.booking.common.enums.WithdrawalStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Payout DTOs: WithdrawalRequestDto, AccountLookupDto, CreateWithdrawalRequestDto
 */
class PayoutDtoTest {

    // ========== AccountLookupDto Tests ==========
    @Test
    void testAccountLookupDto_DefaultConstructor() {
        AccountLookupDto dto = new AccountLookupDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testAccountLookupDto_Constructor() {
        AccountLookupDto dto = new AccountLookupDto("970422", "1234567890");
        
        assertThat(dto.getBin()).isEqualTo("970422");
        assertThat(dto.getAccountNumber()).isEqualTo("1234567890");
    }

    @Test
    void testAccountLookupDto_SettersAndGetters() {
        AccountLookupDto dto = new AccountLookupDto();
        
        dto.setBin("970415");
        dto.setAccountNumber("9876543210");

        assertThat(dto.getBin()).isEqualTo("970415");
        assertThat(dto.getAccountNumber()).isEqualTo("9876543210");
    }

    // ========== CreateWithdrawalRequestDto Tests ==========
    @Test
    void testCreateWithdrawalRequestDto_DefaultConstructor() {
        CreateWithdrawalRequestDto dto = new CreateWithdrawalRequestDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testCreateWithdrawalRequestDto_SettersAndGetters() {
        CreateWithdrawalRequestDto dto = new CreateWithdrawalRequestDto();
        BigDecimal amount = new BigDecimal("500000");

        dto.setBankAccountId(1);
        dto.setAmount(amount);
        dto.setDescription("Monthly withdrawal");

        assertThat(dto.getBankAccountId()).isEqualTo(1);
        assertThat(dto.getAmount()).isEqualByComparingTo(amount);
        assertThat(dto.getDescription()).isEqualTo("Monthly withdrawal");
    }

    @Test
    void testCreateWithdrawalRequestDto_MinimumAmount() {
        CreateWithdrawalRequestDto dto = new CreateWithdrawalRequestDto();
        BigDecimal minimumAmount = new BigDecimal("100000");

        dto.setAmount(minimumAmount);
        assertThat(dto.getAmount()).isEqualByComparingTo(minimumAmount);
    }

    @Test
    void testCreateWithdrawalRequestDto_NullDescription() {
        CreateWithdrawalRequestDto dto = new CreateWithdrawalRequestDto();
        dto.setBankAccountId(1);
        dto.setAmount(new BigDecimal("200000"));
        dto.setDescription(null);

        assertThat(dto.getDescription()).isNull();
    }

    // ========== WithdrawalRequestDto Tests ==========
    @Test
    void testWithdrawalRequestDto_DefaultConstructor() {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testWithdrawalRequestDto_BasicFields() {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();
        BigDecimal amount = new BigDecimal("1000000");
        LocalDateTime now = LocalDateTime.now();

        dto.setRequestId(1);
        dto.setRestaurantId(5);
        dto.setRestaurantName("Test Restaurant");
        dto.setBankAccountId(10);
        dto.setAmount(amount);
        dto.setDescription("Test withdrawal");
        dto.setStatus(WithdrawalStatus.PENDING);
        dto.setStatusDisplay("Chờ duyệt");
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);

        assertThat(dto.getRequestId()).isEqualTo(1);
        assertThat(dto.getRestaurantId()).isEqualTo(5);
        assertThat(dto.getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(dto.getBankAccountId()).isEqualTo(10);
        assertThat(dto.getAmount()).isEqualByComparingTo(amount);
        assertThat(dto.getDescription()).isEqualTo("Test withdrawal");
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
        assertThat(dto.getStatusDisplay()).isEqualTo("Chờ duyệt");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testWithdrawalRequestDto_ReviewFields() {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();
        LocalDateTime reviewedAt = LocalDateTime.now();

        dto.setReviewedByUsername("admin");
        dto.setReviewedAt(reviewedAt);
        dto.setRejectionReason("Insufficient balance");
        dto.setAdminNotes("Reviewed and approved");

        assertThat(dto.getReviewedByUsername()).isEqualTo("admin");
        assertThat(dto.getReviewedAt()).isEqualTo(reviewedAt);
        assertThat(dto.getRejectionReason()).isEqualTo("Insufficient balance");
        assertThat(dto.getAdminNotes()).isEqualTo("Reviewed and approved");
    }

    @Test
    void testWithdrawalRequestDto_FinancialFields() {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();
        BigDecimal amount = new BigDecimal("1000000");
        BigDecimal commissionAmount = new BigDecimal("50000");
        BigDecimal netAmount = new BigDecimal("950000");

        dto.setAmount(amount);
        dto.setCommissionAmount(commissionAmount);
        dto.setNetAmount(netAmount);

        assertThat(dto.getAmount()).isEqualByComparingTo(amount);
        assertThat(dto.getCommissionAmount()).isEqualByComparingTo(commissionAmount);
        assertThat(dto.getNetAmount()).isEqualByComparingTo(netAmount);
    }

    @Test
    void testWithdrawalRequestDto_OwnerFields() {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();

        dto.setOwnerName("John Doe");
        dto.setOwnerEmail("john@example.com");

        assertThat(dto.getOwnerName()).isEqualTo("John Doe");
        assertThat(dto.getOwnerEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testWithdrawalRequestDto_BankAccountFields() {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();

        dto.setBankAccountNumber("1234567890");
        dto.setAccountHolderName("John Doe");
        dto.setBankCode("970422");
        dto.setBankName("Vietcombank");

        assertThat(dto.getBankAccountNumber()).isEqualTo("1234567890");
        assertThat(dto.getAccountHolderName()).isEqualTo("John Doe");
        assertThat(dto.getBankCode()).isEqualTo("970422");
        assertThat(dto.getBankName()).isEqualTo("Vietcombank");
    }

    @Test
    void testWithdrawalRequestDto_AllStatuses() {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();

        dto.setStatus(WithdrawalStatus.PENDING);
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.PENDING);

        dto.setStatus(WithdrawalStatus.APPROVED);
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);

        dto.setStatus(WithdrawalStatus.REJECTED);
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.REJECTED);

        dto.setStatus(WithdrawalStatus.PROCESSING);
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.PROCESSING);

        dto.setStatus(WithdrawalStatus.SUCCEEDED);
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.SUCCEEDED);

        dto.setStatus(WithdrawalStatus.FAILED);
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.FAILED);

        dto.setStatus(WithdrawalStatus.CANCELLED);
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.CANCELLED);
    }

    @Test
    void testWithdrawalRequestDto_CompleteScenario() {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("2000000");
        BigDecimal commissionAmount = new BigDecimal("100000");
        BigDecimal netAmount = new BigDecimal("1900000");

        // Basic info
        dto.setRequestId(100);
        dto.setRestaurantId(50);
        dto.setRestaurantName("Premium Restaurant");
        dto.setBankAccountId(25);

        // Financial info
        dto.setAmount(amount);
        dto.setCommissionAmount(commissionAmount);
        dto.setNetAmount(netAmount);
        dto.setDescription("Monthly profit withdrawal");

        // Status info
        dto.setStatus(WithdrawalStatus.APPROVED);
        dto.setStatusDisplay("Đã duyệt");

        // Review info
        dto.setReviewedByUsername("admin_user");
        dto.setReviewedAt(now);
        dto.setAdminNotes("Approved after verification");

        // Owner info
        dto.setOwnerName("Jane Smith");
        dto.setOwnerEmail("jane@restaurant.com");

        // Bank account info
        dto.setBankAccountNumber("9876543210");
        dto.setAccountHolderName("Jane Smith");
        dto.setBankCode("970415");
        dto.setBankName("Techcombank");

        // Timestamps
        dto.setCreatedAt(now.minusDays(1));
        dto.setUpdatedAt(now);

        // Verify all fields
        assertThat(dto.getRequestId()).isEqualTo(100);
        assertThat(dto.getRestaurantId()).isEqualTo(50);
        assertThat(dto.getRestaurantName()).isEqualTo("Premium Restaurant");
        assertThat(dto.getBankAccountId()).isEqualTo(25);
        assertThat(dto.getAmount()).isEqualByComparingTo(amount);
        assertThat(dto.getCommissionAmount()).isEqualByComparingTo(commissionAmount);
        assertThat(dto.getNetAmount()).isEqualByComparingTo(netAmount);
        assertThat(dto.getDescription()).isEqualTo("Monthly profit withdrawal");
        assertThat(dto.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);
        assertThat(dto.getStatusDisplay()).isEqualTo("Đã duyệt");
        assertThat(dto.getReviewedByUsername()).isEqualTo("admin_user");
        assertThat(dto.getReviewedAt()).isEqualTo(now);
        assertThat(dto.getAdminNotes()).isEqualTo("Approved after verification");
        assertThat(dto.getOwnerName()).isEqualTo("Jane Smith");
        assertThat(dto.getOwnerEmail()).isEqualTo("jane@restaurant.com");
        assertThat(dto.getBankAccountNumber()).isEqualTo("9876543210");
        assertThat(dto.getAccountHolderName()).isEqualTo("Jane Smith");
        assertThat(dto.getBankCode()).isEqualTo("970415");
        assertThat(dto.getBankName()).isEqualTo("Techcombank");
    }
}

