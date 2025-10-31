package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class RestaurantAvailabilityTest {

    private RestaurantAvailability availability;

    @BeforeEach
    void setUp() {
        availability = new RestaurantAvailability();
        availability.setDate(LocalDate.of(2024, 6, 1));
        availability.setHour(19);
        availability.setTotalTables(10);
        availability.setAvailableTables(5);
        availability.setMaxCapacity(100);
        availability.setCurrentOccupancy(20);
    }

    @Test
    void helperFlagsShouldReflectAvailability() {
        availability.setStatus(RestaurantAvailability.Status.OPEN);
        assertTrue(availability.isAvailable());
        assertFalse(availability.isFullyBooked());

        availability.setAvailableTables(0);
        assertTrue(availability.isFullyBooked());

        availability.setStatus(RestaurantAvailability.Status.FULL);
        availability.setAvailableTables(3);
        assertTrue(availability.isFullyBooked());
    }

    @Test
    void rateCalculationsShouldHandleZeroSafely() {
        availability.setMaxCapacity(0);
        availability.setTotalTables(0);
        assertEquals(0.0, availability.getOccupancyRate());
        assertEquals(0.0, availability.getTableAvailabilityRate());

        availability.setMaxCapacity(200);
        availability.setCurrentOccupancy(50);
        availability.setTotalTables(10);
        availability.setAvailableTables(4);
        assertEquals(0.25, availability.getOccupancyRate());
        assertEquals(0.4, availability.getTableAvailabilityRate());
    }

    @Test
    void peakHourDetectionShouldCheckEveningWindow() {
        availability.setHour(19);
        assertTrue(availability.isPeakHour());

        availability.setHour(10);
        assertFalse(availability.isPeakHour());
    }

    @Test
    void updateAvailabilityShouldAdjustStatusAndTimestamp() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 6, 1, 18, 30);

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(fixedNow);

            availability.updateAvailability(0, 10, 10);
            assertEquals(RestaurantAvailability.Status.FULL, availability.getStatus());
            assertEquals(fixedNow, availability.getLastUpdated());

            availability.updateAvailability(1, 10, 9);
            assertEquals(RestaurantAvailability.Status.LIMITED, availability.getStatus());

            availability.updateAvailability(5, 10, 5);
            assertEquals(RestaurantAvailability.Status.OPEN, availability.getStatus());
        }
    }
}

