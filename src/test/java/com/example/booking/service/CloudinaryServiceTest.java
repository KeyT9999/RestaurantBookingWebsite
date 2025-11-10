package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.Api;

/**
 * Unit tests for CloudinaryService
 * 
 * Test Coverage:
 * 1. uploadImage() - Success, validation errors, IOException
 * 2. uploadRestaurantImage() - With/without imageNumber
 * 3. uploadBusinessLicense() - PDF and image files
 * 4. uploadDishImage()
 * 5. uploadAvatar()
 * 6. uploadTableImage()
 * 7. uploadReviewEvidence()
 * 8. uploadServiceImage()
 * 9. updateImage() - With/without old image
 * 10. deleteImage() - Success, failure, invalid URL
 * 11. deleteFolder()
 * 12. Validation methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryService Tests")
public class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private Api api;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    private MultipartFile validImageFile;
    private MultipartFile validPdfFile;
    private MultipartFile invalidFileType;
    private MultipartFile tooLargeFile;
    private Map<String, Object> uploadResult;

    @BeforeEach
    void setUp() {
        // Setup valid image file
        validImageFile = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        // Setup valid PDF file
        validPdfFile = new MockMultipartFile(
            "document",
            "license.pdf",
            "application/pdf",
            "fake pdf content".getBytes()
        );

        // Setup invalid file type
        invalidFileType = new MockMultipartFile(
            "file",
            "test.exe",
            "application/x-msdownload",
            "fake exe content".getBytes()
        );

        // Setup too large file (mock)
        byte[] largeContent = new byte[25 * 1024 * 1024]; // 25MB
        tooLargeFile = new MockMultipartFile(
            "image",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        // Setup upload result
        uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/test/image/upload/test.jpg");
        uploadResult.put("public_id", "test");

        // Setup mocks
        when(cloudinary.uploader()).thenReturn(uploader);
        when(cloudinary.api()).thenReturn(api);
    }

    // ========== uploadImage() Tests ==========

    @Test
    @DisplayName("shouldUploadImage_successfully")
    void shouldUploadImage_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadImage(validImageFile, "test-folder", "test-public-id");

        // Then
        assertNotNull(result);
        assertEquals("https://res.cloudinary.com/test/image/upload/test.jpg", result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("shouldThrowException_whenFileIsEmpty")
    void shouldThrowException_whenFileIsEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[0]);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.uploadImage(emptyFile, "folder", "public-id");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenFileIsNull")
    void shouldThrowException_whenFileIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            cloudinaryService.uploadImage(null, "folder", "public-id");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenInvalidFileType")
    void shouldThrowException_whenInvalidFileType() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.uploadImage(invalidFileType, "folder", "public-id");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenFileTooLarge")
    void shouldThrowException_whenFileTooLarge() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.uploadImage(tooLargeFile, "folder", "public-id");
        });
    }

    @Test
    @DisplayName("shouldHandleIOException_whenUploadFails")
    void shouldHandleIOException_whenUploadFails() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("Upload failed"));

        // When & Then
        assertThrows(IOException.class, () -> {
            cloudinaryService.uploadImage(validImageFile, "folder", "public-id");
        });
    }

    // ========== uploadRestaurantImage() Tests ==========

    @Test
    @DisplayName("shouldUploadRestaurantImage_withImageNumber")
    void shouldUploadRestaurantImage_withImageNumber() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadRestaurantImage(validImageFile, 1, "main", 2);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("shouldUploadRestaurantImage_withoutImageNumber")
    void shouldUploadRestaurantImage_withoutImageNumber() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadRestaurantImage(validImageFile, 1, "main");

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== uploadBusinessLicense() Tests ==========

    @Test
    @DisplayName("shouldUploadBusinessLicense_pdfFile")
    void shouldUploadBusinessLicense_pdfFile() throws Exception {
        // Given
        Map<String, Object> pdfResult = new HashMap<>();
        pdfResult.put("secure_url", "https://res.cloudinary.com/test/raw/upload/license.pdf");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(pdfResult);

        // When
        String result = cloudinaryService.uploadBusinessLicense(validPdfFile, 1);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("license.pdf") || result.contains("res.cloudinary.com"));
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("shouldUploadBusinessLicense_imageFile")
    void shouldUploadBusinessLicense_imageFile() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadBusinessLicense(validImageFile, 1);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("shouldThrowException_whenBusinessLicenseInvalidType")
    void shouldThrowException_whenBusinessLicenseInvalidType() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.uploadBusinessLicense(invalidFileType, 1);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenBusinessLicenseFilenameNull")
    void shouldThrowException_whenBusinessLicenseFilenameNull() {
        // Given
        MultipartFile fileWithoutName = new MockMultipartFile(
            "document",
            null,
            "application/pdf",
            "content".getBytes()
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.uploadBusinessLicense(fileWithoutName, 1);
        });
    }

    // ========== uploadDishImage() Tests ==========

    @Test
    @DisplayName("shouldUploadDishImage_successfully")
    void shouldUploadDishImage_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadDishImage(validImageFile, 1, 10);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== uploadAvatar() Tests ==========

    @Test
    @DisplayName("shouldUploadAvatar_successfully")
    void shouldUploadAvatar_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadAvatar(validImageFile, "test-user-id");

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== uploadTableImage() Tests ==========

    @Test
    @DisplayName("shouldUploadTableImage_withImageNumber")
    void shouldUploadTableImage_withImageNumber() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadTableImage(validImageFile, 1, 5, 2);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("shouldUploadTableImage_withoutImageNumber")
    void shouldUploadTableImage_withoutImageNumber() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadTableImage(validImageFile, 1, 5);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== uploadReviewEvidence() Tests ==========

    @Test
    @DisplayName("shouldUploadReviewEvidence_successfully")
    void shouldUploadReviewEvidence_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadReviewEvidence(validImageFile, 1);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== uploadServiceImage() Tests ==========

    @Test
    @DisplayName("shouldUploadServiceImage_successfully")
    void shouldUploadServiceImage_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadServiceImage(validImageFile, 1, 10);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== updateImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateImage_withOldImageUrl")
    void shouldUpdateImage_withOldImageUrl() throws Exception {
        // Given
        String oldImageUrl = "https://res.cloudinary.com/test/image/upload/old.jpg";
        Map<String, Object> destroyResult = new HashMap<>();
        destroyResult.put("result", "ok");
        when(uploader.destroy(anyString(), any(Map.class))).thenReturn(destroyResult);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateImage(validImageFile, oldImageUrl, "folder", "public-id");

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).destroy(anyString(), any(Map.class));
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("shouldUpdateImage_withoutOldImageUrl")
    void shouldUpdateImage_withoutOldImageUrl() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateImage(validImageFile, null, "folder", "public-id");

        // Then
        assertNotNull(result);
        verify(uploader, never()).destroy(anyString(), any(Map.class));
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("shouldUpdateImage_withEmptyOldImageUrl")
    void shouldUpdateImage_withEmptyOldImageUrl() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateImage(validImageFile, "", "folder", "public-id");

        // Then
        assertNotNull(result);
        verify(uploader, never()).destroy(anyString(), any(Map.class));
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== deleteImage() Tests ==========

    @Test
    @DisplayName("shouldDeleteImage_successfully")
    void shouldDeleteImage_successfully() throws Exception {
        // Given
        String imageUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/test.jpg";
        Map<String, Object> destroyResult = new HashMap<>();
        destroyResult.put("result", "ok");
        when(uploader.destroy(anyString(), any(Map.class))).thenReturn(destroyResult);

        // When
        boolean result = cloudinaryService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        verify(uploader, times(1)).destroy(anyString(), any(Map.class));
    }

    @Test
    @DisplayName("shouldReturnFalse_whenDeleteImageFails")
    void shouldReturnFalse_whenDeleteImageFails() throws Exception {
        // Given
        String imageUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/test.jpg";
        Map<String, Object> destroyResult = new HashMap<>();
        destroyResult.put("result", "not_found");
        when(uploader.destroy(anyString(), any(Map.class))).thenReturn(destroyResult);

        // When
        boolean result = cloudinaryService.deleteImage(imageUrl);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenInvalidImageUrl")
    void shouldReturnFalse_whenInvalidImageUrl() {
        // Given
        String invalidUrl = "invalid-url";

        // When
        boolean result = cloudinaryService.deleteImage(invalidUrl);

        // Then
        assertFalse(result);
        // verify() does not throw IOException, so this is safe
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNullImageUrl")
    void shouldReturnFalse_whenNullImageUrl() {
        // When
        boolean result = cloudinaryService.deleteImage(null);

        // Then
        assertFalse(result);
        try {
            verify(uploader, never()).destroy(anyString(), any(Map.class));
        } catch (IOException e) {
            // Expected in test
        }
    }

    @Test
    @DisplayName("shouldHandleException_whenDeleteThrowsException")
    void shouldHandleException_whenDeleteThrowsException() throws Exception {
        // Given
        String imageUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/test.jpg";
        doAnswer(invocation -> {
            throw new IOException("Delete failed");
        }).when(uploader).destroy(anyString(), any(Map.class));

        // When
        boolean result = cloudinaryService.deleteImage(imageUrl);

        // Then
        assertFalse(result);
    }
    // ========== deleteFolder() Tests ==========

    @Test
    @DisplayName("shouldDeleteFolder_successfully")
    void shouldDeleteFolder_successfully() throws Exception {
        // Given
        String folderPath = "restaurants/1";
        Map<String, Object> deleteResult = new HashMap<>();
        deleteResult.put("result", "ok");
        // api.deleteFolder returns Map<String, Object>, not ApiResponse
        // Using doAnswer to handle the actual return type
        doAnswer(invocation -> deleteResult).when(api).deleteFolder(eq(folderPath), any(Map.class));

        // When
        boolean result = cloudinaryService.deleteFolder(folderPath);

        // Then
        assertTrue(result);
        verify(api, times(1)).deleteFolder(eq(folderPath), any(Map.class));
    }

    @Test
    @DisplayName("shouldReturnFalse_whenDeleteFolderFails")
    void shouldReturnFalse_whenDeleteFolderFails() throws Exception {
        // Given
        String folderPath = "restaurants/1";
        Map<String, Object> deleteResult = new HashMap<>();
        deleteResult.put("result", "not_found");
        // api.deleteFolder returns Map<String, Object>, not ApiResponse
        // Using doAnswer to handle the actual return type
        doAnswer(invocation -> deleteResult).when(api).deleteFolder(eq(folderPath), any(Map.class));

        // When
        boolean result = cloudinaryService.deleteFolder(folderPath);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldHandleException_whenDeleteFolderThrowsException")
    void shouldHandleException_whenDeleteFolderThrowsException() throws Exception {
        // Given
        String folderPath = "restaurants/1";
        doAnswer(invocation -> {
            throw new IOException("Delete failed");
        }).when(api).deleteFolder(eq(folderPath), any(Map.class));

        // When
        boolean result = cloudinaryService.deleteFolder(folderPath);

        // Then
        assertFalse(result);
    }

    // ========== updateRestaurantImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateRestaurantImage_successfully")
    void shouldUpdateRestaurantImage_successfully() throws Exception {
        // Given
        String oldImageUrl = "https://res.cloudinary.com/test/image/upload/old.jpg";
        Map<String, Object> destroyResult = new HashMap<>();
        destroyResult.put("result", "ok");
        when(uploader.destroy(anyString(), any(Map.class))).thenReturn(destroyResult);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateRestaurantImage(validImageFile, oldImageUrl, 1, "main");

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== updateDishImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateDishImage_successfully")
    void shouldUpdateDishImage_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateDishImage(validImageFile, null, 1, 10);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== updateTableImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateTableImage_successfully")
    void shouldUpdateTableImage_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateTableImage(validImageFile, null, 1, 5);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== updateAvatar() Tests ==========

    @Test
    @DisplayName("shouldUpdateAvatar_successfully")
    void shouldUpdateAvatar_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateAvatar(validImageFile, null, "test-user-id");

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== updateReviewEvidence() Tests ==========

    @Test
    @DisplayName("shouldUpdateReviewEvidence_successfully")
    void shouldUpdateReviewEvidence_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateReviewEvidence(validImageFile, null, 1);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    // ========== updateServiceImage() Tests ==========

    @Test
    @DisplayName("shouldUpdateServiceImage_successfully")
    void shouldUpdateServiceImage_successfully() throws Exception {
        // Given
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.updateServiceImage(validImageFile, null, 1, 10);

        // Then
        assertNotNull(result);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }
}

