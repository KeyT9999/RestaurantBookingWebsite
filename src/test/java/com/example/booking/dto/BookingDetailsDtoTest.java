package com.example.booking.dto;

import com.example.booking.common.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BookingDetailsDto.
 * Coverage Target: ≥80% Branch Coverage
 * Test Strategy: 3 cases per method (happy, edge, error)
 * 
 * @author Senior SDET
 */
@DisplayName("BookingDetailsDto Business Logic Tests")
class BookingDetailsDtoTest {

    // ==================== hasDishes() Tests ====================

    @Test
    @DisplayName("hasDishes - Happy path: returns true when dishes list is not empty")
    void hasDishes_HappyPath_ReturnsTrueWhenListNotEmpty() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        List<BookingDishDto> dishes = new ArrayList<>();
        dishes.add(new BookingDishDto());
        dto.setDishes(dishes);

        // When
        boolean result = dto.hasDishes();

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @DisplayName("hasDishes - Edge cases: handles null and empty lists")
    @MethodSource("dishesEdgeCasesProvider")
    void hasDishes_EdgeCases_HandlesNullAndEmpty(List<BookingDishDto> dishes, boolean expected) {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setDishes(dishes);

        // When
        boolean result = dto.hasDishes();

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("hasDishes - Error case: null dishes list returns false")
    void hasDishes_ErrorCase_NullListReturnsFalse() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setDishes(null);

        // When
        boolean result = dto.hasDishes();

        // Then
        assertThat(result).isFalse();
    }

    // ==================== hasServices() Tests ====================

    @Test
    @DisplayName("hasServices - Happy path: returns true when services list is not empty")
    void hasServices_HappyPath_ReturnsTrueWhenListNotEmpty() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        List<BookingServiceDto> services = new ArrayList<>();
        services.add(new BookingServiceDto());
        dto.setServices(services);

        // When
        boolean result = dto.hasServices();

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @DisplayName("hasServices - Edge cases: handles null and empty lists")
    @MethodSource("servicesEdgeCasesProvider")
    void hasServices_EdgeCases_HandlesNullAndEmpty(List<BookingServiceDto> services, boolean expected) {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setServices(services);

        // When
        boolean result = dto.hasServices();

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("hasServices - Error case: null services list returns false")
    void hasServices_ErrorCase_NullListReturnsFalse() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setServices(null);

        // When
        boolean result = dto.hasServices();

        // Then
        assertThat(result).isFalse();
    }

    // ==================== canBeEdited() Tests ====================

    @Test
    @DisplayName("canBeEdited - Happy path: returns true for PENDING status")
    void canBeEdited_HappyPath_PendingStatusReturnsTrue() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setStatus(BookingStatus.PENDING);

        // When
        boolean result = dto.canBeEdited();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canBeEdited - Happy path: returns true for CONFIRMED status")
    void canBeEdited_HappyPath_ConfirmedStatusReturnsTrue() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setStatus(BookingStatus.CONFIRMED);

        // When
        boolean result = dto.canBeEdited();

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @DisplayName("canBeEdited - Edge cases: returns false for non-editable statuses")
    @EnumSource(value = BookingStatus.class, names = {"COMPLETED", "PENDING_CANCEL", "CANCELLED", "NO_SHOW"})
    void canBeEdited_EdgeCases_NonEditableStatusesReturnFalse(BookingStatus status) {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setStatus(status);

        // When
        boolean result = dto.canBeEdited();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canBeEdited - Error case: null status returns false")
    void canBeEdited_ErrorCase_NullStatusReturnsFalse() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setStatus(null);

        // When
        boolean result = dto.canBeEdited();

        // Then
        assertThat(result).isFalse();
    }

    // ==================== canBeCancelled() Tests ====================

    @Test
    @DisplayName("canBeCancelled - Happy path: returns true for PENDING status")
    void canBeCancelled_HappyPath_PendingStatusReturnsTrue() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setStatus(BookingStatus.PENDING);

        // When
        boolean result = dto.canBeCancelled();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canBeCancelled - Happy path: returns true for CONFIRMED status")
    void canBeCancelled_HappyPath_ConfirmedStatusReturnsTrue() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setStatus(BookingStatus.CONFIRMED);

        // When
        boolean result = dto.canBeCancelled();

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @DisplayName("canBeCancelled - Edge cases: returns false for non-cancellable statuses")
    @EnumSource(value = BookingStatus.class, names = {"COMPLETED", "PENDING_CANCEL", "CANCELLED", "NO_SHOW"})
    void canBeCancelled_EdgeCases_NonCancellableStatusesReturnFalse(BookingStatus status) {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setStatus(status);

        // When
        boolean result = dto.canBeCancelled();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canBeCancelled - Error case: null status returns false")
    void canBeCancelled_ErrorCase_NullStatusReturnsFalse() {
        // Given
        BookingDetailsDto dto = new BookingDetailsDto();
        dto.setStatus(null);

        // When
        boolean result = dto.canBeCancelled();

        // Then
        assertThat(result).isFalse();
    }

    // ==================== Parameterized Test Data Providers ====================

    private static Stream<Arguments> dishesEdgeCasesProvider() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of(Collections.emptyList(), false),
                Arguments.of(createDishesList(1), true),
                Arguments.of(createDishesList(5), true)
        );
    }

    private static Stream<Arguments> servicesEdgeCasesProvider() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of(Collections.emptyList(), false),
                Arguments.of(createServicesList(1), true),
                Arguments.of(createServicesList(3), true)
        );
    }

    private static List<BookingDishDto> createDishesList(int count) {
        List<BookingDishDto> dishes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dishes.add(new BookingDishDto());
        }
        return dishes;
    }

    private static List<BookingServiceDto> createServicesList(int count) {
        List<BookingServiceDto> services = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            services.add(new BookingServiceDto());
        }
        return services;
    }
}

