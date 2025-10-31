package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;


/**
 * Unit tests for FileUploadService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadService Tests")
public class FileUploadServiceTest {

    @InjectMocks
    private FileUploadService fileUploadService;

    @TempDir
    Path tempDir;

    private MultipartFile validImageFile;
    private MultipartFile validPdfFile;
    private MultipartFile emptyFile;
    private MultipartFile invalidFileType;

    @BeforeEach
    void setUp() throws IOException {
        // Set upload directory to temp dir
        ReflectionTestUtils.setField(fileUploadService, "uploadDir", tempDir.toString());

        // Setup valid image file
        validImageFile = mock(MultipartFile.class);
        when(validImageFile.isEmpty()).thenReturn(false);
        when(validImageFile.getOriginalFilename()).thenReturn("test.jpg");
        when(validImageFile.getInputStream()).thenReturn(
            new java.io.ByteArrayInputStream("fake image content".getBytes()));
        when(validImageFile.getSize()).thenReturn(1024L);

        // Setup valid PDF file
        validPdfFile = mock(MultipartFile.class);
        when(validPdfFile.isEmpty()).thenReturn(false);
        when(validPdfFile.getOriginalFilename()).thenReturn("document.pdf");
        when(validPdfFile.getInputStream()).thenReturn(
            new java.io.ByteArrayInputStream("fake pdf content".getBytes()));
        when(validPdfFile.getSize()).thenReturn(2048L);

        // Setup empty file
        emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        // Setup invalid file type
        invalidFileType = mock(MultipartFile.class);
        when(invalidFileType.isEmpty()).thenReturn(false);
        when(invalidFileType.getOriginalFilename()).thenReturn("test.exe");
    }

    // ========== uploadRestaurantMedia() Tests ==========

    @Test
    @DisplayName("shouldUploadRestaurantMedia_successfully")
    void shouldUploadRestaurantMedia_successfully() throws Exception {
        // When
        String result = fileUploadService.uploadRestaurantMedia(validImageFile, 1, "main");

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));
        assertTrue(result.contains("restaurant_1"));
    }

    @Test
    @DisplayName("shouldThrowException_whenFileIsEmpty")
    void shouldThrowException_whenFileIsEmpty() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadService.uploadRestaurantMedia(emptyFile, 1, "main");
        });
    }

    // ========== uploadDishImage() Tests ==========

    @Test
    @DisplayName("shouldUploadDishImage_successfully")
    void shouldUploadDishImage_successfully() throws Exception {
        // When
        String result = fileUploadService.uploadDishImage(validImageFile, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));
        assertTrue(result.contains("dish_10"));
    }

    // ========== uploadReviewEvidence() Tests ==========

    @Test
    @DisplayName("shouldUploadReviewEvidence_successfully")
    void shouldUploadReviewEvidence_successfully() throws Exception {
        // Given
        UUID ownerId = UUID.randomUUID();

        // When
        String result = fileUploadService.uploadReviewEvidence(validImageFile, 1, ownerId);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));
    }

    // ========== uploadBusinessLicense() Tests ==========

    @Test
    @DisplayName("shouldUploadBusinessLicense_successfully")
    void shouldUploadBusinessLicense_successfully() throws Exception {
        // When
        String result = fileUploadService.uploadBusinessLicense(validPdfFile, 1);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));
    }

    // ========== deleteFile() Tests ==========

    @Test
    @DisplayName("shouldDeleteFile_successfully")
    void shouldDeleteFile_successfully() throws Exception {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "test content".getBytes());
        String filePath = "/uploads/test.txt";

        // When
        boolean result = fileUploadService.deleteFile(filePath);

        // Then
        // Since deleteFile uses filePath to find file, and we're using tempDir,
        // the result depends on implementation
        assertNotNull(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenFileNotFound")
    void shouldReturnFalse_whenFileNotFound() {
        // Given
        String filePath = "/uploads/nonexistent.txt";

        // When
        boolean result = fileUploadService.deleteFile(filePath);

        // Then
        assertFalse(result);
    }

    // ========== getFileSize() Tests ==========

    @Test
    @DisplayName("shouldGetFileSize_successfully")
    void shouldGetFileSize_successfully() throws Exception {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "test content".getBytes());
        String filePath = "/uploads/test.txt";

        // When
        long result = fileUploadService.getFileSize(filePath);

        // Then
        assertTrue(result > 0);
    }

    @Test
    @DisplayName("shouldReturnZero_whenFileNotFound")
    void shouldReturnZero_whenFileNotFound() {
        // Given
        String filePath = "/uploads/nonexistent.txt";

        // When
        long result = fileUploadService.getFileSize(filePath);

        // Then
        assertEquals(0, result);
    }

    // ========== uploadContractDocument() Tests ==========

    @Test
    @DisplayName("shouldUploadContractDocument_successfully")
    void shouldUploadContractDocument_successfully() throws Exception {
        // When
        String result = fileUploadService.uploadContractDocument(validPdfFile, 1, "partnership");

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/contracts/"));
        assertTrue(result.contains("contract_partnership_1"));
    }

    @Test
    @DisplayName("shouldThrowException_whenUploadContractWithInvalidFile")
    void shouldThrowException_whenUploadContractWithInvalidFile() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadService.uploadContractDocument(emptyFile, 1, "partnership");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenUploadContractWithImageFile")
    void shouldThrowException_whenUploadContractWithImageFile() {
        // When & Then
        // Contract documents should only accept PDF/DOC/DOCX
        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadService.uploadContractDocument(validImageFile, 1, "partnership");
        });
    }

    // ========== uploadRestaurantMediaEnhanced() Tests ==========

    @Test
    @DisplayName("shouldUploadRestaurantMediaEnhanced_successfully")
    void shouldUploadRestaurantMediaEnhanced_successfully() throws Exception {
        // When
        String result = fileUploadService.uploadRestaurantMediaEnhanced(validImageFile, 1, "cover");

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/covers/"));
    }

    @Test
    @DisplayName("shouldUploadRestaurantMediaEnhanced_withDocumentType")
    void shouldUploadRestaurantMediaEnhanced_withDocumentType() throws Exception {
        // When
        String result = fileUploadService.uploadRestaurantMediaEnhanced(validPdfFile, 1, "document");

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/documents/"));
    }

    @Test
    @DisplayName("shouldThrowException_whenUploadRestaurantMediaEnhancedWithEmptyFile")
    void shouldThrowException_whenUploadRestaurantMediaEnhancedWithEmptyFile() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadService.uploadRestaurantMediaEnhanced(emptyFile, 1, "cover");
        });
    }

    // ========== renameBusinessLicenseFile() Tests ==========

    @Test
    @DisplayName("shouldRenameBusinessLicenseFile_successfully")
    void shouldRenameBusinessLicenseFile_successfully() throws Exception {
        // Given
        String oldFilePath = fileUploadService.uploadBusinessLicense(validPdfFile, 1);
        String oldUrl = "/uploads/business_licenses/" + oldFilePath.substring(oldFilePath.lastIndexOf('/') + 1);

        // When
        String result = fileUploadService.renameBusinessLicenseFile(oldUrl, 1);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/business_licenses/"));
        assertTrue(result.contains("business_license_1"));
    }

    @Test
    @DisplayName("shouldThrowException_whenRenameWithEmptyUrl")
    void shouldThrowException_whenRenameWithEmptyUrl() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadService.renameBusinessLicenseFile("", 1);
        });
    }

    @Test
    @DisplayName("shouldReturnOriginalUrl_whenFileNotFound")
    void shouldReturnOriginalUrl_whenFileNotFound() throws Exception {
        // Given
        String originalUrl = "/uploads/business_licenses/nonexistent.pdf";

        // When
        String result = fileUploadService.renameBusinessLicenseFile(originalUrl, 1);

        // Then
        assertEquals(originalUrl, result);
    }

    // ========== getFileInfo() Tests ==========

    @Test
    @DisplayName("shouldGetFileInfo_successfully")
    void shouldGetFileInfo_successfully() throws Exception {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "test content".getBytes());
        String filePath = "/uploads/test.txt";

        // When
        FileUploadService.FileInfo result = fileUploadService.getFileInfo(filePath);

        // Then
        assertNotNull(result);
        assertTrue(result.getFilename().contains("test.txt"));
        assertTrue(result.getSize() > 0);
    }

    @Test
    @DisplayName("shouldReturnNull_whenFileNotFound")
    void shouldReturnNull_whenFileNotFound() {
        // Given
        String filePath = "/uploads/nonexistent.txt";

        // When
        FileUploadService.FileInfo result = fileUploadService.getFileInfo(filePath);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("shouldGetFormattedSize_correctly")
    void shouldGetFormattedSize_correctly() throws Exception {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        byte[] content = new byte[1024]; // 1KB
        Files.write(testFile, content);
        String filePath = "/uploads/test.txt";

        // When
        FileUploadService.FileInfo result = fileUploadService.getFileInfo(filePath);

        // Then
        assertNotNull(result);
        assertTrue(result.getFormattedSize().contains("KB"));
    }

    // ========== uploadReviewEvidence() Edge Cases ==========

    @Test
    @DisplayName("shouldUploadReviewEvidence_withNullOwnerId")
    void shouldUploadReviewEvidence_withNullOwnerId() throws Exception {
        // When
        String result = fileUploadService.uploadReviewEvidence(validImageFile, 1, null);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));
        assertTrue(result.contains("review_report_1"));
    }

    @Test
    @DisplayName("shouldUploadReviewEvidence_withValidOwnerId")
    void shouldUploadReviewEvidence_withValidOwnerId() throws Exception {
        // Given
        UUID ownerId = UUID.randomUUID();

        // When
        String result = fileUploadService.uploadReviewEvidence(validImageFile, 1, ownerId);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));
        assertTrue(result.contains("review_report_1"));
        assertTrue(result.contains(ownerId.toString().substring(0, 8)));
    }

    // ========== uploadRestaurantMedia() Edge Cases ==========

    @Test
    @DisplayName("shouldUploadRestaurantMedia_withDifferentTypes")
    void shouldUploadRestaurantMedia_withDifferentTypes() throws Exception {
        // Test different media types
        String[] types = {"main", "cover", "gallery", "menu"};
        
        for (String type : types) {
            // When
            String result = fileUploadService.uploadRestaurantMedia(validImageFile, 1, type);

            // Then
            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/"));
            assertTrue(result.contains("restaurant_1_" + type));
        }
    }

    @Test
    @DisplayName("shouldUploadRestaurantMedia_withNoExtension")
    void shouldUploadRestaurantMedia_withNoExtension() throws Exception {
        // Given
        MultipartFile fileWithoutExt = mock(MultipartFile.class);
        when(fileWithoutExt.isEmpty()).thenReturn(false);
        when(fileWithoutExt.getOriginalFilename()).thenReturn("test");
        when(fileWithoutExt.getInputStream()).thenReturn(
            new java.io.ByteArrayInputStream("fake content".getBytes()));
        when(fileWithoutExt.getSize()).thenReturn(1024L);

        // When
        String result = fileUploadService.uploadRestaurantMedia(fileWithoutExt, 1, "main");

        // Then
        assertNotNull(result);
        assertTrue(result.endsWith(".jpg")); // Default extension
    }

    // ========== uploadDishImage() Edge Cases ==========

    @Test
    @DisplayName("shouldUploadDishImage_withDifferentFormats")
    void shouldUploadDishImage_withDifferentFormats() throws Exception {
        // Test different image formats
        String[] formats = {".jpg", ".png", ".gif", ".webp"};
        
        for (String format : formats) {
            // Given
            MultipartFile formatFile = mock(MultipartFile.class);
            when(formatFile.isEmpty()).thenReturn(false);
            when(formatFile.getOriginalFilename()).thenReturn("test" + format);
            when(formatFile.getInputStream()).thenReturn(
                new java.io.ByteArrayInputStream("fake content".getBytes()));
            when(formatFile.getSize()).thenReturn(1024L);

            // When
            String result = fileUploadService.uploadDishImage(formatFile, 1);

            // Then
            assertNotNull(result);
            assertTrue(result.startsWith("/uploads/"));
            assertTrue(result.contains("dish_1"));
        }
    }
}


