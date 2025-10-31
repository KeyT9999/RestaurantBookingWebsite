package com.example.booking.dto.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for RestaurantRecommendation
 */
@DisplayName("RestaurantRecommendation Tests")
public class RestaurantRecommendationTest {

    private RestaurantRecommendation recommendation;

    @BeforeEach
    void setUp() {
        recommendation = new RestaurantRecommendation();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Default constructor should create empty instance")
    void defaultConstructor_shouldCreateEmptyInstance() {
        // When
        RestaurantRecommendation rec = new RestaurantRecommendation();

        // Then
        assertNotNull(rec);
        assertNull(rec.getRestaurantId());
        assertNull(rec.getRestaurantName());
    }

    @Test
    @DisplayName("Constructor with ID and name should set fields")
    void constructorWithIdAndName_shouldSetFields() {
        // When
        RestaurantRecommendation rec = new RestaurantRecommendation(1, "Test Restaurant");

        // Then
        assertEquals(1, rec.getRestaurantId());
        assertEquals("Test Restaurant", rec.getRestaurantName());
    }

    // ========== Basic Info Tests ==========

    @Test
    @DisplayName("Should set and get restaurantId")
    void shouldSetAndGetRestaurantId() {
        // When
        recommendation.setRestaurantId(1);

        // Then
        assertEquals(1, recommendation.getRestaurantId());
    }

    @Test
    @DisplayName("Should set and get restaurantName")
    void shouldSetAndGetRestaurantName() {
        // When
        recommendation.setRestaurantName("Test Restaurant");

        // Then
        assertEquals("Test Restaurant", recommendation.getRestaurantName());
    }

    @Test
    @DisplayName("Should set and get restaurantAddress")
    void shouldSetAndGetRestaurantAddress() {
        // When
        recommendation.setRestaurantAddress("123 Test St");

        // Then
        assertEquals("123 Test St", recommendation.getRestaurantAddress());
    }

    @Test
    @DisplayName("Should set and get restaurantPhone")
    void shouldSetAndGetRestaurantPhone() {
        // When
        recommendation.setRestaurantPhone("0901234567");

        // Then
        assertEquals("0901234567", recommendation.getRestaurantPhone());
    }

    @Test
    @DisplayName("Should set and get cuisineType")
    void shouldSetAndGetCuisineType() {
        // When
        recommendation.setCuisineType("Italian");

        // Then
        assertEquals("Italian", recommendation.getCuisineType());
    }

    @Test
    @DisplayName("Should set and get description")
    void shouldSetAndGetDescription() {
        // When
        recommendation.setDescription("A wonderful restaurant");

        // Then
        assertEquals("A wonderful restaurant", recommendation.getDescription());
    }

    @Test
    @DisplayName("Should set and get imageUrl")
    void shouldSetAndGetImageUrl() {
        // When
        recommendation.setImageUrl("http://example.com/image.jpg");

        // Then
        assertEquals("http://example.com/image.jpg", recommendation.getImageUrl());
    }

    @Test
    @DisplayName("Should set and get websiteUrl")
    void shouldSetAndGetWebsiteUrl() {
        // When
        recommendation.setWebsiteUrl("http://example.com");

        // Then
        assertEquals("http://example.com", recommendation.getWebsiteUrl());
    }

    // ========== Ratings Tests ==========

    @Test
    @DisplayName("Should set and get rating")
    void shouldSetAndGetRating() {
        // When
        recommendation.setRating(new BigDecimal("4.5"));

        // Then
        assertEquals(new BigDecimal("4.5"), recommendation.getRating());
    }

    @Test
    @DisplayName("Should set and get reviewCount")
    void shouldSetAndGetReviewCount() {
        // When
        recommendation.setReviewCount(100);

        // Then
        assertEquals(100, recommendation.getReviewCount());
    }

    @Test
    @DisplayName("Should set and get recentReview")
    void shouldSetAndGetRecentReview() {
        // When
        recommendation.setRecentReview("Great food!");

        // Then
        assertEquals("Great food!", recommendation.getRecentReview());
    }

    // ========== Pricing Tests ==========

    @Test
    @DisplayName("Should set and get priceRange")
    void shouldSetAndGetPriceRange() {
        // When
        recommendation.setPriceRange("200k - 500k");

        // Then
        assertEquals("200k - 500k", recommendation.getPriceRange());
    }

    @Test
    @DisplayName("Should set and get minPrice")
    void shouldSetAndGetMinPrice() {
        // When
        recommendation.setMinPrice(new BigDecimal("200000"));

        // Then
        assertEquals(new BigDecimal("200000"), recommendation.getMinPrice());
    }

    @Test
    @DisplayName("Should set and get maxPrice")
    void shouldSetAndGetMaxPrice() {
        // When
        recommendation.setMaxPrice(new BigDecimal("500000"));

        // Then
        assertEquals(new BigDecimal("500000"), recommendation.getMaxPrice());
    }

    @Test
    @DisplayName("Should set and get priceLevel")
    void shouldSetAndGetPriceLevel() {
        // When
        recommendation.setPriceLevel("$$");

        // Then
        assertEquals("$$", recommendation.getPriceLevel());
    }

    // ========== Location Tests ==========

    @Test
    @DisplayName("Should set and get distanceKm")
    void shouldSetAndGetDistanceKm() {
        // When
        recommendation.setDistanceKm(2.5);

        // Then
        assertEquals(2.5, recommendation.getDistanceKm());
    }

    @Test
    @DisplayName("Should set and get district")
    void shouldSetAndGetDistrict() {
        // When
        recommendation.setDistrict("District 1");

        // Then
        assertEquals("District 1", recommendation.getDistrict());
    }

    @Test
    @DisplayName("Should set and get city")
    void shouldSetAndGetCity() {
        // When
        recommendation.setCity("Ho Chi Minh City");

        // Then
        assertEquals("Ho Chi Minh City", recommendation.getCity());
    }

    // ========== Availability Tests ==========

    @Test
    @DisplayName("Should set and get isAvailable")
    void shouldSetAndGetIsAvailable() {
        // When
        recommendation.setIsAvailable(true);

        // Then
        assertTrue(recommendation.getIsAvailable());
    }

    @Test
    @DisplayName("Should set and get nextAvailableTime")
    void shouldSetAndGetNextAvailableTime() {
        // When
        recommendation.setNextAvailableTime("18:00");

        // Then
        assertEquals("18:00", recommendation.getNextAvailableTime());
    }

    @Test
    @DisplayName("Should set and get availableTables")
    void shouldSetAndGetAvailableTables() {
        // When
        recommendation.setAvailableTables(5);

        // Then
        assertEquals(5, recommendation.getAvailableTables());
    }

    @Test
    @DisplayName("Should set and get availabilityStatus")
    void shouldSetAndGetAvailabilityStatus() {
        // When
        recommendation.setAvailabilityStatus("Available");

        // Then
        assertEquals("Available", recommendation.getAvailabilityStatus());
    }

    // ========== AI-Specific Data Tests ==========

    @Test
    @DisplayName("Should set and get confidenceScore")
    void shouldSetAndGetConfidenceScore() {
        // When
        recommendation.setConfidenceScore(new BigDecimal("0.95"));

        // Then
        assertEquals(new BigDecimal("0.95"), recommendation.getConfidenceScore());
    }

    @Test
    @DisplayName("Should set and get aiExplanation")
    void shouldSetAndGetAiExplanation() {
        // When
        recommendation.setAiExplanation("Great food and service");

        // Then
        assertEquals("Great food and service", recommendation.getAiExplanation());
    }

    @Test
    @DisplayName("Should set and get matchingFactors")
    void shouldSetAndGetMatchingFactors() {
        // Given
        List<String> factors = Arrays.asList("cuisine", "price", "distance");

        // When
        recommendation.setMatchingFactors(factors);

        // Then
        assertEquals(factors, recommendation.getMatchingFactors());
        assertEquals(3, recommendation.getMatchingFactors().size());
    }

    @Test
    @DisplayName("Should set and get whyRecommended")
    void shouldSetAndGetWhyRecommended() {
        // Given
        List<String> reasons = Arrays.asList("High rating", "Near you", "Popular");

        // When
        recommendation.setWhyRecommended(reasons);

        // Then
        assertEquals(reasons, recommendation.getWhyRecommended());
        assertEquals(3, recommendation.getWhyRecommended().size());
    }

    // ========== Actions Tests ==========

    @Test
    @DisplayName("Should set and get bookingUrl")
    void shouldSetAndGetBookingUrl() {
        // When
        recommendation.setBookingUrl("http://example.com/book");

        // Then
        assertEquals("http://example.com/book", recommendation.getBookingUrl());
    }

    @Test
    @DisplayName("Should set and get viewDetailsUrl")
    void shouldSetAndGetViewDetailsUrl() {
        // When
        recommendation.setViewDetailsUrl("http://example.com/details");

        // Then
        assertEquals("http://example.com/details", recommendation.getViewDetailsUrl());
    }

    @Test
    @DisplayName("Should set and get saveToFavoritesUrl")
    void shouldSetAndGetSaveToFavoritesUrl() {
        // When
        recommendation.setSaveToFavoritesUrl("http://example.com/favorites");

        // Then
        assertEquals("http://example.com/favorites", recommendation.getSaveToFavoritesUrl());
    }

    @Test
    @DisplayName("Should set and get shareUrl")
    void shouldSetAndGetShareUrl() {
        // When
        recommendation.setShareUrl("http://example.com/share");

        // Then
        assertEquals("http://example.com/share", recommendation.getShareUrl());
    }

    // ========== Special Features Tests ==========

    @Test
    @DisplayName("Should set and get specialFeatures")
    void shouldSetAndGetSpecialFeatures() {
        // Given
        List<String> features = Arrays.asList("parking", "wifi", "wheelchair_accessible");

        // When
        recommendation.setSpecialFeatures(features);

        // Then
        assertEquals(features, recommendation.getSpecialFeatures());
        assertEquals(3, recommendation.getSpecialFeatures().size());
    }

    @Test
    @DisplayName("Should set and get ambiance")
    void shouldSetAndGetAmbiance() {
        // Given
        List<String> ambiance = Arrays.asList("romantic", "casual", "family-friendly");

        // When
        recommendation.setAmbiance(ambiance);

        // Then
        assertEquals(ambiance, recommendation.getAmbiance());
        assertEquals(3, recommendation.getAmbiance().size());
    }

    @Test
    @DisplayName("Should set and get openingHours")
    void shouldSetAndGetOpeningHours() {
        // When
        recommendation.setOpeningHours("10:00 - 22:00");

        // Then
        assertEquals("10:00 - 22:00", recommendation.getOpeningHours());
    }

    @Test
    @DisplayName("Should set and get lastOrderTime")
    void shouldSetAndGetLastOrderTime() {
        // When
        recommendation.setLastOrderTime("21:30");

        // Then
        assertEquals("21:30", recommendation.getLastOrderTime());
    }

    // ========== Helper Methods Tests ==========

    @Test
    @DisplayName("isHighConfidence should return true when score >= 0.8")
    void isHighConfidence_shouldReturnTrueWhenScoreHighEnough() {
        // Given
        recommendation.setConfidenceScore(new BigDecimal("0.85"));

        // When & Then
        assertTrue(recommendation.isHighConfidence());
    }

    @Test
    @DisplayName("isHighConfidence should return false when score < 0.8")
    void isHighConfidence_shouldReturnFalseWhenScoreLow() {
        // Given
        recommendation.setConfidenceScore(new BigDecimal("0.75"));

        // When & Then
        assertFalse(recommendation.isHighConfidence());
    }

    @Test
    @DisplayName("isHighConfidence should return false when score is null")
    void isHighConfidence_shouldReturnFalseWhenScoreNull() {
        // Given
        recommendation.setConfidenceScore(null);

        // When & Then
        assertFalse(recommendation.isHighConfidence());
    }

    @Test
    @DisplayName("isNearby should return true when distance <= 2.0")
    void isNearby_shouldReturnTrueWhenDistanceNear() {
        // Given
        recommendation.setDistanceKm(1.5);

        // When & Then
        assertTrue(recommendation.isNearby());
    }

    @Test
    @DisplayName("isNearby should return true when distance exactly 2.0")
    void isNearby_shouldReturnTrueWhenDistanceExactly2() {
        // Given
        recommendation.setDistanceKm(2.0);

        // When & Then
        assertTrue(recommendation.isNearby());
    }

    @Test
    @DisplayName("isNearby should return false when distance > 2.0")
    void isNearby_shouldReturnFalseWhenDistanceFar() {
        // Given
        recommendation.setDistanceKm(3.0);

        // When & Then
        assertFalse(recommendation.isNearby());
    }

    @Test
    @DisplayName("isNearby should return false when distance is null")
    void isNearby_shouldReturnFalseWhenDistanceNull() {
        // Given
        recommendation.setDistanceKm(null);

        // When & Then
        assertFalse(recommendation.isNearby());
    }

    @Test
    @DisplayName("isAffordable should return true when maxPrice <= 300000")
    void isAffordable_shouldReturnTrueWhenPriceAffordable() {
        // Given
        recommendation.setMaxPrice(new BigDecimal("250000"));

        // When & Then
        assertTrue(recommendation.isAffordable());
    }

    @Test
    @DisplayName("isAffordable should return true when maxPrice exactly 300000")
    void isAffordable_shouldReturnTrueWhenPriceExactly300000() {
        // Given
        recommendation.setMaxPrice(new BigDecimal("300000"));

        // When & Then
        assertTrue(recommendation.isAffordable());
    }

    @Test
    @DisplayName("isAffordable should return false when maxPrice > 300000")
    void isAffordable_shouldReturnFalseWhenPriceExpensive() {
        // Given
        recommendation.setMaxPrice(new BigDecimal("500000"));

        // When & Then
        assertFalse(recommendation.isAffordable());
    }

    @Test
    @DisplayName("isAffordable should return false when maxPrice is null")
    void isAffordable_shouldReturnFalseWhenMaxPriceNull() {
        // Given
        recommendation.setMaxPrice(null);

        // When & Then
        assertFalse(recommendation.isAffordable());
    }

    @Test
    @DisplayName("isHighlyRated should return true when rating >= 4.5")
    void isHighlyRated_shouldReturnTrueWhenRatingHigh() {
        // Given
        recommendation.setRating(new BigDecimal("4.7"));

        // When & Then
        assertTrue(recommendation.isHighlyRated());
    }

    @Test
    @DisplayName("isHighlyRated should return true when rating exactly 4.5")
    void isHighlyRated_shouldReturnTrueWhenRatingExactly45() {
        // Given
        recommendation.setRating(new BigDecimal("4.5"));

        // When & Then
        assertTrue(recommendation.isHighlyRated());
    }

    @Test
    @DisplayName("isHighlyRated should return false when rating < 4.5")
    void isHighlyRated_shouldReturnFalseWhenRatingLow() {
        // Given
        recommendation.setRating(new BigDecimal("4.0"));

        // When & Then
        assertFalse(recommendation.isHighlyRated());
    }

    @Test
    @DisplayName("isHighlyRated should return false when rating is null")
    void isHighlyRated_shouldReturnFalseWhenRatingNull() {
        // Given
        recommendation.setRating(null);

        // When & Then
        assertFalse(recommendation.isHighlyRated());
    }

    @Test
    @DisplayName("getFormattedDistance should return meters when < 1km")
    void getFormattedDistance_shouldReturnMetersWhenLessThan1Km() {
        // Given
        recommendation.setDistanceKm(0.5);

        // When
        String formatted = recommendation.getFormattedDistance();

        // Then
        assertEquals("500m", formatted);
    }

    @Test
    @DisplayName("getFormattedDistance should return km when >= 1km")
    void getFormattedDistance_shouldReturnKmWhenGreaterOrEqual1Km() {
        // Given
        recommendation.setDistanceKm(2.5);

        // When
        String formatted = recommendation.getFormattedDistance();

        // Then
        assertEquals("2.5km", formatted);
    }

    @Test
    @DisplayName("getFormattedDistance should return Unknown when distance is null")
    void getFormattedDistance_shouldReturnUnknownWhenDistanceNull() {
        // Given
        recommendation.setDistanceKm(null);

        // When
        String formatted = recommendation.getFormattedDistance();

        // Then
        assertEquals("Unknown", formatted);
    }

    @Test
    @DisplayName("getFormattedPrice should return formatted price when prices set")
    void getFormattedPrice_shouldReturnFormattedPriceWhenPricesSet() {
        // Given
        recommendation.setMinPrice(new BigDecimal("200000"));
        recommendation.setMaxPrice(new BigDecimal("500000"));

        // When
        String formatted = recommendation.getFormattedPrice();

        // Then
        assertEquals("200k - 500k VNĐ", formatted);
    }

    @Test
    @DisplayName("getFormattedPrice should return priceRange when prices null")
    void getFormattedPrice_shouldReturnPriceRangeWhenPricesNull() {
        // Given
        recommendation.setPriceRange("$$");
        recommendation.setMinPrice(null);
        recommendation.setMaxPrice(null);

        // When
        String formatted = recommendation.getFormattedPrice();

        // Then
        assertEquals("$$", formatted);
    }

    @Test
    @DisplayName("getFormattedPrice should return null when all price fields null")
    void getFormattedPrice_shouldReturnNullWhenAllPriceFieldsNull() {
        // Given
        recommendation.setPriceRange(null);
        recommendation.setMinPrice(null);
        recommendation.setMaxPrice(null);

        // When
        String formatted = recommendation.getFormattedPrice();

        // Then
        assertNull(formatted);
    }

    @Test
    @DisplayName("getFormattedPrice should return priceRange when only minPrice set")
    void getFormattedPrice_shouldReturnPriceRangeWhenOnlyMinPriceSet() {
        // Given
        recommendation.setPriceRange("$$$");
        recommendation.setMinPrice(new BigDecimal("200000"));
        recommendation.setMaxPrice(null);

        // When
        String formatted = recommendation.getFormattedPrice();

        // Then
        assertEquals("$$$", formatted);
    }

    @Test
    @DisplayName("getFormattedPrice should return priceRange when only maxPrice set")
    void getFormattedPrice_shouldReturnPriceRangeWhenOnlyMaxPriceSet() {
        // Given
        recommendation.setPriceRange("$$");
        recommendation.setMinPrice(null);
        recommendation.setMaxPrice(new BigDecimal("500000"));

        // When
        String formatted = recommendation.getFormattedPrice();

        // Then
        assertEquals("$$", formatted);
    }

    // ========== Complex Scenarios ==========

    @Test
    @DisplayName("Should handle complete recommendation with all fields")
    void shouldHandleCompleteRecommendationWithAllFields() {
        // When
        recommendation.setRestaurantId(1);
        recommendation.setRestaurantName("Test Restaurant");
        recommendation.setRestaurantAddress("123 Test St");
        recommendation.setRestaurantPhone("0901234567");
        recommendation.setCuisineType("Italian");
        recommendation.setDescription("A wonderful place");
        recommendation.setImageUrl("http://example.com/image.jpg");
        recommendation.setWebsiteUrl("http://example.com");
        recommendation.setRating(new BigDecimal("4.5"));
        recommendation.setReviewCount(100);
        recommendation.setRecentReview("Great!");
        recommendation.setPriceRange("$$");
        recommendation.setMinPrice(new BigDecimal("100000"));
        recommendation.setMaxPrice(new BigDecimal("250000"));
        recommendation.setPriceLevel("$$");
        recommendation.setDistanceKm(1.5);
        recommendation.setDistrict("District 1");
        recommendation.setCity("Ho Chi Minh City");
        recommendation.setIsAvailable(true);
        recommendation.setNextAvailableTime("18:00");
        recommendation.setAvailableTables(5);
        recommendation.setAvailabilityStatus("Available");
        recommendation.setConfidenceScore(new BigDecimal("0.95"));
        recommendation.setAiExplanation("Perfect match");
        recommendation.setMatchingFactors(Arrays.asList("cuisine", "price"));
        recommendation.setWhyRecommended(Arrays.asList("High rating", "Near you"));
        recommendation.setBookingUrl("http://example.com/book");
        recommendation.setViewDetailsUrl("http://example.com/details");
        recommendation.setSaveToFavoritesUrl("http://example.com/favorites");
        recommendation.setShareUrl("http://example.com/share");
        recommendation.setSpecialFeatures(Arrays.asList("parking", "wifi"));
        recommendation.setAmbiance(Arrays.asList("romantic"));
        recommendation.setOpeningHours("10:00 - 22:00");
        recommendation.setLastOrderTime("21:30");

        // Then - verify all fields
        assertEquals(1, recommendation.getRestaurantId());
        assertEquals("Test Restaurant", recommendation.getRestaurantName());
        assertTrue(recommendation.isHighConfidence());
        assertTrue(recommendation.isNearby());
        assertTrue(recommendation.isAffordable());
        assertTrue(recommendation.isHighlyRated());
        assertEquals("1.5km", recommendation.getFormattedDistance());
        assertEquals("100k - 250k VNĐ", recommendation.getFormattedPrice());
    }

    @Test
    @DisplayName("Should handle edge cases for distance formatting")
    void shouldHandleEdgeCasesForDistanceFormatting() {
        // Very small distance
        recommendation.setDistanceKm(0.123);
        assertEquals("123m", recommendation.getFormattedDistance());

        // Exactly 1km
        recommendation.setDistanceKm(1.0);
        assertEquals("1.0km", recommendation.getFormattedDistance());

        // Large distance
        recommendation.setDistanceKm(10.5);
        assertEquals("10.5km", recommendation.getFormattedDistance());
    }

    @Test
    @DisplayName("Should handle all helper methods with null values")
    void shouldHandleAllHelperMethodsWithNullValues() {
        // Given - all null values
        recommendation.setConfidenceScore(null);
        recommendation.setDistanceKm(null);
        recommendation.setMaxPrice(null);
        recommendation.setRating(null);

        // Then
        assertFalse(recommendation.isHighConfidence());
        assertFalse(recommendation.isNearby());
        assertFalse(recommendation.isAffordable());
        assertFalse(recommendation.isHighlyRated());
        assertEquals("Unknown", recommendation.getFormattedDistance());
    }
}
