package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PaymentMethod enum
 */
@DisplayName("PaymentMethod Enum Tests")
public class PaymentMethodTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(PaymentMethod.CASH);
        assertNotNull(PaymentMethod.CARD);
        assertNotNull(PaymentMethod.PAYOS);
        assertNotNull(PaymentMethod.ZALOPAY);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Tiền mặt", PaymentMethod.CASH.getDisplayName());
        assertEquals("Thẻ", PaymentMethod.CARD.getDisplayName());
        assertEquals("payOS", PaymentMethod.PAYOS.getDisplayName());
        assertEquals("ZaloPay", PaymentMethod.ZALOPAY.getDisplayName());
    }
}
