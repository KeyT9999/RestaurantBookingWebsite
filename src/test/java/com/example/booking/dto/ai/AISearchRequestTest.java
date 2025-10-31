package com.example.booking.dto.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class AISearchRequestTest {

    @Test
    void constructorShouldPopulateDefaults() {
        AISearchRequest request = new AISearchRequest("pho near me");

        assertEquals("pho near me", request.getQuery());
        assertEquals("web", request.getSource());
        assertEquals("vi", request.getLanguage());
        assertEquals(5, request.getMaxResults());
        assertTrue(request.getIncludeContext());
        assertTrue(request.getEnableLearning());
        assertNull(request.getSessionId());
    }

    @Test
    void settersShouldOverrideValues() {
        AISearchRequest request = new AISearchRequest();
        request.setQuery("seafood buffet");
        request.setSessionId("session-123");
        request.setUserId("user-456");
        request.setSource("mobile");
        request.setLanguage("en");
        request.setMaxResults(10);
        request.setIncludeContext(false);
        request.setEnableLearning(false);
        request.setUserLocation("Da Nang");
        request.setUserTimezone("Asia/Ho_Chi_Minh");
        request.setDeviceType("ios");
        request.setPreferredCuisines(List.of("seafood", "bbq"));
        request.setMinPrice(100000);
        request.setMaxPrice(500000);
        request.setMaxDistance(5);
        request.setPreferredDistricts(List.of("Hai Chau", "Son Tra"));

        assertEquals("seafood buffet", request.getQuery());
        assertEquals("session-123", request.getSessionId());
        assertEquals("user-456", request.getUserId());
        assertEquals("mobile", request.getSource());
        assertEquals("en", request.getLanguage());
        assertEquals(10, request.getMaxResults());
        assertEquals(false, request.getIncludeContext());
        assertEquals(false, request.getEnableLearning());
        assertEquals("Da Nang", request.getUserLocation());
        assertEquals("Asia/Ho_Chi_Minh", request.getUserTimezone());
        assertEquals("ios", request.getDeviceType());
        assertEquals(List.of("seafood", "bbq"), request.getPreferredCuisines());
        assertEquals(100000, request.getMinPrice());
        assertEquals(500000, request.getMaxPrice());
        assertEquals(5, request.getMaxDistance());
        assertEquals(List.of("Hai Chau", "Son Tra"), request.getPreferredDistricts());
    }
}

