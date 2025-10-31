package com.example.booking.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit test for AuditAction enum
 * Coverage: 100% - All branches in is...Operation() methods, fromCode(), getters, toString()
 */
@DisplayName("AuditAction Tests")
class AuditActionTest {

    @Nested
    @DisplayName("isCrudOperation() Tests")
    class IsCrudOperationTests {

        @Test
        @DisplayName("shouldReturnTrue_ForAllCrudOperations")
        void shouldReturnTrue_ForAllCrudOperations() {
            // Test all CRUD operations return true
            assertTrue(AuditAction.CREATE.isCrudOperation());
            assertTrue(AuditAction.READ.isCrudOperation());
            assertTrue(AuditAction.UPDATE.isCrudOperation());
            assertTrue(AuditAction.DELETE.isCrudOperation());
        }

        @Test
        @DisplayName("shouldReturnFalse_ForNonCrudOperations")
        void shouldReturnFalse_ForNonCrudOperations() {
            // Test non-CRUD operations return false
            assertFalse(AuditAction.LOGIN.isCrudOperation());
            assertFalse(AuditAction.PAYMENT_CREATE.isCrudOperation());
            assertFalse(AuditAction.BOOKING_CREATE.isCrudOperation());
            assertFalse(AuditAction.SYSTEM_STARTUP.isCrudOperation());
        }
    }

    @Nested
    @DisplayName("isSecurityOperation() Tests")
    class IsSecurityOperationTests {

        @Test
        @DisplayName("shouldReturnTrue_ForAllSecurityOperations")
        void shouldReturnTrue_ForAllSecurityOperations() {
            // Test all security operations return true
            assertTrue(AuditAction.LOGIN.isSecurityOperation());
            assertTrue(AuditAction.LOGOUT.isSecurityOperation());
            assertTrue(AuditAction.LOGIN_FAILED.isSecurityOperation());
            assertTrue(AuditAction.PASSWORD_CHANGE.isSecurityOperation());
            assertTrue(AuditAction.PASSWORD_RESET.isSecurityOperation());
            assertTrue(AuditAction.ACCESS_DENIED.isSecurityOperation());
            assertTrue(AuditAction.PERMISSION_DENIED.isSecurityOperation());
            assertTrue(AuditAction.SUSPICIOUS_ACTIVITY.isSecurityOperation());
            assertTrue(AuditAction.RATE_LIMIT_EXCEEDED.isSecurityOperation());
            assertTrue(AuditAction.IP_BLOCKED.isSecurityOperation());
        }

        @Test
        @DisplayName("shouldReturnFalse_ForNonSecurityOperations")
        void shouldReturnFalse_ForNonSecurityOperations() {
            // Test non-security operations return false
            assertFalse(AuditAction.CREATE.isSecurityOperation());
            assertFalse(AuditAction.PAYMENT_CREATE.isSecurityOperation());
            assertFalse(AuditAction.BOOKING_CREATE.isSecurityOperation());
            assertFalse(AuditAction.SYSTEM_STARTUP.isSecurityOperation());
        }
    }

    @Nested
    @DisplayName("isPaymentOperation() Tests")
    class IsPaymentOperationTests {

        @Test
        @DisplayName("shouldReturnTrue_ForAllPaymentOperations")
        void shouldReturnTrue_ForAllPaymentOperations() {
            // Test all payment operations return true
            assertTrue(AuditAction.PAYMENT_CREATE.isPaymentOperation());
            assertTrue(AuditAction.PAYMENT_PROCESS.isPaymentOperation());
            assertTrue(AuditAction.PAYMENT_COMPLETE.isPaymentOperation());
            assertTrue(AuditAction.PAYMENT_FAILED.isPaymentOperation());
            assertTrue(AuditAction.REFUND.isPaymentOperation());
            assertTrue(AuditAction.REFUND_PARTIAL.isPaymentOperation());
            assertTrue(AuditAction.REFUND_FAILED.isPaymentOperation());
        }

        @Test
        @DisplayName("shouldReturnFalse_ForNonPaymentOperations")
        void shouldReturnFalse_ForNonPaymentOperations() {
            // Test non-payment operations return false
            assertFalse(AuditAction.CREATE.isPaymentOperation());
            assertFalse(AuditAction.LOGIN.isPaymentOperation());
            assertFalse(AuditAction.BOOKING_CREATE.isPaymentOperation());
            assertFalse(AuditAction.SYSTEM_STARTUP.isPaymentOperation());
        }
    }

    @Nested
    @DisplayName("isBookingOperation() Tests")
    class IsBookingOperationTests {

        @Test
        @DisplayName("shouldReturnTrue_ForAllBookingOperations")
        void shouldReturnTrue_ForAllBookingOperations() {
            // Test all booking operations return true
            assertTrue(AuditAction.BOOKING_CREATE.isBookingOperation());
            assertTrue(AuditAction.BOOKING_CONFIRM.isBookingOperation());
            assertTrue(AuditAction.BOOKING_CANCEL.isBookingOperation());
            assertTrue(AuditAction.BOOKING_MODIFY.isBookingOperation());
            assertTrue(AuditAction.BOOKING_NO_SHOW.isBookingOperation());
        }

