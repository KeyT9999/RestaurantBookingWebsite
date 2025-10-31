package com.example.booking.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryService Test Suite")
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    private MockMultipartFile testImageFile;
    private MockMultipartFile testPdfFile;
    private Map<String, Object> mockUploadResult;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);

        testImageFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());

        testPdfFile = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "test pdf content".getBytes());

        mockUploadResult = new HashMap<>();
        mockUploadResult.put("secure_url", "https://res.cloudinary.com/test/image/upload/test.jpg");
        mockUploadResult.put("public_id", "test");
    }

    @Nested
    @DisplayName("uploadImage() Tests")
    class UploadImageTests {

        @Test
        @DisplayName("Should upload image successfully")
        void shouldUploadImageSuccessfully() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadImage(testImageFile, "test-folder", "test-id");

            assertNotNull(result);
            assertEquals("https://res.cloudinary.com/test/image/upload/test.jpg", result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[0]);

            assertThrows(IllegalArgumentException.class, () ->
                    cloudinaryService.uploadImage(emptyFile, "test-folder", "test-id"));
        }

        @Test
        @DisplayName("Should handle Cloudinary upload exception")
        void shouldHandleCloudinaryUploadException() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenThrow(new IOException("Cloudinary error"));

            assertThrows(IOException.class, () ->
                    cloudinaryService.uploadImage(testImageFile, "test-folder", "test-id"));
        }
    }

    @Nested
    @DisplayName("uploadRestaurantImage() Tests")
    class UploadRestaurantImageTests {

        @Test
        @DisplayName("Should upload restaurant image successfully")
        void shouldUploadRestaurantImageSuccessfully() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadRestaurantImage(testImageFile, 1, "logo", 1);

            assertNotNull(result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }

        @Test
        @DisplayName("Should upload restaurant image with default image number")
        void shouldUploadRestaurantImageWithDefaultImageNumber() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadRestaurantImage(testImageFile, 1, "logo");

            assertNotNull(result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }
    }

    @Nested
    @DisplayName("uploadBusinessLicense() Tests")
    class UploadBusinessLicenseTests {

        @Test
        @DisplayName("Should upload PDF business license successfully")
        void shouldUploadPdfBusinessLicenseSuccessfully() throws IOException {
            Map<String, Object> pdfResult = new HashMap<>();
            pdfResult.put("secure_url", "https://res.cloudinary.com/test/raw/upload/document.pdf");

            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(pdfResult);

            String result = cloudinaryService.uploadBusinessLicense(testPdfFile, 1);

            assertNotNull(result);
            assertEquals("https://res.cloudinary.com/test/raw/upload/document.pdf", result);
        }

        @Test
        @DisplayName("Should upload image business license successfully")
        void shouldUploadImageBusinessLicenseSuccessfully() throws IOException {
            MockMultipartFile imageLicense = new MockMultipartFile(
                    "file", "license.jpg", "image/jpeg", "test content".getBytes());

            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadBusinessLicense(imageLicense, 1);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw exception for invalid file type")
        void shouldThrowExceptionForInvalidFileType() {
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file", "license.exe", "application/x-msdownload", "test content".getBytes());

            assertThrows(IllegalArgumentException.class, () ->
                    cloudinaryService.uploadBusinessLicense(invalidFile, 1));
        }

        @Test
        @DisplayName("Should throw exception for file too large")
        void shouldThrowExceptionForFileTooLarge() {
            byte[] largeContent = new byte[21 * 1024 * 1024]; // 21MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file", "license.pdf", "application/pdf", largeContent);

            assertThrows(IllegalArgumentException.class, () ->
                    cloudinaryService.uploadBusinessLicense(largeFile, 1));
        }
    }

    @Nested
    @DisplayName("uploadDishImage() Tests")
    class UploadDishImageTests {

        @Test
        @DisplayName("Should upload dish image successfully")
        void shouldUploadDishImageSuccessfully() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadDishImage(testImageFile, 1, 1);

            assertNotNull(result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }
    }

    @Nested
    @DisplayName("uploadAvatar() Tests")
    class UploadAvatarTests {

        @Test
        @DisplayName("Should upload avatar successfully")
        void shouldUploadAvatarSuccessfully() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadAvatar(testImageFile, 1);

            assertNotNull(result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }
    }

    @Nested
    @DisplayName("uploadTableImage() Tests")
    class UploadTableImageTests {

        @Test
        @DisplayName("Should upload table image successfully")
        void shouldUploadTableImageSuccessfully() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadTableImage(testImageFile, 1, 1, 1);

            assertNotNull(result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }

        @Test
        @DisplayName("Should upload table image with default image number")
        void shouldUploadTableImageWithDefaultImageNumber() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadTableImage(testImageFile, 1, 1);

            assertNotNull(result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }
    }

    @Nested
    @DisplayName("uploadReviewEvidence() Tests")
    class UploadReviewEvidenceTests {

        @Test
        @DisplayName("Should upload review evidence successfully")
        void shouldUploadReviewEvidenceSuccessfully() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadReviewEvidence(testImageFile, 1);

            assertNotNull(result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }
    }

    @Nested
    @DisplayName("uploadServiceImage() Tests")
    class UploadServiceImageTests {

        @Test
        @DisplayName("Should upload service image successfully")
        void shouldUploadServiceImageSuccessfully() throws IOException {
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(mockUploadResult);

            String result = cloudinaryService.uploadServiceImage(testImageFile, 1, 1);

            assertNotNull(result);
            verify(uploader).upload(any(byte[].class), any(Map.class));
        }
    }

    @Nested
    @DisplayName("deleteImage() Tests")
    class DeleteImageTests {

        @Test
        @DisplayName("Should delete image successfully")
        void shouldDeleteImageSuccessfully() throws IOException {
            Map<String, Object> deleteResult = new HashMap<>();
            deleteResult.put("result", "ok");

            when(uploader.destroy(anyString(), any(Map.class)))
                    .thenReturn(deleteResult);

            boolean result = cloudinaryService.deleteImage("test-public-id");

            assertTrue(result);
            verify(uploader).destroy(eq("test-public-id"), any(Map.class));
        }

        @Test
        @DisplayName("Should return false when deletion fails")
        void shouldReturnFalseWhenDeletionFails() throws IOException {
            Map<String, Object> deleteResult = new HashMap<>();
            deleteResult.put("result", "not found");

            when(uploader.destroy(anyString(), any(Map.class)))
                    .thenReturn(deleteResult);

            boolean result = cloudinaryService.deleteImage("non-existent-id");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle Cloudinary exception")
        void shouldHandleCloudinaryException() throws IOException {
            when(uploader.destroy(anyString(), any(Map.class)))
                    .thenThrow(new IOException("Cloudinary error"));

            assertThrows(IOException.class, () ->
                    cloudinaryService.deleteImage("test-id"));
        }
    }

    @Nested
    @DisplayName("validateImageFile() Tests")
    class ValidateImageFileTests {

        @Test
        @DisplayName("Should validate valid image file")
        void shouldValidateValidImageFile() {
            assertDoesNotThrow(() -> {
                // Validation is called internally, so we test via upload
                when(uploader.upload(any(byte[].class), any(Map.class)))
                        .thenReturn(mockUploadResult);
                cloudinaryService.uploadImage(testImageFile, "test", "test");
            });
        }

        @Test
        @DisplayName("Should reject invalid image type")
        void shouldRejectInvalidImageType() {
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file", "test.exe", "application/x-msdownload", "test content".getBytes());

            assertThrows(IllegalArgumentException.class, () -> {
                when(uploader.upload(any(byte[].class), any(Map.class)))
                        .thenReturn(mockUploadResult);
                cloudinaryService.uploadImage(invalidFile, "test", "test");
            });
        }

        @Test
        @DisplayName("Should reject file too large")
        void shouldRejectFileTooLarge() {
            byte[] largeContent = new byte[21 * 1024 * 1024]; // 21MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file", "large.jpg", "image/jpeg", largeContent);

            assertThrows(IllegalArgumentException.class, () -> {
                when(uploader.upload(any(byte[].class), any(Map.class)))
                        .thenReturn(mockUploadResult);
                cloudinaryService.uploadImage(largeFile, "test", "test");
            });
        }
    }
}

