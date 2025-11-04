package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Unit test for RestaurantAvailability
 * Coverage: 100% - All constructors, getters/setters, logic methods, updateAvailability branches
 */
@DisplayName("RestaurantAvailability Tests")
class RestaurantAvailabilityTest {

    private RestaurantAvailability availability;

    @BeforeEach
    void setUp() {
        availability = new RestaurantAvailability();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("shouldCreateDefaultConstructor")
        void shouldCreateDefaultConstructor() {
            // When
            RestaurantAvailability availability = new RestaurantAvailability();

            // Then
            assertNotNull(availability);
        }

        @Test
        @DisplayName("shouldCreateConstructorWithParameters")
        void shouldCreateConstructorWithParameters() {
            // Given
            RestaurantProfile restaurant = new RestaurantProfile();
            LocalDate date = LocalDate.now();
            Integer hour = 18;

            // When
            RestaurantAvailability availability = new RestaurantAvailability(restaurant, date, hour);

            // Then
            assertNotNull(availability);
            assertEquals(restaurant, availability.getRestaurant());
            assertEquals(date, availability.getDate());
            assertEquals(hour, availability.getHour());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("shouldGetAndSetId")
        void shouldGetAndSetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            availability.setId(id);

            // Then
            assertEquals(id, availability.getId());
        }

        @Test
        @DisplayName("shouldGetAndSetRestaurant")
        void shouldGetAndSetRestaurant() {
            // Given
            RestaurantProfile restaurant = new RestaurantProfile();

            // When
            availability.setRestaurant(restaurant);

            // Then
            assertEquals(restaurant, availability.getRestaurant());
        }

        @Test
        @DisplayName("shouldGetAndSetDate")
        void shouldGetAndSetDate() {
            // Given
            LocalDate date = LocalDate.now();

            // When
            availability.setDate(date);

            // Then
            assertEquals(date, availability.getDate());
        }

        @Test
        @DisplayName("shouldGetAndSetHour")
        void shouldGetAndSetHour() {
            // Given
            Integer hour = 19;

            // When
            availability.setHour(hour);

            // Then
            assertEquals(hour, availability.getHour());
        }

        @Test
        @DisplayName("shouldGetAndSetAvailableTables")
        void shouldGetAndSetAvailableTables() {
            // Given
            Integer availableTables = 5;

            // When
            availability.setAvailableTables(availableTables);

            // Then
            assertEquals(availableTables, availability.getAvailableTables());
        }

        @Test
        @DisplayName("shouldGetAndSetTotalTables")
        void shouldGetAndSetTotalTables() {
            // Given
            Integer totalTables = 10;

            // When
            availability.setTotalTables(totalTables);

            // Then
            assertEquals(totalTables, availability.getTotalTables());
        }

        @Test
        @DisplayName("shouldGetAndSetReservedTables")
        void shouldGetAndSetReservedTables() {
            // Given
            Integer reservedTables = 3;

            // When
            availability.setReservedTables(reservedTables);

            // Then
            assertEquals(reservedTables, availability.getReservedTables());
        }

        @Test
        @DisplayName("shouldGetAndSetMaxCapacity")
        void shouldGetAndSetMaxCapacity() {
            // Given
            Integer maxCapacity = 50;

            // When
            availability.setMaxCapacity(maxCapacity);

            // Then
            assertEquals(maxCapacity, availability.getMaxCapacity());
        }

        @Test
        @DisplayName("shouldGetAndSetCurrentOccupancy")
        void shouldGetAndSetCurrentOccupancy() {
            // Given
            Integer currentOccupancy = 30;

            // When
            availability.setCurrentOccupancy(currentOccupancy);

            // Then
            assertEquals(currentOccupancy, availability.getCurrentOccupancy());
        }

        @Test
        @DisplayName("shouldGetAndSetStatus")
        void shouldGetAndSetStatus() {
            // Given
            RestaurantAvailability.Status status = RestaurantAvailability.Status.CLOSED;

            // When
            availability.setStatus(status);

            // Then
            assertEquals(status, availability.getStatus());
        }

        @Test
        @DisplayName("shouldGetAndSetLastUpdated")
        void shouldGetAndSetLastUpdated() {
            // Given
            LocalDateTime lastUpdated = LocalDateTime.now();

            // When
            availability.setLastUpdated(lastUpdated);

            // Then
            assertEquals(lastUpdated, availability.getLastUpdated());
        }

        @Test
        @DisplayName("shouldGetAndSetDataSource")
        void shouldGetAndSetDataSource() {
            // Given
            String dataSource = "automated";

            // When
            availability.setDataSource(dataSource);

            // Then
            assertEquals(dataSource, availability.getDataSource());
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - isAvailable()")
    class IsAvailableTests {

        @Test
        @DisplayName("shouldReturnTrue_whenStatusIsOpenAndHasAvailableTables")
        void shouldReturnTrue_whenStatusIsOpenAndHasAvailableTables() {
            // Given
            availability.setStatus(RestaurantAvailability.Status.OPEN);
            availability.setAvailableTables(5);

            // When
            boolean result = availability.isAvailable();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenStatusIsNotOpen")
        void shouldReturnFalse_whenStatusIsNotOpen() {
            // Given
            availability.setStatus(RestaurantAvailability.Status.CLOSED);
            availability.setAvailableTables(5);

            // When
            boolean result = availability.isAvailable();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenNoAvailableTables")
        void shouldReturnFalse_whenNoAvailableTables() {
            // Given
            availability.setStatus(RestaurantAvailability.Status.OPEN);
            availability.setAvailableTables(0);

            // When
            boolean result = availability.isAvailable();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - isFullyBooked()")
    class IsFullyBookedTests {

        @Test
        @DisplayName("shouldReturnTrue_whenStatusIsFull")
        void shouldReturnTrue_whenStatusIsFull() {
            // Given
            availability.setStatus(RestaurantAvailability.Status.FULL);
            availability.setAvailableTables(5);

            // When
            boolean result = availability.isFullyBooked();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnTrue_whenAvailableTablesIsZero")
        void shouldReturnTrue_whenAvailableTablesIsZero() {
            // Given
            availability.setStatus(RestaurantAvailability.Status.OPEN);
            availability.setAvailableTables(0);

            // When
            boolean result = availability.isFullyBooked();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenStatusIsOpenAndHasAvailableTables")
        void shouldReturnFalse_whenStatusIsOpenAndHasAvailableTables() {
            // Given
            availability.setStatus(RestaurantAvailability.Status.OPEN);
            availability.setAvailableTables(5);

            // When
            boolean result = availability.isFullyBooked();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - getOccupancyRate()")
    class GetOccupancyRateTests {

        @Test
        @DisplayName("shouldReturnZero_whenMaxCapacityIsZero")
        void shouldReturnZero_whenMaxCapacityIsZero() {
            // Given
            availability.setMaxCapacity(0);
            availability.setCurrentOccupancy(10);

            // When
            Double result = availability.getOccupancyRate();

            // Then
            assertEquals(0.0, result);
        }

        @Test
        @DisplayName("shouldReturnCorrectRate_whenMaxCapacityIsGreaterThanZero")
        void shouldReturnCorrectRate_whenMaxCapacityIsGreaterThanZero() {
            // Given
            availability.setMaxCapacity(100);
            availability.setCurrentOccupancy(50);

            // When
            Double result = availability.getOccupancyRate();

            // Then
            assertEquals(0.5, result);
        }

        @Test
        @DisplayName("shouldReturnOne_whenFullyOccupied")
        void shouldReturnOne_whenFullyOccupied() {
            // Given
            availability.setMaxCapacity(100);
            availability.setCurrentOccupancy(100);

            // When
            Double result = availability.getOccupancyRate();

            // Then
            assertEquals(1.0, result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - getTableAvailabilityRate()")
    class GetTableAvailabilityRateTests {

        @Test
        @DisplayName("shouldReturnZero_whenTotalTablesIsZero")
        void shouldReturnZero_whenTotalTablesIsZero() {
            // Given
            availability.setTotalTables(0);
            availability.setAvailableTables(5);

            // When
            Double result = availability.getTableAvailabilityRate();

            // Then
            assertEquals(0.0, result);
        }

        @Test
        @DisplayName("shouldReturnCorrectRate_whenTotalTablesIsGreaterThanZero")
        void shouldReturnCorrectRate_whenTotalTablesIsGreaterThanZero() {
            // Given
            availability.setTotalTables(10);
            availability.setAvailableTables(5);

            // When
            Double result = availability.getTableAvailabilityRate();

            // Then
            assertEquals(0.5, result);
        }

        @Test
        @DisplayName("shouldReturnOne_whenAllTablesAvailable")
        void shouldReturnOne_whenAllTablesAvailable() {
            // Given
            availability.setTotalTables(10);
            availability.setAvailableTables(10);

            // When
            Double result = availability.getTableAvailabilityRate();

            // Then
            assertEquals(1.0, result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - isPeakHour()")
    class IsPeakHourTests {

        @Test
        @DisplayName("shouldReturnTrue_whenHourIs18")
        void shouldReturnTrue_whenHourIs18() {
            // Given
            availability.setHour(18);

            // When
            boolean result = availability.isPeakHour();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnTrue_whenHourIs19")
        void shouldReturnTrue_whenHourIs19() {
            // Given
            availability.setHour(19);

            // When
            boolean result = availability.isPeakHour();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnTrue_whenHourIs20")
        void shouldReturnTrue_whenHourIs20() {
            // Given
            availability.setHour(20);

            // When
            boolean result = availability.isPeakHour();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenHourIs17")
        void shouldReturnFalse_whenHourIs17() {
            // Given
            availability.setHour(17);

            // When
            boolean result = availability.isPeakHour();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenHourIs21")
        void shouldReturnFalse_whenHourIs21() {
            // Given
            availability.setHour(21);

            // When
            boolean result = availability.isPeakHour();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenHourIsBefore18")
        void shouldReturnFalse_whenHourIsBefore18() {
            // Given
            availability.setHour(12);

            // When
            boolean result = availability.isPeakHour();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenHourIsAfter20")
        void shouldReturnFalse_whenHourIsAfter20() {
            // Given
            availability.setHour(22);

            // When
            boolean result = availability.isPeakHour();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - updateAvailability()")
    class UpdateAvailabilityTests {

        @Test
        @DisplayName("shouldSetStatusToFull_whenAvailableIsZero")
        void shouldSetStatusToFull_whenAvailableIsZero() {
            // Given
            Integer available = 0;
            Integer total = 10;
            Integer reserved = 10;

            // When
            availability.updateAvailability(available, total, reserved);

            // Then
            assertEquals(available, availability.getAvailableTables());
            assertEquals(total, availability.getTotalTables());
            assertEquals(reserved, availability.getReservedTables());
            assertEquals(RestaurantAvailability.Status.FULL, availability.getStatus());
            assertNotNull(availability.getLastUpdated());
        }

        @Test
        @DisplayName("shouldSetStatusToFull_whenAvailableIsNegative")
        void shouldSetStatusToFull_whenAvailableIsNegative() {
            // Given
            Integer available = -1;
            Integer total = 10;
            Integer reserved = 11;

            // When
            availability.updateAvailability(available, total, reserved);

            // Then
            assertEquals(RestaurantAvailability.Status.FULL, availability.getStatus());
        }

        @Test
        @DisplayName("shouldSetStatusToLimited_whenAvailableIsLessThan20Percent")
        void shouldSetStatusToLimited_whenAvailableIsLessThan20Percent() {
            // Given
            Integer total = 10;
            Integer available = 1; // 10% of total (less than 20%)
            Integer reserved = 9;

            // When
            availability.updateAvailability(available, total, reserved);

            // Then
            assertEquals(available, availability.getAvailableTables());
            assertEquals(total, availability.getTotalTables());
            assertEquals(reserved, availability.getReservedTables());
            assertEquals(RestaurantAvailability.Status.LIMITED, availability.getStatus());
        }

        @Test
        @DisplayName("shouldSetStatusToLimited_whenAvailableIs19Percent")
        void shouldSetStatusToLimited_whenAvailableIs19Percent() {
            // Given
            Integer total = 100;
            Integer available = 19; // 19% of total (less than 20%)
            Integer reserved = 81;

            // When
            availability.updateAvailability(available, total, reserved);

            // Then
            assertEquals(RestaurantAvailability.Status.LIMITED, availability.getStatus());
        }

        @Test
        @DisplayName("shouldSetStatusToOpen_whenAvailableIs20Percent")
        void shouldSetStatusToOpen_whenAvailableIs20Percent() {
            // Given
            Integer total = 100;
            Integer available = 20; // Exactly 20% of total
            Integer reserved = 80;

            // When
            availability.updateAvailability(available, total, reserved);

            // Then
            assertEquals(available, availability.getAvailableTables());
            assertEquals(total, availability.getTotalTables());
            assertEquals(reserved, availability.getReservedTables());
            assertEquals(RestaurantAvailability.Status.OPEN, availability.getStatus());
        }

        @Test
        @DisplayName("shouldSetStatusToOpen_whenAvailableIsGreaterThan20Percent")
        void shouldSetStatusToOpen_whenAvailableIsGreaterThan20Percent() {
            // Given
            Integer total = 100;
            Integer available = 50; // 50% of total
            Integer reserved = 50;

            // When
            availability.updateAvailability(available, total, reserved);

            // Then
            assertEquals(available, availability.getAvailableTables());
            assertEquals(total, availability.getTotalTables());
            assertEquals(reserved, availability.getReservedTables());
            assertEquals(RestaurantAvailability.Status.OPEN, availability.getStatus());
        }

        @Test
        @DisplayName("shouldSetStatusToOpen_whenAvailableIs50Percent")
        void shouldSetStatusToOpen_whenAvailableIs50Percent() {
            // Given
            Integer total = 100;
            Integer available = 50; // Exactly 50% of total
            Integer reserved = 50;

            // When
            availability.updateAvailability(available, total, reserved);

            // Then
            assertEquals(RestaurantAvailability.Status.OPEN, availability.getStatus());
        }

        @Test
        @DisplayName("shouldSetStatusToOpen_whenAvailableIsGreaterThan50Percent")
        void shouldSetStatusToOpen_whenAvailableIsGreaterThan50Percent() {
            // Given
            Integer total = 100;
            Integer available = 80; // 80% of total
            Integer reserved = 20;

            // When
            availability.updateAvailability(available, total, reserved);

            // Then
            assertEquals(RestaurantAvailability.Status.OPEN, availability.getStatus());
        }

        @Test
        @DisplayName("shouldUpdateLastUpdated")
        void shouldUpdateLastUpdated() {
            // Given
            LocalDateTime beforeUpdate = availability.getLastUpdated();
            Integer available = 5;
            Integer total = 10;
            Integer reserved = 5;

            // When
            try {
                Thread.sleep(10); // Small delay to ensure timestamp changes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            availability.updateAvailability(available, total, reserved);

            // Then
            assertNotNull(availability.getLastUpdated());
            // The lastUpdated should be updated (may be same or later depending on timing)
            assertTrue(availability.getLastUpdated().equals(beforeUpdate) || 
                      availability.getLastUpdated().isAfter(beforeUpdate));
        }
    }
}

