package com.example.booking.dto.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ToggleFavoriteResponse.
 * Coverage Target: 100%
 * Test Cases: 10
 *
 * @author Professional Test Engineer
 */
@DisplayName("ToggleFavoriteResponse Tests")
class ToggleFavoriteResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response with no-args constructor")
        void noArgsConstructor_SetsDefaults() {
            // When
            ToggleFavoriteResponse response = new ToggleFavoriteResponse();

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should create response with 4-args constructor")
        void fourArgsConstructor_SetsFields() {
            // Given
            boolean success = true;
            String message = "Success";
            boolean isFavorited = true;
            Integer favoriteCount = 10;

            // When
            ToggleFavoriteResponse response = new ToggleFavoriteResponse(
                success, message, isFavorited, favoriteCount
            );

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.isFavorited()).isTrue();
            assertThat(response.getFavoriteCount()).isEqualTo(favoriteCount);
        }

        @Test
        @DisplayName("Should create response with 5-args constructor")
        void fiveArgsConstructor_SetsAllFields() {
            // Given
            boolean success = true;
            String message = "Success";
            boolean isFavorited = true;
            Integer favoriteCount = 10;
            Integer restaurantId = 123;

            // When
            ToggleFavoriteResponse response = new ToggleFavoriteResponse(
                success, message, isFavorited, favoriteCount, restaurantId
            );

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.isFavorited()).isTrue();
            assertThat(response.getFavoriteCount()).isEqualTo(favoriteCount);
            assertThat(response.getRestaurantId()).isEqualTo(restaurantId);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create success response when favorited")
        void success_Favorited_ReturnsCorrectMessage() {
            // Given
            boolean isFavorited = true;
            Integer favoriteCount = 5;

            // When
            ToggleFavoriteResponse response = ToggleFavoriteResponse.success(isFavorited, favoriteCount);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Đã thêm vào danh sách yêu thích");
            assertThat(response.isFavorited()).isTrue();
            assertThat(response.getFavoriteCount()).isEqualTo(favoriteCount);
            assertThat(response.getRestaurantId()).isNull();
        }

        @Test
        @DisplayName("Should create success response when unfavorited")
        void success_Unfavorited_ReturnsCorrectMessage() {
            // Given
            boolean isFavorited = false;
            Integer favoriteCount = 3;

            // When
            ToggleFavoriteResponse response = ToggleFavoriteResponse.success(isFavorited, favoriteCount);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Đã xóa khỏi danh sách yêu thích");
            assertThat(response.isFavorited()).isFalse();
            assertThat(response.getFavoriteCount()).isEqualTo(favoriteCount);
        }

        @Test
        @DisplayName("Should create success response with restaurantId when favorited")
        void successWithRestaurantId_Favorited_ReturnsCorrectResponse() {
            // Given
            boolean isFavorited = true;
            Integer favoriteCount = 7;
            Integer restaurantId = 123;

            // When
            ToggleFavoriteResponse response = ToggleFavoriteResponse.success(
                isFavorited, favoriteCount, restaurantId
            );

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Đã thêm vào danh sách yêu thích");
            assertThat(response.isFavorited()).isTrue();
            assertThat(response.getFavoriteCount()).isEqualTo(favoriteCount);
            assertThat(response.getRestaurantId()).isEqualTo(restaurantId);
        }

        @Test
        @DisplayName("Should create success response with restaurantId when unfavorited")
        void successWithRestaurantId_Unfavorited_ReturnsCorrectResponse() {
            // Given
            boolean isFavorited = false;
            Integer favoriteCount = 2;
            Integer restaurantId = 456;

            // When
            ToggleFavoriteResponse response = ToggleFavoriteResponse.success(
                isFavorited, favoriteCount, restaurantId
            );

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Đã xóa khỏi danh sách yêu thích");
            assertThat(response.isFavorited()).isFalse();
            assertThat(response.getFavoriteCount()).isEqualTo(favoriteCount);
            assertThat(response.getRestaurantId()).isEqualTo(restaurantId);
        }

        @Test
        @DisplayName("Should create error response")
        void error_ReturnsErrorResponse() {
            // Given
            String errorMessage = "Something went wrong";

            // When
            ToggleFavoriteResponse response = ToggleFavoriteResponse.error(errorMessage);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo(errorMessage);
            assertThat(response.isFavorited()).isFalse();
            assertThat(response.getFavoriteCount()).isEqualTo(0);
            assertThat(response.getRestaurantId()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get all fields via setters and getters")
        void settersGetters_WorkCorrectly() {
            // Given
            ToggleFavoriteResponse response = new ToggleFavoriteResponse();

            // When
            response.setSuccess(true);
            response.setMessage("Test message");
            response.setFavorited(true);
            response.setFavoriteCount(15);
            response.setRestaurantId(789);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Test message");
            assertThat(response.isFavorited()).isTrue();
            assertThat(response.getFavoriteCount()).isEqualTo(15);
            assertThat(response.getRestaurantId()).isEqualTo(789);
        }
    }
}

