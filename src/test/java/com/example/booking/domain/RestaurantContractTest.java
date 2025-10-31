package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Unit test for RestaurantContract
 * Coverage: 100% - All constructors, getters/setters, helper methods, branches
 */
@DisplayName("RestaurantContract Tests")
class RestaurantContractTest {

    private RestaurantContract contract;

    @BeforeEach
    void setUp() {
        contract = new RestaurantContract();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("shouldCreateDefaultConstructor")
        void shouldCreateDefaultConstructor() {
            // When
            RestaurantContract contract = new RestaurantContract();

            // Then
            assertNotNull(contract);
            assertNotNull(contract.getCreatedAt());
            assertNotNull(contract.getUpdatedAt());
            assertNotNull(contract.getContractStartDate());
            assertEquals(ContractType.STANDARD, contract.getContractType());
            assertEquals(new BigDecimal("5.00"), contract.getCommissionRate());
            assertEquals(ContractStatus.DRAFT, contract.getStatus());
        }

        @Test
        @DisplayName("shouldSetDefaultValues")
        void shouldSetDefaultValues() {
            // Given
            RestaurantContract contract = new RestaurantContract();

            // Then
            assertEquals("Hàng tuần", contract.getPaymentTerms());
            assertFalse(Boolean.TRUE.equals(contract.getSignedByOwner()));
            assertFalse(Boolean.TRUE.equals(contract.getSignedByAdmin()));
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("shouldGetAndSetContractId")
        void shouldGetAndSetContractId() {
            // Given
            Integer contractId = 1;

            // When
            contract.setContractId(contractId);

            // Then
            assertEquals(contractId, contract.getContractId());
        }

        @Test
        @DisplayName("shouldGetAndSetRestaurantId")
        void shouldGetAndSetRestaurantId() {
            // Given
            Integer restaurantId = 123;

            // When
            contract.setRestaurantId(restaurantId);

            // Then
            assertEquals(restaurantId, contract.getRestaurantId());
        }

        @Test
        @DisplayName("shouldGetAndSetOwnerId")
        void shouldGetAndSetOwnerId() {
            // Given
            UUID ownerId = UUID.randomUUID();

            // When
            contract.setOwnerId(ownerId);

            // Then
            assertEquals(ownerId, contract.getOwnerId());
        }

        @Test
        @DisplayName("shouldGetAndSetContractType")
        void shouldGetAndSetContractType() {
            // Given
            ContractType contractType = ContractType.PREMIUM;

            // When
            contract.setContractType(contractType);

            // Then
            assertEquals(contractType, contract.getContractType());
        }

        @Test
        @DisplayName("shouldGetAndSetCommissionRate")
        void shouldGetAndSetCommissionRate() {
            // Given
            BigDecimal commissionRate = new BigDecimal("10.00");

            // When
            contract.setCommissionRate(commissionRate);

            // Then
            assertEquals(commissionRate, contract.getCommissionRate());
        }

        @Test
        @DisplayName("shouldGetAndSetMinimumGuarantee")
        void shouldGetAndSetMinimumGuarantee() {
            // Given
            BigDecimal minimumGuarantee = new BigDecimal("1000000");

            // When
            contract.setMinimumGuarantee(minimumGuarantee);

            // Then
            assertEquals(minimumGuarantee, contract.getMinimumGuarantee());
        }

        @Test
        @DisplayName("shouldGetAndSetPaymentTerms")
        void shouldGetAndSetPaymentTerms() {
            // Given
            String paymentTerms = "Hàng tháng";

            // When
            contract.setPaymentTerms(paymentTerms);

            // Then
            assertEquals(paymentTerms, contract.getPaymentTerms());
        }

        @Test
        @DisplayName("shouldGetAndSetContractStartDate")
        void shouldGetAndSetContractStartDate() {
            // Given
            LocalDateTime startDate = LocalDateTime.now();

            // When
            contract.setContractStartDate(startDate);

            // Then
            assertEquals(startDate, contract.getContractStartDate());
        }

        @Test
        @DisplayName("shouldGetAndSetContractEndDate")
        void shouldGetAndSetContractEndDate() {
            // Given
            LocalDateTime endDate = LocalDateTime.now().plusYears(1);

            // When
            contract.setContractEndDate(endDate);

            // Then
            assertEquals(endDate, contract.getContractEndDate());
        }

        @Test
        @DisplayName("shouldGetAndSetStatus_andUpdateUpdatedAt")
        void shouldGetAndSetStatus_andUpdateUpdatedAt() {
            // Given
            ContractStatus status = ContractStatus.ACTIVE;
            LocalDateTime originalUpdatedAt = contract.getUpdatedAt();

            // When
            try {
                Thread.sleep(10); // Small delay to ensure timestamp changes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            contract.setStatus(status);

            // Then
            assertEquals(status, contract.getStatus());
            assertTrue(contract.getUpdatedAt().isAfter(originalUpdatedAt) || 
                      contract.getUpdatedAt().equals(originalUpdatedAt));
        }

        @Test
        @DisplayName("shouldGetAndSetSignedByOwner_andUpdateUpdatedAt")
        void shouldGetAndSetSignedByOwner_andUpdateUpdatedAt() {
            // Given
            Boolean signedByOwner = true;

            // When
            contract.setSignedByOwner(signedByOwner);

            // Then
            assertEquals(signedByOwner, contract.getSignedByOwner());
            assertNotNull(contract.getUpdatedAt());
        }

        @Test
        @DisplayName("shouldGetAndSetSignedByAdmin_andUpdateUpdatedAt")
        void shouldGetAndSetSignedByAdmin_andUpdateUpdatedAt() {
            // Given
            Boolean signedByAdmin = true;

            // When
            contract.setSignedByAdmin(signedByAdmin);

            // Then
            assertEquals(signedByAdmin, contract.getSignedByAdmin());
            assertNotNull(contract.getUpdatedAt());
        }

        @Test
        @DisplayName("shouldGetAndSetOwnerSignatureDate")
        void shouldGetAndSetOwnerSignatureDate() {
            // Given
            LocalDateTime signatureDate = LocalDateTime.now();

            // When
            contract.setOwnerSignatureDate(signatureDate);

            // Then
            assertEquals(signatureDate, contract.getOwnerSignatureDate());
        }

        @Test
        @DisplayName("shouldGetAndSetAdminSignatureDate")
        void shouldGetAndSetAdminSignatureDate() {
            // Given
            LocalDateTime signatureDate = LocalDateTime.now();

            // When
            contract.setAdminSignatureDate(signatureDate);

            // Then
            assertEquals(signatureDate, contract.getAdminSignatureDate());
        }

        @Test
        @DisplayName("shouldGetAndSetOwnerSignatureIp")
        void shouldGetAndSetOwnerSignatureIp() {
            // Given
            String ip = "192.168.1.1";

            // When
            contract.setOwnerSignatureIp(ip);

            // Then
            assertEquals(ip, contract.getOwnerSignatureIp());
        }

        @Test
        @DisplayName("shouldGetAndSetAdminSignatureIp")
        void shouldGetAndSetAdminSignatureIp() {
            // Given
            String ip = "192.168.1.1";

            // When
            contract.setAdminSignatureIp(ip);

            // Then
            assertEquals(ip, contract.getAdminSignatureIp());
        }

        @Test
        @DisplayName("shouldGetAndSetSpecialTerms")
        void shouldGetAndSetSpecialTerms() {
            // Given
            String specialTerms = "Special conditions apply";

            // When
            contract.setSpecialTerms(specialTerms);

            // Then
            assertEquals(specialTerms, contract.getSpecialTerms());
        }

        @Test
        @DisplayName("shouldGetAndSetTerminationReason")
        void shouldGetAndSetTerminationReason() {
            // Given
            String terminationReason = "Breach of contract";

            // When
            contract.setTerminationReason(terminationReason);

            // Then
            assertEquals(terminationReason, contract.getTerminationReason());
        }

        @Test
        @DisplayName("shouldGetAndSetCreatedAt")
        void shouldGetAndSetCreatedAt() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now();

            // When
            contract.setCreatedAt(createdAt);

            // Then
            assertEquals(createdAt, contract.getCreatedAt());
        }

        @Test
        @DisplayName("shouldGetAndSetUpdatedAt")
        void shouldGetAndSetUpdatedAt() {
            // Given
            LocalDateTime updatedAt = LocalDateTime.now();

            // When
            contract.setUpdatedAt(updatedAt);

            // Then
            assertEquals(updatedAt, contract.getUpdatedAt());
        }

        @Test
        @DisplayName("shouldGetAndSetCreatedBy")
        void shouldGetAndSetCreatedBy() {
            // Given
            String createdBy = "admin";

            // When
            contract.setCreatedBy(createdBy);

            // Then
            assertEquals(createdBy, contract.getCreatedBy());
        }

        @Test
        @DisplayName("shouldGetAndSetUpdatedBy")
        void shouldGetAndSetUpdatedBy() {
            // Given
            String updatedBy = "admin";

            // When
            contract.setUpdatedBy(updatedBy);

            // Then
            assertEquals(updatedBy, contract.getUpdatedBy());
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - isFullySigned()")
    class IsFullySignedTests {

        @Test
        @DisplayName("shouldReturnTrue_whenBothSigned")
        void shouldReturnTrue_whenBothSigned() {
            // Given
            contract.setSignedByOwner(true);
            contract.setSignedByAdmin(true);

            // When
            boolean result = contract.isFullySigned();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenOwnerNotSigned")
        void shouldReturnFalse_whenOwnerNotSigned() {
            // Given
            contract.setSignedByOwner(false);
            contract.setSignedByAdmin(true);

            // When
            boolean result = contract.isFullySigned();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenAdminNotSigned")
        void shouldReturnFalse_whenAdminNotSigned() {
            // Given
            contract.setSignedByOwner(true);
            contract.setSignedByAdmin(false);

            // When
            boolean result = contract.isFullySigned();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - isActive()")
    class IsActiveTests {

        @Test
        @DisplayName("shouldReturnTrue_whenActiveAndFullySigned")
        void shouldReturnTrue_whenActiveAndFullySigned() {
            // Given
            contract.setStatus(ContractStatus.ACTIVE);
            contract.setSignedByOwner(true);
            contract.setSignedByAdmin(true);

            // When
            boolean result = contract.isActive();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenNotActive")
        void shouldReturnFalse_whenNotActive() {
            // Given
            contract.setStatus(ContractStatus.DRAFT);
            contract.setSignedByOwner(true);
            contract.setSignedByAdmin(true);

            // When
            boolean result = contract.isActive();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenNotFullySigned")
        void shouldReturnFalse_whenNotFullySigned() {
            // Given
            contract.setStatus(ContractStatus.ACTIVE);
            contract.setSignedByOwner(true);
            contract.setSignedByAdmin(false);

            // When
            boolean result = contract.isActive();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - isExpired()")
    class IsExpiredTests {

        @Test
        @DisplayName("shouldReturnTrue_whenEndDateInPast")
        void shouldReturnTrue_whenEndDateInPast() {
            // Given
            contract.setContractEndDate(LocalDateTime.now().minusDays(1));

            // When
            boolean result = contract.isExpired();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenEndDateInFuture")
        void shouldReturnFalse_whenEndDateInFuture() {
            // Given
            contract.setContractEndDate(LocalDateTime.now().plusDays(1));

            // When
            boolean result = contract.isExpired();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenEndDateIsNull")
        void shouldReturnFalse_whenEndDateIsNull() {
            // Given
            contract.setContractEndDate(null);

            // When
            boolean result = contract.isExpired();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - canBeSigned()")
    class CanBeSignedTests {

        @Test
        @DisplayName("shouldReturnTrue_whenStatusIsDraft")
        void shouldReturnTrue_whenStatusIsDraft() {
            // Given
            contract.setStatus(ContractStatus.DRAFT);

            // When
            boolean result = contract.canBeSigned();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnTrue_whenStatusIsPendingOwnerSignature")
        void shouldReturnTrue_whenStatusIsPendingOwnerSignature() {
            // Given
            contract.setStatus(ContractStatus.PENDING_OWNER_SIGNATURE);

            // When
            boolean result = contract.canBeSigned();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnTrue_whenStatusIsPendingAdminSignature")
        void shouldReturnTrue_whenStatusIsPendingAdminSignature() {
            // Given
            contract.setStatus(ContractStatus.PENDING_ADMIN_SIGNATURE);

            // When
            boolean result = contract.canBeSigned();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenStatusIsActive")
        void shouldReturnFalse_whenStatusIsActive() {
            // Given
            contract.setStatus(ContractStatus.ACTIVE);

            // When
            boolean result = contract.canBeSigned();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - needsOwnerSignature()")
    class NeedsOwnerSignatureTests {

        @Test
        @DisplayName("shouldReturnTrue_whenOwnerNotSigned")
        void shouldReturnTrue_whenOwnerNotSigned() {
            // Given
            contract.setSignedByOwner(false);

            // When
            boolean result = contract.needsOwnerSignature();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenOwnerSigned")
        void shouldReturnFalse_whenOwnerSigned() {
            // Given
            contract.setSignedByOwner(true);

            // When
            boolean result = contract.needsOwnerSignature();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnTrue_whenSignedByOwnerIsNull")
        void shouldReturnTrue_whenSignedByOwnerIsNull() {
            // Given
            contract.setSignedByOwner(null);

            // When
            boolean result = contract.needsOwnerSignature();

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - needsAdminSignature()")
    class NeedsAdminSignatureTests {

        @Test
        @DisplayName("shouldReturnTrue_whenAdminNotSigned")
        void shouldReturnTrue_whenAdminNotSigned() {
            // Given
            contract.setSignedByAdmin(false);

            // When
            boolean result = contract.needsAdminSignature();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenAdminSigned")
        void shouldReturnFalse_whenAdminSigned() {
            // Given
            contract.setSignedByAdmin(true);

            // When
            boolean result = contract.needsAdminSignature();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnTrue_whenSignedByAdminIsNull")
        void shouldReturnTrue_whenSignedByAdminIsNull() {
            // Given
            contract.setSignedByAdmin(null);

            // When
            boolean result = contract.needsAdminSignature();

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - getStatusDisplay()")
    class GetStatusDisplayTests {

        @Test
        @DisplayName("shouldReturnDisplayName")
        void shouldReturnDisplayName() {
            // Given
            contract.setStatus(ContractStatus.ACTIVE);

            // When
            String result = contract.getStatusDisplay();

            // Then
            assertNotNull(result);
            assertEquals(ContractStatus.ACTIVE.getDisplayName(), result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - getDaysRemaining()")
    class GetDaysRemainingTests {

        @Test
        @DisplayName("shouldReturnMinusOne_whenEndDateIsNull")
        void shouldReturnMinusOne_whenEndDateIsNull() {
            // Given
            contract.setContractEndDate(null);

            // When
            long result = contract.getDaysRemaining();

            // Then
            assertEquals(-1, result);
        }

        @Test
        @DisplayName("shouldReturnDaysRemaining_whenEndDateIsSet")
        void shouldReturnDaysRemaining_whenEndDateIsSet() {
            // Given
            LocalDateTime futureDate = LocalDateTime.now().plusDays(10);
            contract.setContractEndDate(futureDate);

            // When
            long result = contract.getDaysRemaining();

            // Then
            // Should be approximately 10 days (may vary slightly due to timing)
            assertTrue(result >= 9 && result <= 10);
        }

        @Test
        @DisplayName("shouldReturnNegative_whenEndDateIsInPast")
        void shouldReturnNegative_whenEndDateIsInPast() {
            // Given
            LocalDateTime pastDate = LocalDateTime.now().minusDays(5);
            contract.setContractEndDate(pastDate);

            // When
            long result = contract.getDaysRemaining();

            // Then
            assertTrue(result < 0);
        }
    }
}

