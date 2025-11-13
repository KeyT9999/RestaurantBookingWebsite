package com.example.booking.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Utilities: GeoUtils, CityGeoResolver, PayOSSignatureGenerator
 */
class UtilTest {

    private RestTemplate restTemplate;
    private CityGeoResolver cityGeoResolver;

    @BeforeEach
    void setUp() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
        this.cityGeoResolver = new CityGeoResolver(restTemplate);
    }

    // ========== GeoUtils Tests ==========
    @Test
    void testGeoUtils_HaversineKm_SameLocation() {
        double lat = 10.776889;
        double lon = 106.700806;
        
        double distance = GeoUtils.haversineKm(lat, lon, lat, lon);
        
        assertThat(distance).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.1));
    }

    @Test
    void testGeoUtils_HaversineKm_HoChiMinhToHanoi() {
        // Ho Chi Minh City coordinates
        double hcmLat = 10.776889;
        double hcmLon = 106.700806;
        
        // Hanoi coordinates
        double hanoiLat = 21.027763;
        double hanoiLon = 105.834160;
        
        double distance = GeoUtils.haversineKm(hcmLat, hcmLon, hanoiLat, hanoiLon);
        
        // Distance between HCM and Hanoi is approximately 1130 km
        assertThat(distance).isCloseTo(1130.0, org.assertj.core.data.Offset.offset(50.0));
    }

    @Test
    void testGeoUtils_HaversineKm_ShortDistance() {
        // Two points close together (about 1 km apart)
        double lat1 = 10.776889;
        double lon1 = 106.700806;
        double lat2 = 10.785889;
        double lon2 = 106.710806;
        
        double distance = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        
        // Should be approximately 1-2 km
        assertThat(distance).isBetween(0.5, 3.0);
    }

    @Test
    void testGeoUtils_HaversineKm_ReverseOrder() {
        double lat1 = 10.776889;
        double lon1 = 106.700806;
        double lat2 = 21.027763;
        double lon2 = 105.834160;
        
        double distance1 = GeoUtils.haversineKm(lat1, lon1, lat2, lon2);
        double distance2 = GeoUtils.haversineKm(lat2, lon2, lat1, lon1);
        
        // Distance should be the same regardless of order
        assertThat(distance1).isCloseTo(distance2, org.assertj.core.data.Offset.offset(0.1));
    }

    // ========== CityGeoResolver Tests ==========
    @Test
    void testCityGeoResolver_ResolveFromAddress_HoChiMinh() {
        CityGeoResolver.LatLng result = cityGeoResolver.resolveFromAddress("123 Nguyen Hue, Ho Chi Minh City");
        
        assertThat(result).isNotNull();
        assertThat(result.lat).isCloseTo(10.776889, org.assertj.core.data.Offset.offset(0.001));
        assertThat(result.lng).isCloseTo(106.700806, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_HCM() {
        CityGeoResolver.LatLng result = cityGeoResolver.resolveFromAddress("District 1, HCM");
        
        assertThat(result).isNotNull();
        assertThat(result.lat).isCloseTo(10.776889, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_Hanoi() {
        CityGeoResolver.LatLng result = cityGeoResolver.resolveFromAddress("Ha Noi, Vietnam");
        
        assertThat(result).isNotNull();
        assertThat(result.lat).isCloseTo(21.027763, org.assertj.core.data.Offset.offset(0.001));
        assertThat(result.lng).isCloseTo(105.834160, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_DaNang() {
        CityGeoResolver.LatLng result = cityGeoResolver.resolveFromAddress("Da Nang City");
        
        assertThat(result).isNotNull();
        assertThat(result.lat).isCloseTo(16.047079, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_HaiPhong() {
        CityGeoResolver.LatLng result = cityGeoResolver.resolveFromAddress("Hai Phong, Vietnam");
        
        assertThat(result).isNotNull();
        assertThat(result.lat).isCloseTo(20.844911, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_CanTho() {
        CityGeoResolver.LatLng result = cityGeoResolver.resolveFromAddress("Can Tho");
        
        assertThat(result).isNotNull();
        assertThat(result.lat).isCloseTo(10.045162, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_VietnameseAccents() {
        CityGeoResolver.LatLng result1 = cityGeoResolver.resolveFromAddress("Hồ Chí Minh");
        CityGeoResolver.LatLng result2 = cityGeoResolver.resolveFromAddress("Hà Nội");
        CityGeoResolver.LatLng result3 = cityGeoResolver.resolveFromAddress("Đà Nẵng");
        
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_Null() {
        CityGeoResolver.LatLng result = cityGeoResolver.resolveFromAddress(null);
        
        assertThat(result).isNull();
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_Blank() {
        CityGeoResolver.LatLng result1 = cityGeoResolver.resolveFromAddress("");
        CityGeoResolver.LatLng result2 = cityGeoResolver.resolveFromAddress("   ");
        CityGeoResolver.LatLng result3 = cityGeoResolver.resolveFromAddress("\t\n");
        
        assertThat(result1).isNull();
        assertThat(result2).isNull();
        assertThat(result3).isNull();
    }

    @Test
    void testCityGeoResolver_ResolveFromAddress_UnknownCity() {
        CityGeoResolver.LatLng result = cityGeoResolver.resolveFromAddress("Unknown City, Somewhere");
        
        assertThat(result).isNull();
    }

    @Test
    void testCityGeoResolver_LatLng_Constructor() {
        CityGeoResolver.LatLng latLng = new CityGeoResolver.LatLng(10.5, 106.7);
        
        assertThat(latLng.lat).isEqualTo(10.5);
        assertThat(latLng.lng).isEqualTo(106.7);
    }

    // ========== PayOSSignatureGenerator Tests ==========
    @Test
    void testPayOSSignatureGenerator_GenerateSignature_ValidInput() {
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "Test PayOS";
        String cancelUrl = "http://localhost:8080/payment/payos/cancel";
        String returnUrl = "http://localhost:8080/payment/payos/return";
        String checksumKey = "eb7485ce4c656e02cae0629fcce02b2933d5b2a9b0a10447c9aa0662125835cf";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, checksumKey
        );
        
        assertThat(signature).isNotNull();
        assertThat(signature).isNotEmpty();
        // HMAC-SHA256 produces 64-character hex string
        assertThat(signature.length()).isEqualTo(64);
        // Should only contain hex characters
        assertThat(signature).matches("^[0-9a-f]{64}$");
    }

    @Test
    void testPayOSSignatureGenerator_GenerateSignature_DifferentOrderCodes() {
        String checksumKey = "test-key-123";
        
        String sig1 = PayOSSignatureGenerator.generateSignature(
            100L, 20000L, "Test", "http://cancel", "http://return", checksumKey
        );
        
        String sig2 = PayOSSignatureGenerator.generateSignature(
            200L, 20000L, "Test", "http://cancel", "http://return", checksumKey
        );
        
        // Different order codes should produce different signatures
        assertThat(sig1).isNotEqualTo(sig2);
    }

    @Test
    void testPayOSSignatureGenerator_GenerateSignature_DifferentAmounts() {
        String checksumKey = "test-key-123";
        
        String sig1 = PayOSSignatureGenerator.generateSignature(
            100L, 10000L, "Test", "http://cancel", "http://return", checksumKey
        );
        
        String sig2 = PayOSSignatureGenerator.generateSignature(
            100L, 20000L, "Test", "http://cancel", "http://return", checksumKey
        );
        
        // Different amounts should produce different signatures
        assertThat(sig1).isNotEqualTo(sig2);
    }

    @Test
    void testPayOSSignatureGenerator_GenerateSignature_SameInput() {
        String checksumKey = "test-key-123";
        
        String sig1 = PayOSSignatureGenerator.generateSignature(
            100L, 20000L, "Test", "http://cancel", "http://return", checksumKey
        );
        
        String sig2 = PayOSSignatureGenerator.generateSignature(
            100L, 20000L, "Test", "http://cancel", "http://return", checksumKey
        );
        
        // Same input should produce same signature
        assertThat(sig1).isEqualTo(sig2);
    }

    @Test
    void testPayOSSignatureGenerator_GenerateSignature_EmptyDescription() {
        String checksumKey = "test-key-123";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            100L, 20000L, "", "http://cancel", "http://return", checksumKey
        );
        
        assertThat(signature).isNotNull();
        assertThat(signature.length()).isEqualTo(64);
    }

    @Test
    void testPayOSSignatureGenerator_GenerateSignature_WithSpecialCharacters() {
        String checksumKey = "test-key-123";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            100L, 20000L, "Test & Description", "http://cancel?param=value", 
            "http://return?param=value", checksumKey
        );
        
        assertThat(signature).isNotNull();
        assertThat(signature.length()).isEqualTo(64);
    }
}

