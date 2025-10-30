package com.example.booking.dto.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FavoriteRestaurantDto.
 * Coverage Target: 100%
 * Test Cases: 5
 *
 * @author Professional Test Engineer
 */
@DisplayName("FavoriteRestaurantDto Tests")
class FavoriteRestaurantDtoTest {

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void noArgsConstructor_SetsDefaults() {
        // When
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getRestaurantId()).isNull();
        assertThat(dto.isFavorited()).isFalse();
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void allArgsConstructor_SetsAllFields() {
        // Given
        Integer restaurantId = 1;
        String name = "Test Restaurant";
        String address = "123 Test St";
        String phone = "0901234567";
        String description = "Test Description";
        String cuisineType = "Vietnamese";
        String openingHours = "9:00-22:00";
        BigDecimal averagePrice = new BigDecimal("100000");
        String websiteUrl = "https://test.com";
        LocalDateTime favoritedAt = LocalDateTime.now();
        Double averageRating = 4.5;
        Integer reviewCount = 10;
        String imageUrl = "https://image.com/test.jpg";
        boolean isFavorited = true;

        // When
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto(
            restaurantId, name, address, phone, description, cuisineType,
            openingHours, averagePrice, websiteUrl, favoritedAt, 
            averageRating, reviewCount, imageUrl, isFavorited
        );

        // Then
        assertThat(dto.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(dto.getRestaurantName()).isEqualTo(name);
        assertThat(dto.getAddress()).isEqualTo(address);
        assertThat(dto.getPhone()).isEqualTo(phone);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getCuisineType()).isEqualTo(cuisineType);
        assertThat(dto.getOpeningHours()).isEqualTo(openingHours);
        assertThat(dto.getAveragePrice()).isEqualTo(averagePrice);
        assertThat(dto.getWebsiteUrl()).isEqualTo(websiteUrl);
        assertThat(dto.getFavoritedAt()).isEqualTo(favoritedAt);
        assertThat(dto.getAverageRating()).isEqualTo(averageRating);
        assertThat(dto.getReviewCount()).isEqualTo(reviewCount);
        assertThat(dto.getImageUrl()).isEqualTo(imageUrl);
        assertThat(dto.isFavorited()).isEqualTo(isFavorited);
    }

    @Test
    @DisplayName("Should set all fields via setters")
    void setters_UpdateFields() {
        // Given
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto();
        Integer restaurantId = 1;
        String name = "Test Restaurant";
        String address = "123 Test St";
        String phone = "0901234567";
        String description = "Test Description";
        String cuisineType = "Vietnamese";
        String openingHours = "9:00-22:00";
        BigDecimal averagePrice = new BigDecimal("100000");
        String websiteUrl = "https://test.com";
        LocalDateTime favoritedAt = LocalDateTime.now();
        Double averageRating = 4.5;
        Integer reviewCount = 10;
        String imageUrl = "https://image.com/test.jpg";
        boolean isFavorited = true;

        // When
        dto.setRestaurantId(restaurantId);
        dto.setRestaurantName(name);
        dto.setAddress(address);
        dto.setPhone(phone);
        dto.setDescription(description);
        dto.setCuisineType(cuisineType);
        dto.setOpeningHours(openingHours);
        dto.setAveragePrice(averagePrice);
        dto.setWebsiteUrl(websiteUrl);
        dto.setFavoritedAt(favoritedAt);
        dto.setAverageRating(averageRating);
        dto.setReviewCount(reviewCount);
        dto.setImageUrl(imageUrl);
        dto.setFavorited(isFavorited);

        // Then
        assertThat(dto.getRestaurantId()).isEqualTo(restaurantId);
        assertThat(dto.getRestaurantName()).isEqualTo(name);
        assertThat(dto.getAddress()).isEqualTo(address);
        assertThat(dto.getPhone()).isEqualTo(phone);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getCuisineType()).isEqualTo(cuisineType);
        assertThat(dto.getOpeningHours()).isEqualTo(openingHours);
        assertThat(dto.getAveragePrice()).isEqualTo(averagePrice);
        assertThat(dto.getWebsiteUrl()).isEqualTo(websiteUrl);
        assertThat(dto.getFavoritedAt()).isEqualTo(favoritedAt);
        assertThat(dto.getAverageRating()).isEqualTo(averageRating);
        assertThat(dto.getReviewCount()).isEqualTo(reviewCount);
        assertThat(dto.getImageUrl()).isEqualTo(imageUrl);
        assertThat(dto.isFavorited()).isEqualTo(isFavorited);
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void nullValues_HandledCorrectly() {
        // Given
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto();

        // When
        dto.setRestaurantId(null);
        dto.setRestaurantName(null);
        dto.setAddress(null);
        dto.setAveragePrice(null);
        dto.setAverageRating(null);

        // Then
        assertThat(dto.getRestaurantId()).isNull();
        assertThat(dto.getRestaurantName()).isNull();
        assertThat(dto.getAddress()).isNull();
        assertThat(dto.getAveragePrice()).isNull();
        assertThat(dto.getAverageRating()).isNull();
    }

    @Test
    @DisplayName("Should toggle favorited status correctly")
    void favoritedStatus_TogglesCorrectly() {
        // Given
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto();

        // When & Then - initially false
        assertThat(dto.isFavorited()).isFalse();

        // When - set to true
        dto.setFavorited(true);

        // Then
        assertThat(dto.isFavorited()).isTrue();

        // When - set back to false
        dto.setFavorited(false);

        // Then
        assertThat(dto.isFavorited()).isFalse();
    }
}

