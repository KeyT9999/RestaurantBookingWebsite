package com.example.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileUploadService Test Suite")
class FileUploadServiceTest {

    private FileUploadService fileUploadService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileUploadService = new FileUploadService();
        ReflectionTestUtils.setField(fileUploadService, "uploadDir", tempDir.toString());
    }

    @Nested
    @DisplayName("uploadRestaurantMedia() Tests")
    class UploadRestaurantMediaTests {

        @Test
        @DisplayName("Should upload restaurant media successfully")
        void shouldUploadRestaurantMediaSuccessfully() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "test image content".getBytes());

            String result = fileUploadService.uploadRestaurantMedia(file, 1, "logo");

            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/"));
            assertTrue(result.contains("restaurant_1_logo"));
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[0]);

            assertThrows(IllegalArgumentException.class, () -> 
                    fileUploadService.uploadRestaurantMedia(emptyFile, 1, "logo"));
        }

        @Test
        @DisplayName("Should create directory if not exists")
        void shouldCreateDirectoryIfNotExists() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "test content".getBytes());

            String result = fileUploadService.uploadRestaurantMedia(file, 1, "logo");

            assertNotNull(result);
            assertTrue(Files.exists(tempDir));
        }
    }

    @Nested
    @DisplayName("uploadDishImage() Tests")
    class UploadDishImageTests {

        @Test
        @DisplayName("Should upload dish image successfully")
        void shouldUploadDishImageSuccessfully() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "dish.jpg", "image/jpeg", "test content".getBytes());

            String result = fileUploadService.uploadDishImage(file, 1);

            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/"));
            assertTrue(result.contains("dish_1"));
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "dish.jpg", "image/jpeg", new byte[0]);

            assertThrows(IllegalArgumentException.class, () -> 
                    fileUploadService.uploadDishImage(emptyFile, 1));
        }
    }

    @Nested
    @DisplayName("uploadReviewEvidence() Tests")
    class UploadReviewEvidenceTests {

        @Test
        @DisplayName("Should upload review evidence successfully")
        void shouldUploadReviewEvidenceSuccessfully() throws IOException {
            UUID ownerId = UUID.randomUUID();
            MockMultipartFile file = new MockMultipartFile(
                    "file", "evidence.jpg", "image/jpeg", "test content".getBytes());

            String result = fileUploadService.uploadReviewEvidence(file, 1, ownerId);

            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/"));
            assertTrue(result.contains("review_report_1"));
        }

        @Test
        @DisplayName("Should handle null owner ID")
        void shouldHandleNullOwnerId() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "evidence.jpg", "image/jpeg", "test content".getBytes());

            String result = fileUploadService.uploadReviewEvidence(file, 1, null);

            assertNotNull(result);
            assertTrue(result.contains("review_report_1"));
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "evidence.jpg", "image/jpeg", new byte[0]);

            assertThrows(IllegalArgumentException.class, () -> 
                    fileUploadService.uploadReviewEvidence(emptyFile, 1, UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("deleteFile() Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Should delete file successfully")
        void shouldDeleteFileSuccessfully() throws IOException {
            Path testFile = tempDir.resolve("test.jpg");
            Files.write(testFile, "test content".getBytes());

            boolean result = fileUploadService.deleteFile("/uploads/test.jpg");

            assertTrue(result);
            assertFalse(Files.exists(testFile));
        }

        @Test
        @DisplayName("Should return false for non-existent file")
        void shouldReturnFalseForNonExistentFile() {
            boolean result = fileUploadService.deleteFile("/uploads/nonexistent.jpg");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle invalid file path")
        void shouldHandleInvalidFilePath() {
            boolean result = fileUploadService.deleteFile("invalid/path");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getFileSize() Tests")
    class GetFileSizeTests {

        @Test
        @DisplayName("Should return file size successfully")
        void shouldReturnFileSizeSuccessfully() throws IOException {
            Path testFile = tempDir.resolve("test.jpg");
            byte[] content = "test content".getBytes();
            Files.write(testFile, content);

            long result = fileUploadService.getFileSize("/uploads/test.jpg");

            assertEquals(content.length, result);
        }

        @Test
        @DisplayName("Should return 0 for non-existent file")
        void shouldReturnZeroForNonExistentFile() {
            long result = fileUploadService.getFileSize("/uploads/nonexistent.jpg");

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("uploadBusinessLicense() Tests")
    class UploadBusinessLicenseTests {

        @Test
        @DisplayName("Should upload business license PDF successfully")
        void shouldUploadBusinessLicensePdfSuccessfully() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "license.pdf", "application/pdf", "test pdf content".getBytes());

            String result = fileUploadService.uploadBusinessLicense(file, 1);

            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/business_licenses/"));
            assertTrue(result.contains("business_license_1"));
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "license.pdf", "application/pdf", new byte[0]);

            assertThrows(IllegalArgumentException.class, () -> 
                    fileUploadService.uploadBusinessLicense(emptyFile, 1));
        }

        @Test
        @DisplayName("Should throw exception for invalid file type")
        void shouldThrowExceptionForInvalidFileType() {
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file", "license.exe", "application/x-msdownload", "test content".getBytes());

            assertThrows(IllegalArgumentException.class, () -> 
                    fileUploadService.uploadBusinessLicense(invalidFile, 1));
        }
    }

    @Nested
    @DisplayName("uploadContractDocument() Tests")
    class UploadContractDocumentTests {

        @Test
        @DisplayName("Should upload contract document successfully")
        void shouldUploadContractDocumentSuccessfully() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "contract.pdf", "application/pdf", "test content".getBytes());

            String result = fileUploadService.uploadContractDocument(file, 1, "STANDARD");

            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/contracts/"));
            assertTrue(result.contains("contract_STANDARD_1"));
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "contract.pdf", "application/pdf", new byte[0]);

            assertThrows(IllegalArgumentException.class, () -> 
                    fileUploadService.uploadContractDocument(emptyFile, 1, "STANDARD"));
        }
    }

    @Nested
    @DisplayName("uploadRestaurantMediaEnhanced() Tests")
    class UploadRestaurantMediaEnhancedTests {

        @Test
        @DisplayName("Should upload image type successfully")
        void shouldUploadImageTypeSuccessfully() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "logo.jpg", "image/jpeg", "test content".getBytes());

            String result = fileUploadService.uploadRestaurantMediaEnhanced(file, 1, "logo");

            assertNotNull(result);
            assertTrue(result.contains("logo"));
        }

        @Test
        @DisplayName("Should upload document type successfully")
        void shouldUploadDocumentTypeSuccessfully() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "document.pdf", "application/pdf", "test content".getBytes());

            String result = fileUploadService.uploadRestaurantMediaEnhanced(file, 1, "document");

            assertNotNull(result);
            assertTrue(result.contains("document"));
        }
    }

    @Nested
    @DisplayName("renameBusinessLicenseFile() Tests")
    class RenameBusinessLicenseFileTests {

        @Test
        @DisplayName("Should rename business license file successfully")
        void shouldRenameBusinessLicenseFileSuccessfully() throws IOException {
            // Create business licenses directory
            Path businessLicenseDir = tempDir.resolve("business_licenses");
            Files.createDirectories(businessLicenseDir);
            
            // Create old file
            Path oldFile = businessLicenseDir.resolve("old_license.pdf");
            Files.write(oldFile, "test content".getBytes());

            String oldUrl = "/uploads/business_licenses/old_license.pdf";
            String result = fileUploadService.renameBusinessLicenseFile(oldUrl, 1);

            assertNotNull(result);
            assertTrue(result.contains("business_license_1"));
            assertFalse(Files.exists(oldFile));
        }

        @Test
        @DisplayName("Should throw exception for null URL")
        void shouldThrowExceptionForNullUrl() {
            assertThrows(IllegalArgumentException.class, () -> 
                    fileUploadService.renameBusinessLicenseFile(null, 1));
        }

        @Test
        @DisplayName("Should return original URL if file not found")
        void shouldReturnOriginalUrlIfFileNotFound() throws IOException {
            String oldUrl = "/uploads/business_licenses/nonexistent.pdf";
            String result = fileUploadService.renameBusinessLicenseFile(oldUrl, 1);

            assertEquals(oldUrl, result);
        }
    }

    @Nested
    @DisplayName("getFileInfo() Tests")
    class GetFileInfoTests {

        @Test
        @DisplayName("Should return file info successfully")
        void shouldReturnFileInfoSuccessfully() throws IOException {
            Path testFile = tempDir.resolve("test.jpg");
            Files.write(testFile, "test content".getBytes());

            FileUploadService.FileInfo result = fileUploadService.getFileInfo("/uploads/test.jpg");

            assertNotNull(result);
            assertEquals("test.jpg", result.getFilename());
            assertTrue(result.getSize() > 0);
        }

        @Test
        @DisplayName("Should return null for non-existent file")
        void shouldReturnNullForNonExistentFile() {
            FileUploadService.FileInfo result = fileUploadService.getFileInfo("/uploads/nonexistent.jpg");

            assertNull(result);
        }

        @Test
        @DisplayName("Should format file size correctly")
        void shouldFormatFileSizeCorrectly() throws IOException {
            Path testFile = tempDir.resolve("test.jpg");
            Files.write(testFile, "test content".getBytes());

            FileUploadService.FileInfo result = fileUploadService.getFileInfo("/uploads/test.jpg");

            assertNotNull(result);
            assertNotNull(result.getFormattedSize());
            assertTrue(result.getFormattedSize().contains("B") || 
                      result.getFormattedSize().contains("KB") || 
                      result.getFormattedSize().contains("MB"));
        }
    }
}

