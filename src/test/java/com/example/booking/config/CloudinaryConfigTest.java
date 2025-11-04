package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

/**
 * Unit test for CloudinaryConfig
 * Coverage: 100% - All bean methods
 */
@SpringBootTest
@TestPropertySource(properties = {
    "cloudinary.cloud-name=test-cloud",
    "cloudinary.api-key=test-key",
    "cloudinary.api-secret=test-secret"
})
@DisplayName("CloudinaryConfig Tests")
class CloudinaryConfigTest {

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Test
    @DisplayName("shouldReturnCloudinaryBean")
    void shouldReturnCloudinaryBean() {
        // When
        Cloudinary cloudinary = cloudinaryConfig.cloudinary();

        // Then
        assertNotNull(cloudinary);
    }

    @Test
    @DisplayName("shouldReturnRestaurantUploadOptions")
    void shouldReturnRestaurantUploadOptions() {
        // When
        Map<String, Object> options = cloudinaryConfig.restaurantUploadOptions();

        // Then
        assertNotNull(options);
        assertEquals("restaurants", options.get("folder"));
    }

    @Test
    @DisplayName("shouldReturnDishUploadOptions")
    void shouldReturnDishUploadOptions() {
        // When
        Map<String, Object> options = cloudinaryConfig.dishUploadOptions();

        // Then
        assertNotNull(options);
        assertEquals("dishes", options.get("folder"));
    }

    @Test
    @DisplayName("shouldReturnAvatarUploadOptions")
    void shouldReturnAvatarUploadOptions() {
        // When
        Map<String, Object> options = cloudinaryConfig.avatarUploadOptions();

        // Then
        assertNotNull(options);
        assertEquals("avatars", options.get("folder"));
    }

    @Test
    @DisplayName("shouldReturnReviewEvidenceUploadOptions")
    void shouldReturnReviewEvidenceUploadOptions() {
        // When
        Map<String, Object> options = cloudinaryConfig.reviewEvidenceUploadOptions();

        // Then
        assertNotNull(options);
        assertEquals("review_evidence", options.get("folder"));
    }
}
