package com.example.booking.dto.customer;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Customer DTOs
 */
class CustomerDtoTest {

    // ========== FavoriteRestaurantDto Tests ==========
    @Test
    void testFavoriteRestaurantDto_DefaultConstructor() {
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testFavoriteRestaurantDto_AllArgsConstructor() {
        LocalDateTime favoritedAt = LocalDateTime.now();
        BigDecimal averagePrice = new BigDecimal("200000");
        
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto(
            1, "Test Restaurant", "123 Test St", "0123456789",
            "Test Description", "Vietnamese", "9:00-22:00",
            averagePrice, "https://test.com", favoritedAt,
            4.5, 100, "https://image.com/test.jpg", true
        );

        assertThat(dto.getRestaurantId()).isEqualTo(1);
        assertThat(dto.getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(dto.getAddress()).isEqualTo("123 Test St");
        assertThat(dto.getPhone()).isEqualTo("0123456789");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getCuisineType()).isEqualTo("Vietnamese");
        assertThat(dto.getOpeningHours()).isEqualTo("9:00-22:00");
        assertThat(dto.getAveragePrice()).isEqualByComparingTo(averagePrice);
        assertThat(dto.getWebsiteUrl()).isEqualTo("https://test.com");
        assertThat(dto.getFavoritedAt()).isEqualTo(favoritedAt);
        assertThat(dto.getAverageRating()).isEqualTo(4.5);
        assertThat(dto.getReviewCount()).isEqualTo(100);
        assertThat(dto.getImageUrl()).isEqualTo("https://image.com/test.jpg");
        assertThat(dto.isFavorited()).isTrue();
    }

    @Test
    void testFavoriteRestaurantDto_SettersAndGetters() {
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto();
        LocalDateTime favoritedAt = LocalDateTime.now();
        BigDecimal averagePrice = new BigDecimal("150000");

        dto.setRestaurantId(2);
        dto.setRestaurantName("Another Restaurant");
        dto.setAddress("456 Another St");
        dto.setPhone("0987654321");
        dto.setDescription("Another Description");
        dto.setCuisineType("Italian");
        dto.setOpeningHours("10:00-23:00");
        dto.setAveragePrice(averagePrice);
        dto.setWebsiteUrl("https://another.com");
        dto.setFavoritedAt(favoritedAt);
        dto.setAverageRating(4.8);
        dto.setReviewCount(250);
        dto.setImageUrl("https://image.com/another.jpg");
        dto.setFavorited(false);

        assertThat(dto.getRestaurantId()).isEqualTo(2);
        assertThat(dto.getRestaurantName()).isEqualTo("Another Restaurant");
        assertThat(dto.getAddress()).isEqualTo("456 Another St");
        assertThat(dto.getPhone()).isEqualTo("0987654321");
        assertThat(dto.getDescription()).isEqualTo("Another Description");
        assertThat(dto.getCuisineType()).isEqualTo("Italian");
        assertThat(dto.getOpeningHours()).isEqualTo("10:00-23:00");
        assertThat(dto.getAveragePrice()).isEqualByComparingTo(averagePrice);
        assertThat(dto.getWebsiteUrl()).isEqualTo("https://another.com");
        assertThat(dto.getFavoritedAt()).isEqualTo(favoritedAt);
        assertThat(dto.getAverageRating()).isEqualTo(4.8);
        assertThat(dto.getReviewCount()).isEqualTo(250);
        assertThat(dto.getImageUrl()).isEqualTo("https://image.com/another.jpg");
        assertThat(dto.isFavorited()).isFalse();
    }

    // ========== ToggleFavoriteRequest Tests ==========
    @Test
    void testToggleFavoriteRequest_DefaultConstructor() {
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        assertThat(request).isNotNull();
    }

    @Test
    void testToggleFavoriteRequest_Constructor() {
        ToggleFavoriteRequest request = new ToggleFavoriteRequest(5);
        assertThat(request.getRestaurantId()).isEqualTo(5);
    }

    @Test
    void testToggleFavoriteRequest_SettersAndGetters() {
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        request.setRestaurantId(10);
        assertThat(request.getRestaurantId()).isEqualTo(10);
    }

    @Test
    void testToggleFavoriteRequest_NullRestaurantId() {
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        request.setRestaurantId(null);
        assertThat(request.getRestaurantId()).isNull();
    }

    // ========== ToggleFavoriteResponse Tests ==========
    @Test
    void testToggleFavoriteResponse_DefaultConstructor() {
        ToggleFavoriteResponse response = new ToggleFavoriteResponse();
        assertThat(response).isNotNull();
    }

    @Test
    void testToggleFavoriteResponse_ConstructorWithFourParams() {
        ToggleFavoriteResponse response = new ToggleFavoriteResponse(
            true, "Success", true, 5
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.isFavorited()).isTrue();
        assertThat(response.getFavoriteCount()).isEqualTo(5);
        assertThat(response.getRestaurantId()).isNull();
    }

    @Test
    void testToggleFavoriteResponse_ConstructorWithFiveParams() {
        ToggleFavoriteResponse response = new ToggleFavoriteResponse(
            true, "Success", false, 10, 7
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.isFavorited()).isFalse();
        assertThat(response.getFavoriteCount()).isEqualTo(10);
        assertThat(response.getRestaurantId()).isEqualTo(7);
    }

    @Test
    void testToggleFavoriteResponse_SuccessFactory_AddFavorite() {
        ToggleFavoriteResponse response = ToggleFavoriteResponse.success(true, 15);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Đã thêm vào danh sách yêu thích");
        assertThat(response.isFavorited()).isTrue();
        assertThat(response.getFavoriteCount()).isEqualTo(15);
        assertThat(response.getRestaurantId()).isNull();
    }

    @Test
    void testToggleFavoriteResponse_SuccessFactory_RemoveFavorite() {
        ToggleFavoriteResponse response = ToggleFavoriteResponse.success(false, 14);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Đã xóa khỏi danh sách yêu thích");
        assertThat(response.isFavorited()).isFalse();
        assertThat(response.getFavoriteCount()).isEqualTo(14);
        assertThat(response.getRestaurantId()).isNull();
    }

    @Test
    void testToggleFavoriteResponse_SuccessFactoryWithRestaurantId_AddFavorite() {
        ToggleFavoriteResponse response = ToggleFavoriteResponse.success(true, 20, 3);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Đã thêm vào danh sách yêu thích");
        assertThat(response.isFavorited()).isTrue();
        assertThat(response.getFavoriteCount()).isEqualTo(20);
        assertThat(response.getRestaurantId()).isEqualTo(3);
    }

    @Test
    void testToggleFavoriteResponse_SuccessFactoryWithRestaurantId_RemoveFavorite() {
        ToggleFavoriteResponse response = ToggleFavoriteResponse.success(false, 19, 3);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Đã xóa khỏi danh sách yêu thích");
        assertThat(response.isFavorited()).isFalse();
        assertThat(response.getFavoriteCount()).isEqualTo(19);
        assertThat(response.getRestaurantId()).isEqualTo(3);
    }

    @Test
    void testToggleFavoriteResponse_ErrorFactory() {
        ToggleFavoriteResponse response = ToggleFavoriteResponse.error("Restaurant not found");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Restaurant not found");
        assertThat(response.isFavorited()).isFalse();
        assertThat(response.getFavoriteCount()).isEqualTo(0);
        assertThat(response.getRestaurantId()).isNull();
    }

    @Test
    void testToggleFavoriteResponse_SettersAndGetters() {
        ToggleFavoriteResponse response = new ToggleFavoriteResponse();
        
        response.setSuccess(true);
        response.setMessage("Custom message");
        response.setFavorited(true);
        response.setFavoriteCount(25);
        response.setRestaurantId(8);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Custom message");
        assertThat(response.isFavorited()).isTrue();
        assertThat(response.getFavoriteCount()).isEqualTo(25);
        assertThat(response.getRestaurantId()).isEqualTo(8);
    }
}

