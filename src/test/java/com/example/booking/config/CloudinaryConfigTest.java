package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.cloudinary.Cloudinary;

/**
 * Unit tests for CloudinaryConfig
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryConfig Tests")
public class CloudinaryConfigTest {

    @InjectMocks
    private CloudinaryConfig cloudinaryConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cloudinaryConfig, "cloudName", "test-cloud");
        ReflectionTestUtils.setField(cloudinaryConfig, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(cloudinaryConfig, "apiSecret", "test-api-secret");
        ReflectionTestUtils.setField(cloudinaryConfig, "secure", true);
    }

    // ========== cloudinary() Bean Tests ==========

    @Test
    @DisplayName("shouldCreateCloudinaryBean_successfully")
    void shouldCreateCloudinaryBean_successfully() {
        // When
        Cloudinary cloudinary = cloudinaryConfig.cloudinary();

        // Then
        assertNotNull(cloudinary);
    }

    // ========== restaurantUploadOptions() Tests ==========

    @Test
    @DisplayName("shouldCreateRestaurantUploadOptions_successfully")
    void shouldCreateRestaurantUploadOptions_successfully() {
        // When
        Map<String, Object> options = cloudinaryConfig.restaurantUploadOptions();

        // Then
        assertNotNull(options);
        assertEquals("restaurants", options.get("folder"));
    }
}

