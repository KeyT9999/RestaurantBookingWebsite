package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for BookingServiceDto.
 * Coverage Target: ≥80% Branch Coverage
 * Test Strategy: 3 cases per method (happy, edge, error)
 * 
 * @author Senior SDET
 */
@DisplayName("BookingServiceDto Business Logic Tests")
class BookingServiceDtoTest {

    // ==================== getFormattedPrice() Tests ====================

    @Test
    @DisplayName("getFormattedPrice - Happy path: should format price correctly")
    void getFormattedPrice_HappyPath_FormatsCorrectly() {
        // Given
        BookingServiceDto dto = new BookingServiceDto();
        BigDecimal price = new BigDecimal("150000");
        dto.setPrice(price);

        // When
        String result = dto.getFormattedPrice();

        // Then
        assertThat(result).isEqualTo("150,000 VND");
    }

    @ParameterizedTest
    @DisplayName("getFormattedPrice - Edge cases: various price formats")
    @MethodSource("priceFormatProvider")
    void getFormattedPrice_EdgeCases_HandlesDifferentFormats(BigDecimal price, String expected) {
        // Given
        BookingServiceDto dto = new BookingServiceDto();
        dto.setPrice(price);

        // When
        String result = dto.getFormattedPrice();

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("getFormattedPrice - Error case: null price throws NullPointerException")
    void getFormattedPrice_ErrorCase_NullPriceThrowsException() {
        // Given
        BookingServiceDto dto = new BookingServiceDto();
        dto.setPrice(null);

        // When & Then
        assertThatThrownBy(() -> dto.getFormattedPrice())
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== getFormattedTotalPrice() Tests ====================

    @Test
    @DisplayName("getFormattedTotalPrice - Happy path: should format total price correctly")
    void getFormattedTotalPrice_HappyPath_FormatsCorrectly() {
        // Given
        BookingServiceDto dto = new BookingServiceDto();
        BigDecimal totalPrice = new BigDecimal("300000");
        dto.setTotalPrice(totalPrice);

        // When
        String result = dto.getFormattedTotalPrice();

        // Then
        assertThat(result).isEqualTo("300,000 VND");
    }

    @ParameterizedTest
    @DisplayName("getFormattedTotalPrice - Edge cases: various total price formats")
    @MethodSource("totalPriceFormatProvider")
    void getFormattedTotalPrice_EdgeCases_HandlesDifferentFormats(BigDecimal totalPrice, String expected) {
        // Given
        BookingServiceDto dto = new BookingServiceDto();
        dto.setTotalPrice(totalPrice);

        // When
        String result = dto.getFormattedTotalPrice();

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("getFormattedTotalPrice - Error case: null total price throws NullPointerException")
    void getFormattedTotalPrice_ErrorCase_NullTotalPriceThrowsException() {
        // Given
        BookingServiceDto dto = new BookingServiceDto();
        dto.setTotalPrice(null);

        // When & Then
        assertThatThrownBy(() -> dto.getFormattedTotalPrice())
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Parameterized Test Data Providers ====================

    private static Stream<Arguments> priceFormatProvider() {
        return Stream.of(
                Arguments.of(new BigDecimal("0"), "0 VND"),
                Arguments.of(new BigDecimal("1"), "1 VND"),
                Arguments.of(new BigDecimal("1000"), "1,000 VND"),
                Arguments.of(new BigDecimal("999999"), "999,999 VND"),
                Arguments.of(new BigDecimal("1000000"), "1,000,000 VND"),
                Arguments.of(new BigDecimal("1234567"), "1,234,567 VND"),
                Arguments.of(new BigDecimal("999999999"), "999,999,999 VND")
        );
    }

    private static Stream<Arguments> totalPriceFormatProvider() {
        return Stream.of(
                Arguments.of(new BigDecimal("0"), "0 VND"),
                Arguments.of(new BigDecimal("1"), "1 VND"),
                Arguments.of(new BigDecimal("1000"), "1,000 VND"),
                Arguments.of(new BigDecimal("50000"), "50,000 VND"),
                Arguments.of(new BigDecimal("250000"), "250,000 VND"),
                Arguments.of(new BigDecimal("5000000"), "5,000,000 VND"),
                Arguments.of(new BigDecimal("10000000"), "10,000,000 VND")
        );
    }
}

