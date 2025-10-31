package com.example.booking.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditActionTest {

    @Test
    @DisplayName("fromCode should map string codes to enum values")
    void shouldResolveFromCode() {
        assertThat(AuditAction.fromCode("CREATE")).isEqualTo(AuditAction.CREATE);
        assertThat(AuditAction.fromCode("LOGIN")).isEqualTo(AuditAction.LOGIN);
    }

    @Test
    @DisplayName("fromCode should throw for unknown codes")
    void shouldThrowForUnknownCode() {
        assertThatThrownBy(() -> AuditAction.fromCode("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown audit action code");
    }

    @Test
    @DisplayName("helper methods should classify action categories correctly")
    void shouldClassifyActions() {
        assertThat(AuditAction.CREATE.isCrudOperation()).isTrue();
        assertThat(AuditAction.LOGIN.isSecurityOperation()).isTrue();
        assertThat(AuditAction.PAYMENT_PROCESS.isPaymentOperation()).isTrue();
        assertThat(AuditAction.BOOKING_CANCEL.isBookingOperation()).isTrue();
        assertThat(AuditAction.SYSTEM_MAINTENANCE.isSystemOperation()).isTrue();

        assertThat(AuditAction.EMAIL_SEND.isCrudOperation()).isFalse();
        assertThat(AuditAction.API_CALL.isSecurityOperation()).isFalse();
    }

    @Test
    @DisplayName("toString should render action code")
    void shouldRenderCodeInToString() {
        assertThat(AuditAction.DELETE.toString()).isEqualTo("DELETE");
    }
}
