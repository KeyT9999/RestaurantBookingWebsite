package com.example.booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for handling file uploads
 * Manages restaurant media files (logos, covers, gallery images)
 */
@Service
public class FileUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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
}
