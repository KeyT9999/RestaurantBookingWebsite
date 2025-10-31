package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class RestaurantProfileTest {

    private RestaurantProfile profile;

    @BeforeEach
    void setUp() {
        profile = new RestaurantProfile();
        profile.setRestaurantName("Test Restaurant");
        profile.setRestaurantId(123);
    }

    @Test
    void lifecycleCallbacksShouldPopulateTimestamps() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 10, 0);

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(created);
            profile.onCreate();
            assertEquals(created, profile.getCreatedAt());
            assertEquals(created, profile.getUpdatedAt());

            mocked.when(LocalDateTime::now).thenReturn(updated);
            profile.onUpdate();
            assertEquals(updated, profile.getUpdatedAt());
        }
    }

    @Test
    void reviewHelpersShouldComputeMetrics() {
        Review review1 = new Review();
        review1.setRating(5);
        review1.setCreatedAt(LocalDateTime.of(2024, 5, 1, 12, 0));

        Review review2 = new Review();
        review2.setRating(3);
        review2.setCreatedAt(LocalDateTime.of(2024, 5, 3, 12, 0));

        Review review3 = new Review();
        review3.setRating(4);
        review3.setCreatedAt(LocalDateTime.of(2024, 5, 2, 12, 0));

        List<Review> reviews = Arrays.asList(review1, review2, review3);
        profile.setReviews(reviews);

        assertEquals(4.0, profile.getAverageRating());
        assertEquals("4.0", profile.getFormattedAverageRating());
        assertEquals(3, profile.getReviewCount());
        assertTrue(profile.hasReviews());

        List<Review> recent = profile.getRecentReviews(2);
        assertEquals(2, recent.size());
        assertEquals(review2, recent.get(0));
        assertEquals(review3, recent.get(1));
    }

    @Test
    void tableHelpersShouldHandleNullCollections() {
        assertFalse(profile.hasTables());
        assertEquals(0, profile.getTableCount());

        profile.setTables(Arrays.asList(new RestaurantTable(), new RestaurantTable()));
        assertTrue(profile.hasTables());
        assertEquals(2, profile.getTableCount());
    }

    @Test
    void approvalHelpersShouldReflectStatusAndContract() {
        profile.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        profile.setContractSigned(true);

        assertTrue(profile.isApproved());
        assertTrue(profile.isActive());
        assertEquals(RestaurantApprovalStatus.APPROVED.getDisplayName(), profile.getApprovalStatusDisplay());

        profile.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        assertTrue(profile.isPending());
        assertTrue(profile.needsApproval());
    }

    @Test
    void termsAcceptanceShouldPopulateFields() {
        LocalDateTime acceptedAt = LocalDateTime.of(2024, 2, 1, 8, 0);

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(acceptedAt);
            profile.acceptTerms("2.0");
        }

        assertTrue(profile.hasAcceptedTerms());
        assertEquals("2.0", profile.getTermsVersion());
        assertEquals(acceptedAt, profile.getTermsAcceptedAt());

        profile.setBusinessLicenseFile("license.pdf");
        assertTrue(profile.hasBusinessLicense());
    }

    @Test
    void gettersShouldExposeIdentifiers() {
        assertEquals("Test Restaurant", profile.getName());
        assertEquals("123", profile.getId());
        assertNotNull(profile.getCreatedAt());
    }
}

