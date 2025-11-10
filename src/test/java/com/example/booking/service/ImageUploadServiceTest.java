package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Unit tests for ImageUploadService
 * This is a wrapper around CloudinaryService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageUploadService Tests")
public class ImageUploadServiceTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ImageUploadService imageUploadService;

    private MultipartFile validImageFile;

    @BeforeEach
    void setUp() {
        validImageFile = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
    }

    // ========== uploadImage() Tests ==========

    @Test
    @DisplayName("shouldUploadImage_successfully")
    void shouldUploadImage_successfully() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/test.jpg";
        when(cloudinaryService.uploadImage(any(MultipartFile.class), eq("folder"), eq("public-id")))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(validImageFile, "folder", "public-id");

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadImage(validImageFile, "folder", "public-id");
    }

    // ========== uploadRestaurantImage() Tests ==========

    @Test
    @DisplayName("shouldUploadRestaurantImage_withImageNumber")
    void shouldUploadRestaurantImage_withImageNumber() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/restaurant.jpg";
        when(cloudinaryService.uploadRestaurantImage(eq(validImageFile), eq(1), eq("main"), eq(2)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadRestaurantImage(validImageFile, 1, "main", 2);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadRestaurantImage(validImageFile, 1, "main", 2);
    }

    @Test
    @DisplayName("shouldUploadRestaurantImage_withoutImageNumber")
    void shouldUploadRestaurantImage_withoutImageNumber() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/restaurant.jpg";
        when(cloudinaryService.uploadRestaurantImage(eq(validImageFile), eq(1), eq("main")))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadRestaurantImage(validImageFile, 1, "main");

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadRestaurantImage(validImageFile, 1, "main");
    }

    // ========== uploadBusinessLicense() Tests ==========

    @Test
    @DisplayName("shouldUploadBusinessLicense_successfully")
    void shouldUploadBusinessLicense_successfully() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/raw/upload/license.pdf";
        when(cloudinaryService.uploadBusinessLicense(eq(validImageFile), eq(1)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadBusinessLicense(validImageFile, 1);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadBusinessLicense(validImageFile, 1);
    }

    // ========== uploadDishImage() Tests ==========

    @Test
    @DisplayName("shouldUploadDishImage_successfully")
    void shouldUploadDishImage_successfully() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/dish.jpg";
        when(cloudinaryService.uploadDishImage(eq(validImageFile), eq(1), eq(10)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadDishImage(validImageFile, 1, 10);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadDishImage(validImageFile, 1, 10);
    }

    // ========== uploadTableImage() Tests ==========

    @Test
    @DisplayName("shouldUploadTableImage_withImageNumber")
    void shouldUploadTableImage_withImageNumber() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/table.jpg";
        when(cloudinaryService.uploadTableImage(eq(validImageFile), eq(1), eq(5), eq(2)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadTableImage(validImageFile, 1, 5, 2);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadTableImage(validImageFile, 1, 5, 2);
    }

    @Test
    @DisplayName("shouldUploadTableImage_withoutImageNumber")
    void shouldUploadTableImage_withoutImageNumber() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/table.jpg";
        when(cloudinaryService.uploadTableImage(eq(validImageFile), eq(1), eq(5)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadTableImage(validImageFile, 1, 5);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadTableImage(validImageFile, 1, 5);
    }

    // ========== uploadAvatar() Tests ==========

    @Test
    @DisplayName("shouldUploadAvatar_successfully")
    void shouldUploadAvatar_successfully() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/avatar.jpg";
        String userId = "test-user-id";
        when(cloudinaryService.uploadAvatar(eq(validImageFile), eq(userId)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadAvatar(validImageFile, userId);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadAvatar(validImageFile, userId);
    }

    // ========== uploadReviewEvidence() Tests ==========

    @Test
    @DisplayName("shouldUploadReviewEvidence_successfully")
    void shouldUploadReviewEvidence_successfully() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/evidence.jpg";
        when(cloudinaryService.uploadReviewEvidence(eq(validImageFile), eq(1)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadReviewEvidence(validImageFile, 1);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadReviewEvidence(validImageFile, 1);
    }

    // ========== uploadServiceImage() Tests ==========

    @Test
    @DisplayName("shouldUploadServiceImage_successfully")
    void shouldUploadServiceImage_successfully() throws Exception {
        // Given
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/service.jpg";
        when(cloudinaryService.uploadServiceImage(eq(validImageFile), eq(1), eq(10)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadServiceImage(validImageFile, 1, 10);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).uploadServiceImage(validImageFile, 1, 10);
    }

    // ========== updateRestaurantImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateRestaurantImage_successfully")
    void shouldUpdateRestaurantImage_successfully() throws Exception {
        // Given
        String oldImageUrl = "https://res.cloudinary.com/test/image/upload/old.jpg";
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/new.jpg";
        when(cloudinaryService.updateRestaurantImage(eq(validImageFile), eq(oldImageUrl), eq(1), eq("main")))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.updateRestaurantImage(validImageFile, oldImageUrl, 1, "main");

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).updateRestaurantImage(validImageFile, oldImageUrl, 1, "main");
    }

    // ========== updateDishImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateDishImage_successfully")
    void shouldUpdateDishImage_successfully() throws Exception {
        // Given
        String oldImageUrl = "https://res.cloudinary.com/test/image/upload/old.jpg";
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/new.jpg";
        when(cloudinaryService.updateDishImage(eq(validImageFile), eq(oldImageUrl), eq(1), eq(10)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.updateDishImage(validImageFile, oldImageUrl, 1, 10);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).updateDishImage(validImageFile, oldImageUrl, 1, 10);
    }

    // ========== updateTableImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateTableImage_successfully")
    void shouldUpdateTableImage_successfully() throws Exception {
        // Given
        String oldImageUrl = "https://res.cloudinary.com/test/image/upload/old.jpg";
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/new.jpg";
        when(cloudinaryService.updateTableImage(eq(validImageFile), eq(oldImageUrl), eq(1), eq(5)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.updateTableImage(validImageFile, oldImageUrl, 1, 5);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).updateTableImage(validImageFile, oldImageUrl, 1, 5);
    }

    // ========== updateAvatar() Tests ==========

    @Test
    @DisplayName("shouldUpdateAvatar_successfully")
    void shouldUpdateAvatar_successfully() throws Exception {
        // Given
        String oldImageUrl = "https://res.cloudinary.com/test/image/upload/old.jpg";
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/new.jpg";
        String userId = "test-user-id";
        when(cloudinaryService.updateAvatar(eq(validImageFile), eq(oldImageUrl), eq(userId)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.updateAvatar(validImageFile, oldImageUrl, userId);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).updateAvatar(validImageFile, oldImageUrl, userId);
    }

    // ========== updateReviewEvidence() Tests ==========

    @Test
    @DisplayName("shouldUpdateReviewEvidence_successfully")
    void shouldUpdateReviewEvidence_successfully() throws Exception {
        // Given
        String oldImageUrl = "https://res.cloudinary.com/test/image/upload/old.jpg";
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/new.jpg";
        when(cloudinaryService.updateReviewEvidence(eq(validImageFile), eq(oldImageUrl), eq(1)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.updateReviewEvidence(validImageFile, oldImageUrl, 1);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).updateReviewEvidence(validImageFile, oldImageUrl, 1);
    }

    // ========== updateServiceImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateServiceImage_successfully")
    void shouldUpdateServiceImage_successfully() throws Exception {
        // Given
        String oldImageUrl = "https://res.cloudinary.com/test/image/upload/old.jpg";
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/new.jpg";
        when(cloudinaryService.updateServiceImage(eq(validImageFile), eq(oldImageUrl), eq(1), eq(10)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.updateServiceImage(validImageFile, oldImageUrl, 1, 10);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).updateServiceImage(validImageFile, oldImageUrl, 1, 10);
    }

    // ========== deleteImage() Tests ==========

    @Test
    @DisplayName("shouldDeleteImage_successfully")
    void shouldDeleteImage_successfully() {
        // Given
        String imageUrl = "https://res.cloudinary.com/test/image/upload/test.jpg";
        when(cloudinaryService.deleteImage(eq(imageUrl))).thenReturn(true);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        verify(cloudinaryService, times(1)).deleteImage(imageUrl);
    }

    // ========== getOptimizedImageUrl() Tests ==========

    @Test
    @DisplayName("shouldGetOptimizedImageUrl_successfully")
    void shouldGetOptimizedImageUrl_successfully() {
        // Given
        String originalUrl = "https://res.cloudinary.com/test/image/upload/test.jpg";
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/w_800,h_600,c_fill/test.jpg";
        when(cloudinaryService.getOptimizedImageUrl(eq(originalUrl), eq(800), eq(600)))
            .thenReturn(expectedUrl);

        // When
        String result = imageUploadService.getOptimizedImageUrl(originalUrl, 800, 600);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).getOptimizedImageUrl(originalUrl, 800, 600);
    }

    // ========== getThumbnailUrl() Tests ==========

    @Test
    @DisplayName("shouldGetThumbnailUrl_successfully")
    void shouldGetThumbnailUrl_successfully() {
        // Given
        String originalUrl = "https://res.cloudinary.com/test/image/upload/test.jpg";
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill/test.jpg";
        when(cloudinaryService.getThumbnailUrl(eq(originalUrl))).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.getThumbnailUrl(originalUrl);

        // Then
        assertEquals(expectedUrl, result);
        verify(cloudinaryService, times(1)).getThumbnailUrl(originalUrl);
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("shouldHandleIOException_whenUploadFails")
    void shouldHandleIOException_whenUploadFails() throws Exception {
        // Given
        when(cloudinaryService.uploadImage(any(MultipartFile.class), anyString(), anyString()))
            .thenThrow(new IOException("Upload failed"));

        // When & Then
        assertThrows(IOException.class, () -> {
            imageUploadService.uploadImage(validImageFile, "folder", "public-id");
        });
    }
}