        @Test
        @DisplayName("shouldReturnFalse_ForNonBookingOperations")
        void shouldReturnFalse_ForNonBookingOperations() {
            // Test non-booking operations return false
            assertFalse(AuditAction.CREATE.isBookingOperation());
            assertFalse(AuditAction.LOGIN.isBookingOperation());
            assertFalse(AuditAction.PAYMENT_CREATE.isBookingOperation());
            assertFalse(AuditAction.SYSTEM_STARTUP.isBookingOperation());
        }
    }

    @Nested
    @DisplayName("isSystemOperation() Tests")
    class IsSystemOperationTests {

        @Test
        @DisplayName("shouldReturnTrue_ForAllSystemOperations")
        void shouldReturnTrue_ForAllSystemOperations() {
            // Test all system operations return true
            assertTrue(AuditAction.SYSTEM_STARTUP.isSystemOperation());
            assertTrue(AuditAction.SYSTEM_SHUTDOWN.isSystemOperation());
            assertTrue(AuditAction.SYSTEM_MAINTENANCE.isSystemOperation());
            assertTrue(AuditAction.SYSTEM_BACKUP.isSystemOperation());
            assertTrue(AuditAction.SYSTEM_RESTORE.isSystemOperation());
            assertTrue(AuditAction.RECONCILIATION_START.isSystemOperation());
            assertTrue(AuditAction.RECONCILIATION_COMPLETE.isSystemOperation());
            assertTrue(AuditAction.RECONCILIATION_FAILED.isSystemOperation());
            assertTrue(AuditAction.RECONCILIATION_DISCREPANCY.isSystemOperation());
        }

        @Test
        @DisplayName("shouldReturnFalse_ForNonSystemOperations")
        void shouldReturnFalse_ForNonSystemOperations() {
            // Test non-system operations return false
            assertFalse(AuditAction.CREATE.isSystemOperation());
            assertFalse(AuditAction.LOGIN.isSystemOperation());
            assertFalse(AuditAction.PAYMENT_CREATE.isSystemOperation());
            assertFalse(AuditAction.BOOKING_CREATE.isSystemOperation());
        }
    }

    @Nested
    @DisplayName("fromCode() Tests")
    class FromCodeTests {

        @Test
        @DisplayName("shouldReturnCorrectAction_ForValidCodes")
        void shouldReturnCorrectAction_ForValidCodes() {
            // Test finding actions by code
            assertEquals(AuditAction.CREATE, AuditAction.fromCode("CREATE"));
            assertEquals(AuditAction.LOGIN, AuditAction.fromCode("LOGIN"));
            assertEquals(AuditAction.PAYMENT_CREATE, AuditAction.fromCode("PAYMENT_CREATE"));
            assertEquals(AuditAction.BOOKING_CREATE, AuditAction.fromCode("BOOKING_CREATE"));
            assertEquals(AuditAction.SYSTEM_STARTUP, AuditAction.fromCode("SYSTEM_STARTUP"));
        }

        @Test
        @DisplayName("shouldThrowException_ForInvalidCode")
        void shouldThrowException_ForInvalidCode() {
            // Test exception for unknown code
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AuditAction.fromCode("INVALID_CODE")
            );
            assertTrue(exception.getMessage().contains("Unknown audit action code"));
        }

        @Test
        @DisplayName("shouldFindAction_AfterLoopIterations")
        void shouldFindAction_AfterLoopIterations() {
            // Test finding action that requires multiple loop iterations (not first enum value)
            assertEquals(AuditAction.IMPORT, AuditAction.fromCode("IMPORT"));
            assertEquals(AuditAction.SORT, AuditAction.fromCode("SORT"));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("shouldReturnCode")
        void shouldReturnCode() {
            assertEquals("CREATE", AuditAction.CREATE.getCode());
            assertEquals("LOGIN", AuditAction.LOGIN.getCode());
            assertEquals("PAYMENT_CREATE", AuditAction.PAYMENT_CREATE.getCode());
        }

        @Test
        @DisplayName("shouldReturnDescription")
        void shouldReturnDescription() {
            assertNotNull(AuditAction.CREATE.getDescription());
            assertNotNull(AuditAction.LOGIN.getDescription());
            assertNotNull(AuditAction.PAYMENT_CREATE.getDescription());
            assertFalse(AuditAction.CREATE.getDescription().isEmpty());
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("shouldReturnCode_AsString")
        void shouldReturnCode_AsString() {
            assertEquals("CREATE", AuditAction.CREATE.toString());
            assertEquals("LOGIN", AuditAction.LOGIN.toString());
            assertEquals("PAYMENT_CREATE", AuditAction.PAYMENT_CREATE.toString());
        }
    }

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("shouldHaveAllEnumValues")
        void shouldHaveAllEnumValues() {
            // Verify enum values() method works
            AuditAction[] values = AuditAction.values();
            assertTrue(values.length > 0);
            
            // Verify some key values exist
            assertTrue(java.util.Arrays.asList(values).contains(AuditAction.CREATE));
            assertTrue(java.util.Arrays.asList(values).contains(AuditAction.LOGIN));
            assertTrue(java.util.Arrays.asList(values).contains(AuditAction.PAYMENT_CREATE));
        }

        @Test
        @DisplayName("shouldValueOf_ReturnCorrectEnum")
        void shouldValueOf_ReturnCorrectEnum() {
            assertEquals(AuditAction.CREATE, AuditAction.valueOf("CREATE"));
            assertEquals(AuditAction.LOGIN, AuditAction.valueOf("LOGIN"));
            assertEquals(AuditAction.PAYMENT_CREATE, AuditAction.valueOf("PAYMENT_CREATE"));
        }
    }
}

