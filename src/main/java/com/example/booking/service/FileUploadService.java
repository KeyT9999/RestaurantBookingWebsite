package com.example.booking.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Enhanced Service for handling file uploads
 * Manages restaurant media files, business licenses, and contract documents
 */
@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // File validation constants
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList("pdf", "doc", "docx");
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20MB

    /**
     * Upload restaurant media file
     */
    public String uploadRestaurantMedia(MultipartFile file, Integer restaurantId, String type) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Create upload directory if it doesn't exist
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = "restaurant_" + restaurantId + "_" + type + "_" + 
                         UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // Save file
        Path filePath = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return URL path
        return "/uploads/" + filename;
    }

    /**
     * Upload dish image
     */
    public String uploadDishImage(MultipartFile file, Integer dishId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Create upload directory if it doesn't exist
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = "dish_" + dishId + "_" + 
                         UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // Save file
        Path filePath = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return URL path
        return "/uploads/" + filename;
    }

    /**
     * Upload review report evidence image
     */
    public String uploadReviewEvidence(MultipartFile file, Integer reviewId, UUID ownerId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String filename = "review_report_" + reviewId + "_" +
                (ownerId != null ? ownerId.toString().substring(0, 8) : "unknown") + "_" +
                UUID.randomUUID().toString().substring(0, 8) + extension;

        Path filePath = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename;
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath.replace("/uploads/", ""));
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath.replace("/uploads/", ""));
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }

    // ============= NEW METHODS FOR PHASE 4.1 =============

    /**
     * Upload business license file for restaurant approval
     */
    public String uploadBusinessLicense(MultipartFile file, Integer restaurantId) throws IOException {
        logger.info("Uploading business license for restaurant ID: {}", restaurantId);
        
        // Validate file
        validateBusinessLicenseFile(file);
        
        // Create business license directory
        String businessLicenseDir = uploadDir + File.separator + "business_licenses";
        File dir = new File(businessLicenseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate secure filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = "business_license_" + restaurantId + "_" + 
                         UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // Save file
        Path filePath = Paths.get(businessLicenseDir, filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("Business license uploaded successfully: {}", filename);
        
        // Return URL path
        return "/uploads/business_licenses/" + filename;
    }

    /**
     * Upload contract document
     */
    public String uploadContractDocument(MultipartFile file, Integer restaurantId, String contractType) throws IOException {
        logger.info("Uploading contract document for restaurant ID: {}, type: {}", restaurantId, contractType);
        
        // Validate file
        validateContractFile(file);
        
        // Create contract directory
        String contractDir = uploadDir + File.separator + "contracts";
        File dir = new File(contractDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate secure filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = "contract_" + contractType + "_" + restaurantId + "_" + 
                         UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // Save file
        Path filePath = Paths.get(contractDir, filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("Contract document uploaded successfully: {}", filename);
        
        // Return URL path
        return "/uploads/contracts/" + filename;
    }

    /**
     * Enhanced restaurant media upload with validation
     */
    public String uploadRestaurantMediaEnhanced(MultipartFile file, Integer restaurantId, String type) throws IOException {
        logger.info("Uploading restaurant media for restaurant ID: {}, type: {}", restaurantId, type);
        
        // Validate based on type
        if (isImageType(type)) {
            validateImageFile(file);
        } else if (isDocumentType(type)) {
            validateDocumentFile(file);
        }
        
        // Create type-specific directory
        String mediaDir = uploadDir + File.separator + type + "s";
        File dir = new File(mediaDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate secure filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = "restaurant_" + restaurantId + "_" + type + "_" + 
                         UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // Save file
        Path filePath = Paths.get(mediaDir, filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("Restaurant media uploaded successfully: {}", filename);
        
        // Return URL path
        return "/uploads/" + type + "s/" + filename;
    }

    // ============= VALIDATION METHODS =============

    /**
     * Validate business license file
     */
    private void validateBusinessLicenseFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File giấy phép kinh doanh không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        // Remove dot from extension for comparison
        String extensionWithoutDot = extension.startsWith(".") ? extension.substring(1) : extension;
        if (!ALLOWED_DOCUMENT_TYPES.contains(extensionWithoutDot) && !ALLOWED_IMAGE_TYPES.contains(extensionWithoutDot)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + 
                String.join(", ", ALLOWED_DOCUMENT_TYPES) + ", " + String.join(", ", ALLOWED_IMAGE_TYPES));
        }

        if (file.getSize() > MAX_DOCUMENT_SIZE) {
            throw new IllegalArgumentException("Kích thước file quá lớn. Tối đa " + (MAX_DOCUMENT_SIZE / 1024 / 1024) + "MB");
        }

        // Additional security checks
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("Tên file chứa ký tự không hợp lệ");
        }
    }

    /**
     * Validate contract file
     */
    private void validateContractFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File hợp đồng không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_DOCUMENT_TYPES.contains(extension)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + 
                String.join(", ", ALLOWED_DOCUMENT_TYPES));
        }

        if (file.getSize() > MAX_DOCUMENT_SIZE) {
            throw new IllegalArgumentException("Kích thước file quá lớn. Tối đa " + (MAX_DOCUMENT_SIZE / 1024 / 1024) + "MB");
        }
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename).toLowerCase();
        // Remove dot from extension for comparison
        String extensionWithoutDot = extension.startsWith(".") ? extension.substring(1) : extension;
        if (!ALLOWED_IMAGE_TYPES.contains(extensionWithoutDot)) {
            throw new IllegalArgumentException("Định dạng ảnh không được hỗ trợ. Chỉ chấp nhận: " + 
                String.join(", ", ALLOWED_IMAGE_TYPES));
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Kích thước ảnh quá lớn. Tối đa " + (MAX_IMAGE_SIZE / 1024 / 1024) + "MB");
        }
    }

    /**
     * Validate document file
     */
    private void validateDocumentFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File tài liệu không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename).toLowerCase();
        // Remove dot from extension for comparison
        String extensionWithoutDot = extension.startsWith(".") ? extension.substring(1) : extension;
        if (!ALLOWED_DOCUMENT_TYPES.contains(extensionWithoutDot)) {
            throw new IllegalArgumentException("Định dạng tài liệu không được hỗ trợ. Chỉ chấp nhận: " + 
                String.join(", ", ALLOWED_DOCUMENT_TYPES));
        }

        if (file.getSize() > MAX_DOCUMENT_SIZE) {
            throw new IllegalArgumentException("Kích thước tài liệu quá lớn. Tối đa " + (MAX_DOCUMENT_SIZE / 1024 / 1024) + "MB");
        }
    }

    // ============= HELPER METHODS =============

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Check if type is image type
     */
    private boolean isImageType(String type) {
        return Arrays.asList("logo", "cover", "gallery", "menu").contains(type.toLowerCase());
    }

    /**
     * Check if type is document type
     */
    private boolean isDocumentType(String type) {
        return Arrays.asList("business_license", "contract", "document").contains(type.toLowerCase());
    }
    
    /**
     * Rename business license file with actual restaurant ID
     */
    public String renameBusinessLicenseFile(String oldFileUrl, Integer restaurantId) throws IOException {
        if (oldFileUrl == null || oldFileUrl.isEmpty()) {
            throw new IllegalArgumentException("File URL không được để trống");
        }
        
        // Extract filename from URL
        String oldFilename = oldFileUrl.substring(oldFileUrl.lastIndexOf('/') + 1);
        String extension = getFileExtension(oldFilename);
        
        // Generate new filename with actual restaurant ID
        String newFilename = "business_license_" + restaurantId + "_" + 
                           UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // Build file paths
        String businessLicenseDir = uploadDir + File.separator + "business_licenses";
        Path oldFilePath = Paths.get(businessLicenseDir, oldFilename);
        Path newFilePath = Paths.get(businessLicenseDir, newFilename);
        
        // Rename file
        if (Files.exists(oldFilePath)) {
            Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Renamed business license file from {} to {}", oldFilename, newFilename);
            
            // Return new URL
            return "/uploads/business_licenses/" + newFilename;
        } else {
            logger.warn("Old business license file not found: {}", oldFilePath);
            return oldFileUrl; // Return original URL if file not found
        }
    }

    /**
     * Get file info for display
     */
    public FileInfo getFileInfo(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath.replace("/uploads/", ""));
            if (!Files.exists(path)) {
                return null;
            }
            
            return new FileInfo(
                path.getFileName().toString(),
                Files.size(path),
                Files.probeContentType(path)
            );
        } catch (IOException e) {
            logger.error("Error getting file info for: {}", filePath, e);
            return null;
        }
    }

    /**
     * File info class
     */
    public static class FileInfo {
        private final String filename;
        private final long size;
        private final String contentType;

        public FileInfo(String filename, long size, String contentType) {
            this.filename = filename;
            this.size = size;
            this.contentType = contentType;
        }

        public String getFilename() { return filename; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
        public String getFormattedSize() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
