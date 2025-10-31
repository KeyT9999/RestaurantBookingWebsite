package com.example.booking.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeoUtils Tests")
class GeoUtilsTest {

    // Test TC GU-001: Calculate distance between same points
    @Test
    @DisplayName("TC GU-001: Should return zero distance for same coordinates")
    void shouldReturnZeroDistanceForSameCoordinates() {
        double lat = 10.762622;
        double lon = 106.660172;
        
        double distance = GeoUtils.haversineKm(lat, lon, lat, lon);
        
        assertEquals(0.0, distance, 0.001);
    }

    // Test TC GU-002: Calculate distance between known points
    @Test
    @DisplayName("TC GU-002: Should calculate distance between Ho Chi Minh City and Hanoi")
    void shouldCalculateDistanceBetweenHCMAndHanoi() {
        // HCM coordinates
        double hcmLat = 10.762622;
        double hcmLon = 106.660172;
        
        // Hanoi coordinates
        double hanoiLat = 21.028511;
        double hanoiLon = 105.804817;
        
        // Expected distance: approximately 1140 km
        double distance = GeoUtils.haversineKm(hcmLat, hcmLon, hanoiLat, hanoiLon);
        
        assertEquals(1140.0, distance, 50.0); // Allow 50km tolerance
    }

    // Test TC GU-003: Calculate short distance
    @Test
    @DisplayName("TC GU-003: Should calculate short distance accurately")
    void shouldCalculateShortDistance() {
        // Same latitude, different longitude (1 degree)
        double lat1 = 10.762622;
        double lon1 = 106.660172;
        double lat2 = 10.762622;
        double lon2 = 107.660172;
        
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        
        // At equator, 1 degree longitude ≈ 111 km
        assertTrue(distance > 90 && distance < 115);
    }

    // Test TC GU-004: Calculate distance north-south
    @Test
    @DisplayName("TC GU-004: Should calculate north-south distance accurately")
    void shouldCalculateNorthSouthDistance() {
        // Same longitude, different latitude
        double lat1 = 10.000;
        double lon1 = 106.000;
        double lat2 = 11.000;
        double lon2 = 106.000;
        
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        
        // 1 degree latitude ≈ 111 km
        assertEquals(111.0, distance, 5.0);
    }

    // Test TC GU-005: Calculate very short distance
    @Test
    @DisplayName("TC GU-005: Should calculate very short distance")
    void shouldCalculateVeryShortDistance() {
        // Very close points (about 1km apart)
        double lat1 = 10.762622;
        double lon1 = 106.660172;
        double lat2 = 10.771000; // ~1km north
        double lon2 = 106.660172;
        
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        
        assertTrue(distance > 0.5 && distance < 2.0);
    }

    // Test TC GU-006: Handle extreme coordinates
    @Test
    @DisplayName("TC GU-006: Should handle extreme coordinates")
    void shouldHandleExtremeCoordinates() {
        // North pole to south pole
        double distance = GeoUtils.haversineKm(90.0, 0.0, -90.0, 0.0);
        
        // Distance should be approximately half the Earth's circumference
        assertEquals(20015.0, distance, 100.0);
    }

    // Test TC GU-007: Calculate distance with international date line crossing
    @Test
    @DisplayName("TC GU-007: Should handle date line crossing")
    void shouldHandleDateLineCrossing() {
        // Points on opposite sides of international date line
        double lat1 = 10.0;
        double lon1 = 179.0;
        double lat2 = 10.0;
        double lon2 = -179.0;
        
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        
        // Should be approximately 222 km (2 degrees at equator)
        assertTrue(distance > 200 && distance < 250);
    }

    // Test TC GU-008: Calculate distance edge case - zero longitude
    @Test
    @DisplayName("TC GU-008: Should handle zero longitude")
    void shouldHandleZeroLongitude() {
        double lat1 = 0.0;
        double lon1 = 0.0;
        double lat2 = 1.0;
        double lon2 = 0.0;
        
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        
        assertEquals(111.0, distance, 5.0);
    }

    // Test TC GU-009: Verify commutative property
    @Test
    @DisplayName("TC GU-009: Should produce same result regardless of direction")
    void shouldProduceSameResultRegardlessOfDirection() {
        double lat1 = 10.762622;
        double lon1 = 106.660172;
        double lat2 = 21.028511;
        double lon2 = 105.804817;
        
        double distance1 = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        double distance2 = GeoUtils.haversineKm(lat2, lon2, lat1, lon1);
        
        assertEquals(distance1, distance2, 0.001);
    }

    // Test TC GU-010: Calculate distance for Ho Chi Minh City landmarks
    @Test
    @DisplayName("TC GU-010: Should calculate distance between HCM landmarks")
    void shouldCalculateDistanceBetweenHCMLandmarks() {
        // District 1 to District 7 (approximately 7-8 km)
        double dist1Lat = 10.762622; // District 1
        double dist1Lon = 106.660172;
        
        double dist7Lat = 10.7328; // District 7
        double dist7Lon = 106.7222;
        
        double distance = GeoUtils.haversineKm(dist1Lat, dist1Lon, dist7Lat, dist7Lon);
        
        assertTrue(distance > 7 && distance < 10);
    }
}

