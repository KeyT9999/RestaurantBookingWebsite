package com.example.booking.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for GeoUtils
 */
@DisplayName("GeoUtils Tests")
public class GeoUtilsTest {

    // ========== haversineKm() Tests ==========

    @Test
    @DisplayName("shouldCalculateDistance_successfully")
    void shouldCalculateDistance_successfully() {
        // Given - Ho Chi Minh City to Hanoi (approximately 1140 km)
        double lat1 = 10.8231; // Ho Chi Minh City
        double lon1 = 106.6297;
        double lat2 = 21.0285; // Hanoi
        double lon2 = 105.8542;

        // When
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);

        // Then
        assertTrue(distance > 1100 && distance < 1200); // Should be approximately 1140 km
    }

    @Test
    @DisplayName("shouldReturnZero_forSameCoordinates")
    void shouldReturnZero_forSameCoordinates() {
        // Given
        double lat = 10.8231;
        double lon = 106.6297;

        // When
        double distance = GeoUtils.haversineKm(lat, lon, lat, lon);

        // Then
        assertEquals(0.0, distance, 0.1);
    }

    @Test
    @DisplayName("shouldCalculateShortDistance_successfully")
    void shouldCalculateShortDistance_successfully() {
        // Given - Short distance (about 5 km)
        double lat1 = 10.8231;
        double lon1 = 106.6297;
        double lat2 = 10.8650; // Nearby coordinates
        double lon2 = 106.6500;

        // When
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);

        // Then
        assertTrue(distance > 0 && distance < 10); // Should be a small positive distance
    }

    @Test
    @DisplayName("shouldHandleNegativeCoordinates")
    void shouldHandleNegativeCoordinates() {
        // Given - Negative latitude (southern hemisphere)
        double lat1 = -10.8231;
        double lon1 = 106.6297;
        double lat2 = -10.8650;
        double lon2 = 106.6500;

        // When
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);

        // Then
        assertTrue(distance >= 0);
    }
}

