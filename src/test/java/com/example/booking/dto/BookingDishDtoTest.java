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
 * Unit tests for BookingDishDto.
 * Coverage Target: ≥80% Branch Coverage
 * Test Strategy: 3 cases per method (happy, edge, error)
 * 
 * @author Senior SDET
 */
@DisplayName("BookingDishDto Business Logic Tests")
class BookingDishDtoTest {

    // ==================== getFormattedPrice() Tests ====================

    @Test
    @DisplayName("getFormattedPrice - Happy path: should format price correctly")
    void getFormattedPrice_HappyPath_FormatsCorrectly() {
        // Given
        BookingDishDto dto = new BookingDishDto();
        BigDecimal price = new BigDecimal("125000");
        dto.setPrice(price);

        // When
        String result = dto.getFormattedPrice();

        // Then
        assertThat(result).isEqualTo("125,000 VND");
    }

    @ParameterizedTest
    @DisplayName("getFormattedPrice - Edge cases: various price formats")
    @MethodSource("priceFormatProvider")
    void getFormattedPrice_EdgeCases_HandlesDifferentFormats(BigDecimal price, String expected) {
        // Given
        BookingDishDto dto = new BookingDishDto();
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
        BookingDishDto dto = new BookingDishDto();
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
        BookingDishDto dto = new BookingDishDto();
        BigDecimal totalPrice = new BigDecimal("375000");
        dto.setTotalPrice(totalPrice);

        // When
        String result = dto.getFormattedTotalPrice();

        // Then
        assertThat(result).isEqualTo("375,000 VND");
    }

    @ParameterizedTest
    @DisplayName("getFormattedTotalPrice - Edge cases: various total price formats")
    @MethodSource("totalPriceFormatProvider")
    void getFormattedTotalPrice_EdgeCases_HandlesDifferentFormats(BigDecimal totalPrice, String expected) {
        // Given
        BookingDishDto dto = new BookingDishDto();
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
        BookingDishDto dto = new BookingDishDto();
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
                Arguments.of(new BigDecimal("5000"), "5,000 VND"),
                Arguments.of(new BigDecimal("75000"), "75,000 VND"),
                Arguments.of(new BigDecimal("999999"), "999,999 VND"),
                Arguments.of(new BigDecimal("2000000"), "2,000,000 VND"),
                Arguments.of(new BigDecimal("15000000"), "15,000,000 VND")
        );
    }

    private static Stream<Arguments> totalPriceFormatProvider() {
        return Stream.of(
                Arguments.of(new BigDecimal("0"), "0 VND"),
                Arguments.of(new BigDecimal("1"), "1 VND"),
                Arguments.of(new BigDecimal("10000"), "10,000 VND"),
                Arguments.of(new BigDecimal("100000"), "100,000 VND"),
                Arguments.of(new BigDecimal("500000"), "500,000 VND"),
                Arguments.of(new BigDecimal("3000000"), "3,000,000 VND"),
                Arguments.of(new BigDecimal("20000000"), "20,000,000 VND")
        );
    }
}

