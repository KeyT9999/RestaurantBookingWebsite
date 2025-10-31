package com.example.booking.dto.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RestaurantRecommendationTest {

    @Test
    void shouldStoreAndReturnAllFields() {
        RestaurantRecommendation recommendation = new RestaurantRecommendation(1, "Pho 24");

        recommendation.setRestaurantAddress("123 Main St");
        recommendation.setRestaurantPhone("0123 456 789");
        recommendation.setCuisineType("Vietnamese");
        recommendation.setDescription("Authentic Pho");
        recommendation.setImageUrl("http://example.com/image.jpg");
        recommendation.setWebsiteUrl("http://example.com");
        recommendation.setRating(new BigDecimal("4.7"));
        recommendation.setReviewCount(120);
        recommendation.setRecentReview("Great taste");
        recommendation.setPriceRange("100k - 200k");
        recommendation.setMinPrice(new BigDecimal("150000"));
        recommendation.setMaxPrice(new BigDecimal("250000"));
        recommendation.setPriceLevel("$$");
        recommendation.setDistanceKm(1.2);
        recommendation.setDistrict("District 1");
        recommendation.setCity("Ho Chi Minh");
        recommendation.setIsAvailable(true);
        recommendation.setNextAvailableTime("18:00");
        recommendation.setAvailableTables(3);
        recommendation.setAvailabilityStatus("Limited");
        recommendation.setConfidenceScore(new BigDecimal("0.85"));
        recommendation.setAiExplanation("Matches your preferences");
        recommendation.setMatchingFactors(List.of("nearby", "pho"));
        recommendation.setWhyRecommended(List.of("High rating", "Popular"));
        recommendation.setBookingUrl("http://example.com/book");
        recommendation.setViewDetailsUrl("http://example.com/details");
        recommendation.setSaveToFavoritesUrl("http://example.com/save");
        recommendation.setShareUrl("http://example.com/share");
        recommendation.setSpecialFeatures(List.of("wifi", "parking"));
        recommendation.setAmbiance(List.of("romantic", "casual"));
        recommendation.setOpeningHours("08:00 - 22:00");
        recommendation.setLastOrderTime("21:30");

        assertEquals(1, recommendation.getRestaurantId());
        assertEquals("Pho 24", recommendation.getRestaurantName());
        assertEquals("123 Main St", recommendation.getRestaurantAddress());
        assertEquals("0123 456 789", recommendation.getRestaurantPhone());
        assertEquals("Vietnamese", recommendation.getCuisineType());
        assertEquals("Authentic Pho", recommendation.getDescription());
        assertEquals("http://example.com/image.jpg", recommendation.getImageUrl());
        assertEquals("http://example.com", recommendation.getWebsiteUrl());
        assertEquals(new BigDecimal("4.7"), recommendation.getRating());
        assertEquals(120, recommendation.getReviewCount());
        assertEquals("Great taste", recommendation.getRecentReview());
        assertEquals("100k - 200k", recommendation.getPriceRange());
        assertEquals(new BigDecimal("150000"), recommendation.getMinPrice());
        assertEquals(new BigDecimal("250000"), recommendation.getMaxPrice());
        assertEquals("$$", recommendation.getPriceLevel());
        assertEquals(1.2, recommendation.getDistanceKm());
        assertEquals("District 1", recommendation.getDistrict());
        assertEquals("Ho Chi Minh", recommendation.getCity());
        assertTrue(recommendation.getIsAvailable());
        assertEquals("18:00", recommendation.getNextAvailableTime());
        assertEquals(3, recommendation.getAvailableTables());
        assertEquals("Limited", recommendation.getAvailabilityStatus());
        assertEquals(new BigDecimal("0.85"), recommendation.getConfidenceScore());
        assertEquals("Matches your preferences", recommendation.getAiExplanation());
        assertEquals(List.of("nearby", "pho"), recommendation.getMatchingFactors());
        assertEquals(List.of("High rating", "Popular"), recommendation.getWhyRecommended());
        assertEquals("http://example.com/book", recommendation.getBookingUrl());
        assertEquals("http://example.com/details", recommendation.getViewDetailsUrl());
        assertEquals("http://example.com/save", recommendation.getSaveToFavoritesUrl());
        assertEquals("http://example.com/share", recommendation.getShareUrl());
        assertEquals(List.of("wifi", "parking"), recommendation.getSpecialFeatures());
        assertEquals(List.of("romantic", "casual"), recommendation.getAmbiance());
        assertEquals("08:00 - 22:00", recommendation.getOpeningHours());
        assertEquals("21:30", recommendation.getLastOrderTime());
    }

    @Test
    void helperMethodsShouldEvaluateBusinessLogic() {
        RestaurantRecommendation recommendation = new RestaurantRecommendation();
        recommendation.setConfidenceScore(new BigDecimal("0.81"));
        recommendation.setDistanceKm(1.5);
        recommendation.setMaxPrice(new BigDecimal("299999"));
        recommendation.setRating(new BigDecimal("4.6"));

        assertTrue(recommendation.isHighConfidence());
        assertTrue(recommendation.isNearby());
        assertTrue(recommendation.isAffordable());
        assertTrue(recommendation.isHighlyRated());

        recommendation.setConfidenceScore(new BigDecimal("0.5"));
        recommendation.setDistanceKm(5.0);
        recommendation.setMaxPrice(new BigDecimal("400000"));
        recommendation.setRating(new BigDecimal("4.0"));

        assertFalse(recommendation.isHighConfidence());
        assertFalse(recommendation.isNearby());
        assertFalse(recommendation.isAffordable());
        assertFalse(recommendation.isHighlyRated());
    }

    @Test
    void formattedHelpersShouldHandleEdgeCases() {
        RestaurantRecommendation recommendation = new RestaurantRecommendation();

        // Distance formatting
        assertEquals("Unknown", recommendation.getFormattedDistance());
        recommendation.setDistanceKm(0.5);
        assertEquals("500m", recommendation.getFormattedDistance());
        recommendation.setDistanceKm(2.345);
        assertEquals("2.3km", recommendation.getFormattedDistance());

        // Price formatting
        recommendation.setPriceRange("100k - 200k");
        assertEquals("100k - 200k", recommendation.getFormattedPrice());

        recommendation.setMinPrice(new BigDecimal("120000"));
        recommendation.setMaxPrice(new BigDecimal("240000"));
        assertEquals("120k - 240k VNĐ", recommendation.getFormattedPrice());
    }
}

