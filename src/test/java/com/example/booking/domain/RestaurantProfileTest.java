package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.RestaurantApprovalStatus;

/**
 * Unit tests for RestaurantProfile domain entity
 */
@DisplayName("RestaurantProfile Domain Entity Tests")
public class RestaurantProfileTest {

    private RestaurantProfile restaurant;
    private RestaurantOwner owner;

    @BeforeEach
    void setUp() {
        owner = new RestaurantOwner();
        owner.setUser(new User());

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRestaurantName_successfully")
    void shouldSetAndGetRestaurantName_successfully() {
        // Given
        String name = "New Restaurant Name";

        // When
        restaurant.setRestaurantName(name);

        // Then
        assertEquals(name, restaurant.getRestaurantName());
    }

    @Test
    @DisplayName("shouldSetAndGetOwner_successfully")
    void shouldSetAndGetOwner_successfully() {
        // When
        RestaurantOwner result = restaurant.getOwner();

        // Then
        assertNotNull(result);
        assertEquals(owner.getOwnerId(), result.getOwnerId());
    }

    @Test
    @DisplayName("shouldSetAndGetApprovalStatus_successfully")
    void shouldSetAndGetApprovalStatus_successfully() {
        // Given
        RestaurantApprovalStatus status = RestaurantApprovalStatus.APPROVED;

        // When
        restaurant.setApprovalStatus(status);

        // Then
        assertEquals(status, restaurant.getApprovalStatus());
    }

    // ========== Lifecycle Callback Tests ==========

    @Test
    @DisplayName("shouldSetCreatedAtAndUpdatedAt_onPrePersist")
    void shouldSetCreatedAtAndUpdatedAt_onPrePersist() throws Exception {
        // Given
        RestaurantProfile newRestaurant = new RestaurantProfile();

        // When - Simulate @PrePersist by calling onCreate directly
        java.lang.reflect.Method onCreate = RestaurantProfile.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(newRestaurant);

        // Then
        assertNotNull(newRestaurant.getCreatedAt());
        assertNotNull(newRestaurant.getUpdatedAt());
        assertEquals(newRestaurant.getCreatedAt(), newRestaurant.getUpdatedAt());
    }

    @Test
    @DisplayName("shouldSetUpdatedAt_onPreUpdate")
    void shouldSetUpdatedAt_onPreUpdate() throws Exception {
        // Given
        LocalDateTime initialCreatedAt = LocalDateTime.now().minusHours(1);
        restaurant.setCreatedAt(initialCreatedAt);
        restaurant.setUpdatedAt(initialCreatedAt);

        // When - Simulate @PreUpdate by calling onUpdate directly
        java.lang.reflect.Method onUpdate = RestaurantProfile.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(restaurant);

        // Then
        assertNotNull(restaurant.getUpdatedAt());
        assertTrue(restaurant.getUpdatedAt().isAfter(initialCreatedAt));
        assertEquals(initialCreatedAt, restaurant.getCreatedAt()); // createdAt should not change
    }

    // ========== Basic Field Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRestaurantId_successfully")
    void shouldSetAndGetRestaurantId_successfully() {
        // Given
        Integer restaurantId = 123;

        // When
        restaurant.setRestaurantId(restaurantId);

        // Then
        assertEquals(restaurantId, restaurant.getRestaurantId());
    }

    @Test
    @DisplayName("shouldSetAndGetAddress_successfully")
    void shouldSetAndGetAddress_successfully() {
        // Given
        String address = "123 Main Street";

        // When
        restaurant.setAddress(address);

        // Then
        assertEquals(address, restaurant.getAddress());
    }

    @Test
    @DisplayName("shouldSetAndGetPhone_successfully")
    void shouldSetAndGetPhone_successfully() {
        // Given
        String phone = "0987654321";

        // When
        restaurant.setPhone(phone);

        // Then
        assertEquals(phone, restaurant.getPhone());
    }

    @Test
    @DisplayName("shouldSetAndGetDescription_successfully")
    void shouldSetAndGetDescription_successfully() {
        // Given
        String description = "A great restaurant";

        // When
        restaurant.setDescription(description);

        // Then
        assertEquals(description, restaurant.getDescription());
    }

    @Test
    @DisplayName("shouldSetAndGetCuisineType_successfully")
    void shouldSetAndGetCuisineType_successfully() {
        // Given
        String cuisineType = "Vietnamese";

        // When
        restaurant.setCuisineType(cuisineType);

        // Then
        assertEquals(cuisineType, restaurant.getCuisineType());
    }

    @Test
    @DisplayName("shouldSetAndGetOpeningHours_successfully")
    void shouldSetAndGetOpeningHours_successfully() {
        // Given
        String openingHours = "9:00 AM - 10:00 PM";

        // When
        restaurant.setOpeningHours(openingHours);

        // Then
        assertEquals(openingHours, restaurant.getOpeningHours());
    }

    @Test
    @DisplayName("shouldSetAndGetAveragePrice_successfully")
    void shouldSetAndGetAveragePrice_successfully() {
        // Given
        BigDecimal averagePrice = new BigDecimal("200000");

        // When
        restaurant.setAveragePrice(averagePrice);

        // Then
        assertEquals(averagePrice, restaurant.getAveragePrice());
    }

    @Test
    @DisplayName("shouldSetAndGetWebsiteUrl_successfully")
    void shouldSetAndGetWebsiteUrl_successfully() {
        // Given
        String websiteUrl = "https://example.com";

        // When
        restaurant.setWebsiteUrl(websiteUrl);

        // Then
        assertEquals(websiteUrl, restaurant.getWebsiteUrl());
    }

    // ========== Extended Presentation Fields Tests ==========

    @Test
    @DisplayName("shouldSetAndGetHeroFields_successfully")
    void shouldSetAndGetHeroFields_successfully() {
        // Given
        String heroCity = "Ho Chi Minh City";
        String heroHeadline = "Welcome";
        String heroSubheadline = "Great Food";
        String heroSearchPlaceholder = "Search restaurants";

        // When
        restaurant.setHeroCity(heroCity);
        restaurant.setHeroHeadline(heroHeadline);
        restaurant.setHeroSubheadline(heroSubheadline);
        restaurant.setHeroSearchPlaceholder(heroSearchPlaceholder);

        // Then
        assertEquals(heroCity, restaurant.getHeroCity());
        assertEquals(heroHeadline, restaurant.getHeroHeadline());
        assertEquals(heroSubheadline, restaurant.getHeroSubheadline());
        assertEquals(heroSearchPlaceholder, restaurant.getHeroSearchPlaceholder());
    }

    @Test
    @DisplayName("shouldSetAndGetContactFields_successfully")
    void shouldSetAndGetContactFields_successfully() {
        // Given
        String contactHotline = "1900-xxxx";
        String contactSecondaryPhone = "0987654321";
        String statusMessage = "Open";

        // When
        restaurant.setContactHotline(contactHotline);
        restaurant.setContactSecondaryPhone(contactSecondaryPhone);
        restaurant.setStatusMessage(statusMessage);

        // Then
        assertEquals(contactHotline, restaurant.getContactHotline());
        assertEquals(contactSecondaryPhone, restaurant.getContactSecondaryPhone());
        assertEquals(statusMessage, restaurant.getStatusMessage());
    }

    @Test
    @DisplayName("shouldSetAndGetPriceRangeFields_successfully")
    void shouldSetAndGetPriceRangeFields_successfully() {
        // Given
        BigDecimal priceRangeMin = new BigDecimal("100000");
        BigDecimal priceRangeMax = new BigDecimal("500000");

        // When
        restaurant.setPriceRangeMin(priceRangeMin);
        restaurant.setPriceRangeMax(priceRangeMax);

        // Then
        assertEquals(priceRangeMin, restaurant.getPriceRangeMin());
        assertEquals(priceRangeMax, restaurant.getPriceRangeMax());
    }

    @Test
    @DisplayName("shouldSetAndGetBookingFields_successfully")
    void shouldSetAndGetBookingFields_successfully() {
        // Given
        String bookingInformation = "Booking info";
        String bookingNotes = "Booking notes";

        // When
        restaurant.setBookingInformation(bookingInformation);
        restaurant.setBookingNotes(bookingNotes);

        // Then
        assertEquals(bookingInformation, restaurant.getBookingInformation());
        assertEquals(bookingNotes, restaurant.getBookingNotes());
    }

    @Test
    @DisplayName("shouldSetAndGetPromotionFields_successfully")
    void shouldSetAndGetPromotionFields_successfully() {
        // Given
        String generalPromotions = "General promotions";
        String groupPromotions = "Group promotions";
        String promotionNotes = "Promotion notes";

        // When
        restaurant.setGeneralPromotions(generalPromotions);
        restaurant.setGroupPromotions(groupPromotions);
        restaurant.setPromotionNotes(promotionNotes);

        // Then
        assertEquals(generalPromotions, restaurant.getGeneralPromotions());
        assertEquals(groupPromotions, restaurant.getGroupPromotions());
        assertEquals(promotionNotes, restaurant.getPromotionNotes());
    }

    @Test
    @DisplayName("shouldSetAndGetSummaryFields_successfully")
    void shouldSetAndGetSummaryFields_successfully() {
        // Given
        String summaryHighlights = "Great food";
        String suitableFor = "Families";
        String signatureDishes = "Pho, Banh Mi";

        // When
        restaurant.setSummaryHighlights(summaryHighlights);
        restaurant.setSuitableFor(suitableFor);
        restaurant.setSignatureDishes(signatureDishes);

        // Then
        assertEquals(summaryHighlights, restaurant.getSummaryHighlights());
        assertEquals(suitableFor, restaurant.getSuitableFor());
        assertEquals(signatureDishes, restaurant.getSignatureDishes());
    }

    // ========== Timestamp Tests ==========

    @Test
    @DisplayName("shouldSetAndGetCreatedAt_successfully")
    void shouldSetAndGetCreatedAt_successfully() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        restaurant.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, restaurant.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetUpdatedAt_successfully")
    void shouldSetAndGetUpdatedAt_successfully() {
        // Given
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        restaurant.setUpdatedAt(updatedAt);

        // Then
        assertEquals(updatedAt, restaurant.getUpdatedAt());
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("shouldHandleNullAddress")
    void shouldHandleNullAddress() {
        // When
        restaurant.setAddress(null);

        // Then
        assertNull(restaurant.getAddress());
    }

    @Test
    @DisplayName("shouldHandleNullPhone")
    void shouldHandleNullPhone() {
        // When
        restaurant.setPhone(null);

        // Then
        assertNull(restaurant.getPhone());
    }

    @Test
    @DisplayName("shouldHandleNullDescription")
    void shouldHandleNullDescription() {
        // When
        restaurant.setDescription(null);

        // Then
        assertNull(restaurant.getDescription());
    }

    @Test
    @DisplayName("shouldHandleNullAveragePrice")
    void shouldHandleNullAveragePrice() {
        // When
        restaurant.setAveragePrice(null);

        // Then
        assertNull(restaurant.getAveragePrice());
    }
}

