package com.example.booking.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for GeoUtils
 * 
 * Coverage Target: 100%
 * 
 * @author Professional Test Engineer
 */
@DisplayName("GeoUtils Tests")
class GeoUtilsTest {

    @Test
    @DisplayName("Should calculate distance between two same points as zero")
    void haversineKm_SamePoints_ReturnsZero() {
        // Given
        double lat = 10.776889;
        double lon = 106.700806;

        // When
        double distance = GeoUtils.haversineKm(lat, lon, lat, lon);

        // Then
        assertThat(distance).isCloseTo(0.0, within(0.001));
    }

    @Test
    @DisplayName("Should calculate correct distance between HCMC and Hanoi")
    void haversineKm_HcmcToHanoi_ReturnsCorrectDistance() {
        // Given - HCMC coordinates
        double hcmcLat = 10.776889;
        double hcmcLon = 106.700806;
        
        // Hanoi coordinates
        double hanoiLat = 21.027763;
        double hanoiLon = 105.834160;

        // When
        double distance = GeoUtils.haversineKm(hcmcLat, hcmcLon, hanoiLat, hanoiLon);

        // Then - distance should be approximately 1144 km
        assertThat(distance).isCloseTo(1144.0, within(5.0));
    }

    @Test
    @DisplayName("Should calculate correct distance between HCMC and Da Nang")
    void haversineKm_HcmcToDaNang_ReturnsCorrectDistance() {
        // Given
        double hcmcLat = 10.776889;
        double hcmcLon = 106.700806;
        
        double danangLat = 16.047079;
        double danangLon = 108.206230;

        // When
        double distance = GeoUtils.haversineKm(hcmcLat, hcmcLon, danangLat, danangLon);

        // Then - distance should be approximately 608 km
        assertThat(distance).isCloseTo(608.0, within(10.0));
    }

    @Test
    @DisplayName("Should calculate symmetric distance (A to B = B to A)")
    void haversineKm_Symmetric_ReturnsSameDistance() {
        // Given
        double lat1 = 10.776889;
        double lon1 = 106.700806;
        double lat2 = 21.027763;
        double lon2 = 105.834160;

        // When
        double distanceAtoB = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        double distanceBtoA = GeoUtils.haversineKm(lat2, lon2, lat1, lon1);

        // Then
        assertThat(distanceAtoB).isCloseTo(distanceBtoA, within(0.001));
    }

    @Test
    @DisplayName("Should handle negative coordinates correctly")
    void haversineKm_NegativeCoordinates_CalculatesCorrectly() {
        // Given - coordinates with negative values
        double lat1 = -33.865143;  // Sydney
        double lon1 = 151.209900;
        double lat2 = -37.840935;  // Melbourne
        double lon2 = 144.946457;

        // When
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);

        // Then - distance should be approximately 714 km
        assertThat(distance).isCloseTo(714.0, within(20.0));
    }

    @Test
    @DisplayName("Should handle coordinates crossing the equator")
    void haversineKm_CrossingEquator_CalculatesCorrectly() {
        // Given
        double lat1 = 10.0;   // North
        double lon1 = 100.0;
        double lat2 = -10.0;  // South
        double lon2 = 100.0;

        // When
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);

        // Then - distance should be approximately 2226 km (20 degrees latitude)
        assertThat(distance).isGreaterThan(2200.0);
        assertThat(distance).isLessThan(2300.0);
    }

    @Test
    @DisplayName("Should handle coordinates crossing the prime meridian")
    void haversineKm_CrossingPrimeMeridian_CalculatesCorrectly() {
        // Given
        double lat1 = 51.5074;  // London
        double lon1 = -0.1278;
        double lat2 = 48.8566;  // Paris
        double lon2 = 2.3522;

        // When
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);

        // Then - distance should be approximately 344 km
        assertThat(distance).isCloseTo(344.0, within(20.0));
    }

    @Test
    @DisplayName("Should handle very small distances accurately")
    void haversineKm_VerySmallDistance_CalculatesAccurately() {
        // Given - two points 1km apart (approximately)
        double lat1 = 10.776889;
        double lon1 = 106.700806;
        double lat2 = 10.785889;  // ~1km north
        double lon2 = 106.700806;

        // When
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);

        // Then
        assertThat(distance).isCloseTo(1.0, within(0.1));
    }

    @Test
    @DisplayName("Should handle edge case of poles")
    void haversineKm_Poles_CalculatesCorrectly() {
        // Given - North Pole to South Pole
        double northPoleLat = 90.0;
        double northPoleLon = 0.0;
        double southPoleLat = -90.0;
        double southPoleLon = 0.0;

        // When
        double distance = GeoUtils.haversineKm(northPoleLat, northPoleLon, southPoleLat, southPoleLon);

        // Then - distance should be approximately half Earth's circumference (~20,000 km)
        assertThat(distance).isCloseTo(20015.0, within(100.0));
    }

    @Test
    @DisplayName("Should return always positive distance")
    void haversineKm_AlwaysPositive() {
        // Given
        double lat1 = 10.776889;
        double lon1 = 106.700806;
        double lat2 = -10.776889;
        double lon2 = -106.700806;

        // When
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);

        // Then
        assertThat(distance).isPositive();
    }
}

