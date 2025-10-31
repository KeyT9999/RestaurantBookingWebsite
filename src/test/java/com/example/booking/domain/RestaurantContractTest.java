package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class RestaurantContractTest {

    private RestaurantContract contract;

    @BeforeEach
    void setUp() {
        contract = new RestaurantContract();
    }

    @Test
    void defaultConstructorShouldInitializeDefaultValues() {
        assertEquals(ContractStatus.DRAFT, contract.getStatus());
        assertEquals(ContractType.STANDARD, contract.getContractType());
        assertEquals(new BigDecimal("5.00"), contract.getCommissionRate());
        assertEquals("Hàng tuần", contract.getPaymentTerms());
        assertFalse(contract.getSignedByOwner());
        assertFalse(contract.getSignedByAdmin());
        assertNotNull(contract.getCreatedAt());
        assertNotNull(contract.getUpdatedAt());
        assertNotNull(contract.getContractStartDate());
    }

    @Test
    void settersShouldUpdateTimestampsAndFields() {
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        contract.setUpdatedAt(originalUpdatedAt);

        contract.setRestaurantId(10);
        contract.setOwnerId(UUID.randomUUID());
        contract.setStatus(ContractStatus.PENDING_OWNER_SIGNATURE);
        contract.setSignedByOwner(true);
        contract.setSignedByAdmin(true);

        assertEquals(10, contract.getRestaurantId());
        assertEquals(ContractStatus.PENDING_OWNER_SIGNATURE, contract.getStatus());
        assertTrue(contract.getUpdatedAt().isAfter(originalUpdatedAt));
        assertTrue(contract.getSignedByOwner());
        assertTrue(contract.getSignedByAdmin());
    }

    @Test
    void signatureHelpersShouldReflectState() {
        assertTrue(contract.needsOwnerSignature());
        assertTrue(contract.needsAdminSignature());

        contract.setSignedByOwner(true);
        contract.setSignedByAdmin(true);

        assertTrue(contract.isFullySigned());

        contract.setStatus(ContractStatus.ACTIVE);
        assertTrue(contract.isActive());
    }

    @Test
    void canBeSignedShouldDependOnStatus() {
        contract.setStatus(ContractStatus.DRAFT);
        assertTrue(contract.canBeSigned());

        contract.setStatus(ContractStatus.CANCELLED);
        assertFalse(contract.canBeSigned());
    }

    @Test
    void expirationChecksShouldUseCurrentTime() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 6, 1, 12, 0);
        LocalDateTime past = fixedNow.minusDays(2);
        LocalDateTime future = fixedNow.plusDays(5);

        contract.setContractEndDate(past);

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(fixedNow);
            assertTrue(contract.isExpired());
        }

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(fixedNow);
            contract.setContractEndDate(future);
            assertEquals(5, contract.getDaysRemaining());
            assertFalse(contract.isExpired());
        }
    }

    @Test
    void statusDisplayShouldDelegateToEnum() {
        contract.setStatus(ContractStatus.PENDING_ADMIN_SIGNATURE);
        assertEquals(ContractStatus.PENDING_ADMIN_SIGNATURE.getDisplayName(), contract.getStatusDisplay());
    }
}

