package com.example.booking.dto.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for AISearchRequest
 */
@DisplayName("AISearchRequest Tests")
public class AISearchRequestTest {

    private AISearchRequest request;

    @BeforeEach
    void setUp() {
        request = new AISearchRequest();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Default constructor should create instance with default values")
    void defaultConstructor_shouldCreateInstanceWithDefaults() {
        // When
        AISearchRequest newRequest = new AISearchRequest();

        // Then
        assertNotNull(newRequest);
        assertEquals("web", newRequest.getSource());
        assertEquals("vi", newRequest.getLanguage());
        assertEquals(5, newRequest.getMaxResults());
        assertTrue(newRequest.getIncludeContext());
        assertTrue(newRequest.getEnableLearning());
    }

    @Test
    @DisplayName("Constructor with query should set query")
    void constructorWithQuery_shouldSetQuery() {
        // Given
        String query = "Find Italian restaurants";

        // When
        AISearchRequest newRequest = new AISearchRequest(query);

        // Then
        assertEquals(query, newRequest.getQuery());
    }

    // ========== Query Tests ==========

    @Test
    @DisplayName("Should set and get query successfully")
    void shouldSetAndGetQuery_successfully() {
        // Given
        String query = "Find Italian restaurants";

        // When
        request.setQuery(query);

        // Then
        assertEquals(query, request.getQuery());
    }

    @Test
    @DisplayName("Should handle null query")
    void shouldHandleNullQuery() {
        // When
        request.setQuery(null);

        // Then
        assertNull(request.getQuery());
    }

    @Test
    @DisplayName("Should handle empty query")
    void shouldHandleEmptyQuery() {
        // When
        request.setQuery("");

        // Then
        assertEquals("", request.getQuery());
    }

    @Test
    @DisplayName("Should handle long query")
    void shouldHandleLongQuery() {
        // Given
        String longQuery = "A".repeat(500);

        // When
        request.setQuery(longQuery);

        // Then
        assertEquals(longQuery, request.getQuery());
        assertEquals(500, request.getQuery().length());
    }

    // ========== Session and User Tests ==========

    @Test
    @DisplayName("Should set and get sessionId")
    void shouldSetAndGetSessionId() {
        // Given
        String sessionId = "session-123";

        // When
        request.setSessionId(sessionId);

        // Then
        assertEquals(sessionId, request.getSessionId());
    }

    @Test
    @DisplayName("Should set and get userId")
    void shouldSetAndGetUserId() {
        // Given
        String userId = "user-456";

        // When
        request.setUserId(userId);

        // Then
        assertEquals(userId, request.getUserId());
    }

    // ========== Source Tests ==========

    @Test
    @DisplayName("Should set and get source")
    void shouldSetAndGetSource() {
        // Given
        String source = "mobile";

        // When
        request.setSource(source);

        // Then
        assertEquals(source, request.getSource());
    }

    @Test
    @DisplayName("Should have default source as web")
    void shouldHaveDefaultSourceAsWeb() {
        // Then
        assertEquals("web", request.getSource());
    }

    // ========== Language Tests ==========

    @Test
    @DisplayName("Should set and get language")
    void shouldSetAndGetLanguage() {
        // Given
        String language = "en";

        // When
        request.setLanguage(language);

        // Then
        assertEquals(language, request.getLanguage());
    }

    @Test
    @DisplayName("Should have default language as vi")
    void shouldHaveDefaultLanguageAsVi() {
        // Then
        assertEquals("vi", request.getLanguage());
    }

    // ========== MaxResults Tests ==========

    @Test
    @DisplayName("Should set and get maxResults")
    void shouldSetAndGetMaxResults() {
        // Given
        Integer maxResults = 10;

        // When
        request.setMaxResults(maxResults);

        // Then
        assertEquals(maxResults, request.getMaxResults());
    }

    @Test
    @DisplayName("Should have default maxResults as 5")
    void shouldHaveDefaultMaxResultsAsFive() {
        // Then
        assertEquals(5, request.getMaxResults());
    }

    // ========== Boolean Flags Tests ==========

    @Test
    @DisplayName("Should set and get includeContext")
    void shouldSetAndGetIncludeContext() {
        // When
        request.setIncludeContext(false);

        // Then
        assertFalse(request.getIncludeContext());
    }

    @Test
    @DisplayName("Should have default includeContext as true")
    void shouldHaveDefaultIncludeContextAsTrue() {
        // Then
        assertTrue(request.getIncludeContext());
    }

    @Test
    @DisplayName("Should set and get enableLearning")
    void shouldSetAndGetEnableLearning() {
        // When
        request.setEnableLearning(false);

        // Then
        assertFalse(request.getEnableLearning());
    }

    @Test
    @DisplayName("Should have default enableLearning as true")
    void shouldHaveDefaultEnableLearningAsTrue() {
        // Then
        assertTrue(request.getEnableLearning());
    }

    // ========== Context Data Tests ==========

    @Test
    @DisplayName("Should set and get userLocation")
    void shouldSetAndGetUserLocation() {
        // Given
        String location = "Ho Chi Minh City";

        // When
        request.setUserLocation(location);

        // Then
        assertEquals(location, request.getUserLocation());
    }

    @Test
    @DisplayName("Should set and get userTimezone")
    void shouldSetAndGetUserTimezone() {
        // Given
        String timezone = "Asia/Ho_Chi_Minh";

        // When
        request.setUserTimezone(timezone);

        // Then
        assertEquals(timezone, request.getUserTimezone());
    }

    @Test
    @DisplayName("Should set and get deviceType")
    void shouldSetAndGetDeviceType() {
        // Given
        String deviceType = "smartphone";

        // When
        request.setDeviceType(deviceType);

        // Then
        assertEquals(deviceType, request.getDeviceType());
    }

    // ========== Preferences Tests ==========

    @Test
    @DisplayName("Should set and get preferredCuisines")
    void shouldSetAndGetPreferredCuisines() {
        // Given
        List<String> cuisines = Arrays.asList("Italian", "Japanese", "Vietnamese");

        // When
        request.setPreferredCuisines(cuisines);

        // Then
        assertEquals(cuisines, request.getPreferredCuisines());
        assertEquals(3, request.getPreferredCuisines().size());
    }

    @Test
    @DisplayName("Should handle null preferredCuisines")
    void shouldHandleNullPreferredCuisines() {
        // When
        request.setPreferredCuisines(null);

        // Then
        assertNull(request.getPreferredCuisines());
    }

    @Test
    @DisplayName("Should set and get minPrice")
    void shouldSetAndGetMinPrice() {
        // Given
        Integer minPrice = 100000;

        // When
        request.setMinPrice(minPrice);

        // Then
        assertEquals(minPrice, request.getMinPrice());
    }

    @Test
    @DisplayName("Should set and get maxPrice")
    void shouldSetAndGetMaxPrice() {
        // Given
        Integer maxPrice = 500000;

        // When
        request.setMaxPrice(maxPrice);

        // Then
        assertEquals(maxPrice, request.getMaxPrice());
    }

    @Test
    @DisplayName("Should set and get maxDistance")
    void shouldSetAndGetMaxDistance() {
        // Given
        Integer maxDistance = 5;

        // When
        request.setMaxDistance(maxDistance);

        // Then
        assertEquals(maxDistance, request.getMaxDistance());
    }

    @Test
    @DisplayName("Should set and get preferredDistricts")
    void shouldSetAndGetPreferredDistricts() {
        // Given
        List<String> districts = Arrays.asList("District 1", "District 3", "Binh Thanh");

        // When
        request.setPreferredDistricts(districts);

        // Then
        assertEquals(districts, request.getPreferredDistricts());
        assertEquals(3, request.getPreferredDistricts().size());
    }

    @Test
    @DisplayName("Should handle null preferredDistricts")
    void shouldHandleNullPreferredDistricts() {
        // When
        request.setPreferredDistricts(null);

        // Then
        assertNull(request.getPreferredDistricts());
    }

    // ========== Complex Scenarios ==========

    @Test
    @DisplayName("Should handle complete request with all fields")
    void shouldHandleCompleteRequestWithAllFields() {
        // Given & When
        request.setQuery("Find sushi restaurant");
        request.setSessionId("session-123");
        request.setUserId("user-456");
        request.setSource("mobile");
        request.setLanguage("en");
        request.setMaxResults(10);
        request.setIncludeContext(true);
        request.setEnableLearning(true);
        request.setUserLocation("Ho Chi Minh City");
        request.setUserTimezone("Asia/Ho_Chi_Minh");
        request.setDeviceType("smartphone");
        request.setPreferredCuisines(Arrays.asList("Japanese"));
        request.setMinPrice(200000);
        request.setMaxPrice(500000);
        request.setMaxDistance(5);
        request.setPreferredDistricts(Arrays.asList("District 1"));

        // Then
        assertEquals("Find sushi restaurant", request.getQuery());
        assertEquals("session-123", request.getSessionId());
        assertEquals("user-456", request.getUserId());
        assertEquals("mobile", request.getSource());
        assertEquals("en", request.getLanguage());
        assertEquals(10, request.getMaxResults());
        assertTrue(request.getIncludeContext());
        assertTrue(request.getEnableLearning());
        assertEquals("Ho Chi Minh City", request.getUserLocation());
        assertEquals("Asia/Ho_Chi_Minh", request.getUserTimezone());
        assertEquals("smartphone", request.getDeviceType());
        assertEquals(1, request.getPreferredCuisines().size());
        assertEquals(200000, request.getMinPrice());
        assertEquals(500000, request.getMaxPrice());
        assertEquals(5, request.getMaxDistance());
        assertEquals(1, request.getPreferredDistricts().size());
    }

    @Test
    @DisplayName("Should handle minimal request with only required fields")
    void shouldHandleMinimalRequestWithOnlyRequiredFields() {
        // Given & When
        AISearchRequest minimalRequest = new AISearchRequest("Find pizza");

        // Then
        assertEquals("Find pizza", minimalRequest.getQuery());
        assertNotNull(minimalRequest.getSource());
        assertNotNull(minimalRequest.getLanguage());
        assertNotNull(minimalRequest.getMaxResults());
    }

    @Test
    @DisplayName("Should handle empty lists")
    void shouldHandleEmptyLists() {
        // Given
        List<String> emptyList = Arrays.asList();

        // When
        request.setPreferredCuisines(emptyList);
        request.setPreferredDistricts(emptyList);

        // Then
        assertTrue(request.getPreferredCuisines().isEmpty());
        assertTrue(request.getPreferredDistricts().isEmpty());
    }

    @Test
    @DisplayName("Should handle price range scenarios")
    void shouldHandlePriceRangeScenarios() {
        // Scenario 1: Both min and max
        request.setMinPrice(100000);
        request.setMaxPrice(500000);
        assertEquals(100000, request.getMinPrice());
        assertEquals(500000, request.getMaxPrice());

        // Scenario 2: Only min
        AISearchRequest request2 = new AISearchRequest();
        request2.setMinPrice(100000);
        assertEquals(100000, request2.getMinPrice());
        assertNull(request2.getMaxPrice());

        // Scenario 3: Only max
        AISearchRequest request3 = new AISearchRequest();
        request3.setMaxPrice(500000);
        assertNull(request3.getMinPrice());
        assertEquals(500000, request3.getMaxPrice());
    }

    @Test
    @DisplayName("Should handle different source types")
    void shouldHandleDifferentSourceTypes() {
        // Web
        request.setSource("web");
        assertEquals("web", request.getSource());

        // Mobile
        request.setSource("mobile");
        assertEquals("mobile", request.getSource());

        // API
        request.setSource("api");
        assertEquals("api", request.getSource());

        // Widget
        request.setSource("widget");
        assertEquals("widget", request.getSource());
    }

    @Test
    @DisplayName("Should handle different languages")
    void shouldHandleDifferentLanguages() {
        // Vietnamese
        request.setLanguage("vi");
        assertEquals("vi", request.getLanguage());

        // English
        request.setLanguage("en");
        assertEquals("en", request.getLanguage());

        // Other languages
        request.setLanguage("ja");
        assertEquals("ja", request.getLanguage());
    }

    @Test
    @DisplayName("Should handle various maxResults values")
    void shouldHandleVariousMaxResultsValues() {
        // Small
        request.setMaxResults(1);
        assertEquals(1, request.getMaxResults());

        // Medium
        request.setMaxResults(5);
        assertEquals(5, request.getMaxResults());

        // Large
        request.setMaxResults(20);
        assertEquals(20, request.getMaxResults());

        // Null
        request.setMaxResults(null);
        assertNull(request.getMaxResults());
    }
}
