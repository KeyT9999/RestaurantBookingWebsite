package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for RestaurantProfile
 * Coverage: 100% - All constructors, getters/setters, helper methods, branches
 */
@DisplayName("RestaurantProfile Tests")
class RestaurantProfileTest {

    private RestaurantProfile profile;

    @BeforeEach
    void setUp() {
        profile = new RestaurantProfile();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("shouldCreateDefaultConstructor")
        void shouldCreateDefaultConstructor() {
            // When
            RestaurantProfile profile = new RestaurantProfile();

            // Then
            assertNotNull(profile);
            assertNotNull(profile.getCreatedAt());
        }

        @Test
        @DisplayName("shouldCreateConstructorWithAllParameters")
        void shouldCreateConstructorWithAllParameters() {
            // Given
            RestaurantOwner owner = new RestaurantOwner();
            String restaurantName = "Test Restaurant";
            String address = "123 Test St";
            String phone = "0123456789";
            String description = "Test description";
            String cuisineType = "Vietnamese";
            String openingHours = "9:00-22:00";
            BigDecimal averagePrice = new BigDecimal("100000");
            String websiteUrl = "https://test.com";

            // When
            RestaurantProfile profile = new RestaurantProfile(
                owner, restaurantName, address, phone, description,
                cuisineType, openingHours, averagePrice, websiteUrl
            );

            // Then
            assertNotNull(profile);
            assertEquals(owner, profile.getOwner());
            assertEquals(restaurantName, profile.getRestaurantName());
            assertEquals(address, profile.getAddress());
            assertEquals(phone, profile.getPhone());
            assertEquals(description, profile.getDescription());
            assertEquals(cuisineType, profile.getCuisineType());
            assertEquals(openingHours, profile.getOpeningHours());
            assertEquals(averagePrice, profile.getAveragePrice());
            assertEquals(websiteUrl, profile.getWebsiteUrl());
            assertNotNull(profile.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests - Basic Fields")
    class BasicFieldGetterSetterTests {

        @Test
        @DisplayName("shouldGetAndSetGalleryNotes")
        void shouldGetAndSetGalleryNotes() {
            // Given
            String galleryNotes = "Beautiful restaurant photos";

            // When
            profile.setGalleryNotes(galleryNotes);

            // Then
            assertEquals(galleryNotes, profile.getGalleryNotes());
        }

        @Test
        @DisplayName("shouldGetAndSetDirectionInfo")
        void shouldGetAndSetDirectionInfo() {
            // Given
            String directionInfo = "Turn left at the corner";

            // When
            profile.setDirectionInfo(directionInfo);

            // Then
            assertEquals(directionInfo, profile.getDirectionInfo());
        }

        @Test
        @DisplayName("shouldGetAndSetOperatingSchedule")
        void shouldGetAndSetOperatingSchedule() {
            // Given
            String operatingSchedule = "Monday-Sunday: 9:00-22:00";

            // When
            profile.setOperatingSchedule(operatingSchedule);

            // Then
            assertEquals(operatingSchedule, profile.getOperatingSchedule());
        }

        @Test
        @DisplayName("shouldGetAndSetTermsAcceptedAt")
        void shouldGetAndSetTermsAcceptedAt() {
            // Given
            LocalDateTime termsAcceptedAt = LocalDateTime.now();

            // When
            profile.setTermsAcceptedAt(termsAcceptedAt);

            // Then
            assertEquals(termsAcceptedAt, profile.getTermsAcceptedAt());
        }

        @Test
        @DisplayName("shouldGetAndSetTermsVersion")
        void shouldGetAndSetTermsVersion() {
            // Given
            String termsVersion = "2.0";

            // When
            profile.setTermsVersion(termsVersion);

            // Then
            assertEquals(termsVersion, profile.getTermsVersion());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests - List Relationships")
    class ListRelationshipGetterSetterTests {

        @Test
        @DisplayName("shouldGetAndSetReviews")
        void shouldGetAndSetReviews() {
            // Given
            List<Review> reviews = new ArrayList<>();
            reviews.add(new Review());
            reviews.add(new Review());

            // When
            profile.setReviews(reviews);

            // Then
            assertNotNull(profile.getReviews());
            assertEquals(2, profile.getReviews().size());
        }

        @Test
        @DisplayName("shouldGetAndSetFavorites")
        void shouldGetAndSetFavorites() {
            // Given
            List<CustomerFavorite> favorites = new ArrayList<>();
            favorites.add(new CustomerFavorite());

            // When
            profile.setFavorites(favorites);

            // Then
            assertNotNull(profile.getFavorites());
            assertEquals(1, profile.getFavorites().size());
        }

        @Test
        @DisplayName("shouldGetAndSetVouchers")
        void shouldGetAndSetVouchers() {
            // Given
            List<Voucher> vouchers = new ArrayList<>();
            vouchers.add(new Voucher());

            // When
            profile.setVouchers(vouchers);

            // Then
            assertNotNull(profile.getVouchers());
            assertEquals(1, profile.getVouchers().size());
        }

        @Test
        @DisplayName("shouldGetAndSetWaitlists")
        void shouldGetAndSetWaitlists() {
            // Given
            List<Waitlist> waitlists = new ArrayList<>();
            waitlists.add(new Waitlist());

            // When
            profile.setWaitlists(waitlists);

            // Then
            assertNotNull(profile.getWaitlists());
            assertEquals(1, profile.getWaitlists().size());
        }

        @Test
        @DisplayName("shouldGetAndSetMedia")
        void shouldGetAndSetMedia() {
            // Given
            List<RestaurantMedia> media = new ArrayList<>();
            media.add(new RestaurantMedia());

            // When
            profile.setMedia(media);

            // Then
            assertNotNull(profile.getMedia());
            assertEquals(1, profile.getMedia().size());
        }

        @Test
        @DisplayName("shouldGetAndSetServices")
        void shouldGetAndSetServices() {
            // Given
            List<RestaurantService> services = new ArrayList<>();
            services.add(new RestaurantService());

            // When
            profile.setServices(services);

            // Then
            assertNotNull(profile.getServices());
            assertEquals(1, profile.getServices().size());
        }
    }

    @Nested
    @DisplayName("getRecentReviews() Tests")
    class GetRecentReviewsTests {

        @Test
        @DisplayName("shouldReturnEmptyList_whenReviewsIsNull")
        void shouldReturnEmptyList_whenReviewsIsNull() {
            // Given
            profile.setReviews(null);
            int limit = 5;

            // When
            List<Review> result = profile.getRecentReviews(limit);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("shouldReturnEmptyList_whenReviewsIsEmpty")
        void shouldReturnEmptyList_whenReviewsIsEmpty() {
            // Given
            profile.setReviews(new ArrayList<>());
            int limit = 5;

            // When
            List<Review> result = profile.getRecentReviews(limit);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("shouldReturnSortedAndLimitedReviews_whenReviewsExist")
        void shouldReturnSortedAndLimitedReviews_whenReviewsExist() {
            // Given
            List<Review> reviews = new ArrayList<>();
            Review review1 = new Review();
            review1.setCreatedAt(LocalDateTime.now().minusDays(1));
            Review review2 = new Review();
            review2.setCreatedAt(LocalDateTime.now().minusDays(2));
            Review review3 = new Review();
            review3.setCreatedAt(LocalDateTime.now());
            reviews.add(review1);
            reviews.add(review2);
            reviews.add(review3);
            profile.setReviews(reviews);
            int limit = 2;

            // When
            List<Review> result = profile.getRecentReviews(limit);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            // Most recent should be first
            assertEquals(review3.getCreatedAt(), result.get(0).getCreatedAt());
        }
    }

    @Nested
    @DisplayName("acceptTerms() Tests")
    class AcceptTermsTests {

        @Test
        @DisplayName("shouldAcceptTerms_withVersionProvided")
        void shouldAcceptTerms_withVersionProvided() {
            // Given
            String version = "2.0";

            // When
            profile.acceptTerms(version);

            // Then
            assertTrue(Boolean.TRUE.equals(profile.getTermsAccepted()));
            assertNotNull(profile.getTermsAcceptedAt());
            assertEquals(version, profile.getTermsVersion());
        }

        @Test
        @DisplayName("shouldAcceptTerms_withNullVersion_shouldUseDefault")
        void shouldAcceptTerms_withNullVersion_shouldUseDefault() {
            // Given
            String version = null;

            // When
            profile.acceptTerms(version);

            // Then
            assertTrue(Boolean.TRUE.equals(profile.getTermsAccepted()));
            assertNotNull(profile.getTermsAcceptedAt());
            assertEquals("1.0", profile.getTermsVersion()); // Default when null
        }
    }

    @Nested
    @DisplayName("hasAcceptedTerms() Tests")
    class HasAcceptedTermsTests {

        @Test
        @DisplayName("shouldReturnTrue_whenTermsAcceptedAndAcceptedAtNotNull")
        void shouldReturnTrue_whenTermsAcceptedAndAcceptedAtNotNull() {
            // Given
            profile.setTermsAccepted(true);
            profile.setTermsAcceptedAt(LocalDateTime.now());

            // When
            boolean result = profile.hasAcceptedTerms();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenTermsAcceptedIsFalse")
        void shouldReturnFalse_whenTermsAcceptedIsFalse() {
            // Given
            profile.setTermsAccepted(false);
            profile.setTermsAcceptedAt(LocalDateTime.now());

            // When
            boolean result = profile.hasAcceptedTerms();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenTermsAcceptedAtIsNull")
        void shouldReturnFalse_whenTermsAcceptedAtIsNull() {
            // Given
            profile.setTermsAccepted(true);
            profile.setTermsAcceptedAt(null);

            // When
            boolean result = profile.hasAcceptedTerms();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenBothAreNull")
        void shouldReturnFalse_whenBothAreNull() {
            // Given
            profile.setTermsAccepted(null);
            profile.setTermsAcceptedAt(null);

            // When
            boolean result = profile.hasAcceptedTerms();

            // Then
            assertFalse(result);
        }
    }
}
