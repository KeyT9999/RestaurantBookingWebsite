package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for ReviewForm and ReviewDto
 */
@DisplayName("Review DTOs Test Suite")
class ReviewDtoTest {

    @Nested
    @DisplayName("ReviewForm Tests")
    class ReviewFormTests {

        @Test
        @DisplayName("Should create ReviewForm with default constructor")
        void testDefaultConstructor() {
            ReviewForm form = new ReviewForm();
            assertThat(form).isNotNull();
        }

        @Test
        @DisplayName("Should create ReviewForm with constructor parameters")
        void testConstructor() {
            ReviewForm form = new ReviewForm(1, 5, "Great restaurant!");

            assertThat(form.getRestaurantId()).isEqualTo(1);
            assertThat(form.getRating()).isEqualTo(5);
            assertThat(form.getComment()).isEqualTo("Great restaurant!");
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            ReviewForm form = new ReviewForm();

            form.setRestaurantId(2);
            form.setRating(4);
            form.setComment("Good food");

            assertThat(form.getRestaurantId()).isEqualTo(2);
            assertThat(form.getRating()).isEqualTo(4);
            assertThat(form.getComment()).isEqualTo("Good food");
        }

        @Test
        @DisplayName("Should return correct toString representation")
        void testToString() {
            ReviewForm form = new ReviewForm(1, 5, "Great restaurant!");
            String toString = form.toString();

            assertThat(toString).contains("ReviewForm");
            assertThat(toString).contains("restaurantId=1");
            assertThat(toString).contains("rating=5");
            assertThat(toString).contains("Great restaurant!");
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            ReviewForm form = new ReviewForm();

            form.setRestaurantId(null);
            form.setRating(null);
            form.setComment(null);

            assertThat(form.getRestaurantId()).isNull();
            assertThat(form.getRating()).isNull();
            assertThat(form.getComment()).isNull();
        }
    }

    @Nested
    @DisplayName("ReviewDto Tests")
    class ReviewDtoTests {

        @Test
        @DisplayName("Should create ReviewDto with default constructor")
        void testDefaultConstructor() {
            ReviewDto dto = new ReviewDto();
            assertThat(dto).isNotNull();
        }

        @Test
        @DisplayName("Should create ReviewDto with all constructor parameters")
        void testAllArgsConstructor() {
            LocalDateTime createdAt = LocalDateTime.now();

            ReviewDto dto = new ReviewDto(1, 10, 5, "Great restaurant!",
                    "John Doe", "avatar.jpg", createdAt, "Restaurant Name", true);

            assertThat(dto.getReviewId()).isEqualTo(1);
            assertThat(dto.getRestaurantId()).isEqualTo(10);
            assertThat(dto.getRating()).isEqualTo(5);
            assertThat(dto.getComment()).isEqualTo("Great restaurant!");
            assertThat(dto.getCustomerName()).isEqualTo("John Doe");
            assertThat(dto.getCustomerAvatar()).isEqualTo("avatar.jpg");
            assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
            assertThat(dto.getRestaurantName()).isEqualTo("Restaurant Name");
            assertThat(dto.isEditable()).isTrue();
        }

        @Test
        @DisplayName("Should test all getters and setters")
        void testSettersAndGetters() {
            ReviewDto dto = new ReviewDto();
            LocalDateTime createdAt = LocalDateTime.now();

            dto.setReviewId(2);
            dto.setRestaurantId(20);
            dto.setRating(4);
            dto.setComment("Good food");
            dto.setCustomerName("Jane Doe");
            dto.setCustomerAvatar("jane.jpg");
            dto.setCreatedAt(createdAt);
            dto.setRestaurantName("Another Restaurant");
            dto.setEditable(false);

            assertThat(dto.getReviewId()).isEqualTo(2);
            assertThat(dto.getRestaurantId()).isEqualTo(20);
            assertThat(dto.getRating()).isEqualTo(4);
            assertThat(dto.getComment()).isEqualTo("Good food");
            assertThat(dto.getCustomerName()).isEqualTo("Jane Doe");
            assertThat(dto.getCustomerAvatar()).isEqualTo("jane.jpg");
            assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
            assertThat(dto.getRestaurantName()).isEqualTo("Another Restaurant");
            assertThat(dto.isEditable()).isFalse();
        }

        @Test
        @DisplayName("Should generate correct star rating for rating 5")
        void testGetStarRating_Rating5() {
            ReviewDto dto = new ReviewDto();
            dto.setRating(5);

            String starRating = dto.getStarRating();

            assertThat(starRating).isEqualTo("★★★★★");
        }

        @Test
        @DisplayName("Should generate correct star rating for rating 3")
        void testGetStarRating_Rating3() {
            ReviewDto dto = new ReviewDto();
            dto.setRating(3);

            String starRating = dto.getStarRating();

            assertThat(starRating).isEqualTo("★★★☆☆");
        }

        @Test
        @DisplayName("Should generate correct star rating for rating 1")
        void testGetStarRating_Rating1() {
            ReviewDto dto = new ReviewDto();
            dto.setRating(1);

            String starRating = dto.getStarRating();

            assertThat(starRating).isEqualTo("★☆☆☆☆");
        }

        @Test
        @DisplayName("Should return empty string when rating is null")
        void testGetStarRating_NullRating() {
            ReviewDto dto = new ReviewDto();
            dto.setRating(null);

            String starRating = dto.getStarRating();

            assertThat(starRating).isEmpty();
        }

        @Test
        @DisplayName("Should return correct toString representation")
        void testToString() {
            LocalDateTime createdAt = LocalDateTime.now();
            ReviewDto dto = new ReviewDto(1, 10, 5, "Great!",
                    "John", "avatar.jpg", createdAt, "Restaurant", true);

            String toString = dto.toString();

            assertThat(toString).contains("ReviewDto");
            assertThat(toString).contains("reviewId=1");
            assertThat(toString).contains("rating=5");
            assertThat(toString).contains("Great!");
            assertThat(toString).contains("John");
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            ReviewDto dto = new ReviewDto();

            dto.setComment(null);
            dto.setCustomerName(null);
            dto.setCustomerAvatar(null);
            dto.setRestaurantName(null);
            dto.setRating(null);
            dto.setCreatedAt(null);

            assertThat(dto.getComment()).isNull();
            assertThat(dto.getCustomerName()).isNull();
            assertThat(dto.getCustomerAvatar()).isNull();
            assertThat(dto.getRestaurantName()).isNull();
            assertThat(dto.getRating()).isNull();
            assertThat(dto.getCreatedAt()).isNull();
        }
    }
}

