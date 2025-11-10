package com.example.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Unified service for image uploads
 * Uses Cloudinary for cloud storage only
 */
@Service
public class ImageUploadService {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadService.class);

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Upload single image to Cloudinary - Universal method
     */
    public String uploadImage(MultipartFile file, String folder, String publicId) throws IOException {
        logger.info("Uploading image to Cloudinary folder: {}, publicId: {}", folder, publicId);
        return cloudinaryService.uploadImage(file, folder, publicId);
    }

    /**
     * Upload restaurant image to Cloudinary (supports multiple images per type)
     */
    public String uploadRestaurantImage(MultipartFile file, Integer restaurantId, String imageType, Integer imageNumber) throws IOException {
        logger.info("Uploading restaurant image to Cloudinary for restaurant ID: {}, type: {}, image number: {}", restaurantId, imageType, imageNumber);
        return cloudinaryService.uploadRestaurantImage(file, restaurantId, imageType, imageNumber);
    }

    /**
     * Upload restaurant image to Cloudinary (single image per type - convenience
     * method)
     */
    public String uploadRestaurantImage(MultipartFile file, Integer restaurantId, String imageType) throws IOException {
        logger.info("Uploading restaurant image to Cloudinary for restaurant ID: {}, type: {}", restaurantId, imageType);
        return cloudinaryService.uploadRestaurantImage(file, restaurantId, imageType);
    }

    /**
     * Upload business license document to Cloudinary
     */
    public String uploadBusinessLicense(MultipartFile file, Integer restaurantId) throws IOException {
        logger.info("Uploading business license to Cloudinary for restaurant ID: {}", restaurantId);
        return cloudinaryService.uploadBusinessLicense(file, restaurantId);
    }

    /**
     * Upload dish image to Cloudinary only (single image per dish)
     */
    public String uploadDishImage(MultipartFile file, Integer restaurantId, Integer dishId) throws IOException {
        logger.info("Uploading dish image to Cloudinary for restaurant ID: {}, dish ID: {}", restaurantId, dishId);
        return cloudinaryService.uploadDishImage(file, restaurantId, dishId);
    }

    /**
     * Upload table image to Cloudinary (supports multiple images per table)
     */
    public String uploadTableImage(MultipartFile file, Integer restaurantId, Integer tableId, Integer imageNumber) throws IOException {
        logger.info("Uploading table image to Cloudinary for restaurant ID: {}, table ID: {}, image number: {}", restaurantId, tableId, imageNumber);
        return cloudinaryService.uploadTableImage(file, restaurantId, tableId, imageNumber);
    }

    /**
     * Upload table image to Cloudinary (default image number = 1)
     */
    public String uploadTableImage(MultipartFile file, Integer restaurantId, Integer tableId) throws IOException {
        logger.info("Uploading table image to Cloudinary for restaurant ID: {}, table ID: {}", restaurantId, tableId);
        return cloudinaryService.uploadTableImage(file, restaurantId, tableId);
    }

    /**
     * Upload user avatar to Cloudinary only
     */
    public String uploadAvatar(MultipartFile file, String userId) throws IOException {
        logger.info("Uploading avatar to Cloudinary for user ID: {}", userId);
        return cloudinaryService.uploadAvatar(file, userId);
    }

    /**
     * Upload review evidence to Cloudinary only
     */
    public String uploadReviewEvidence(MultipartFile file, Integer reviewId) throws IOException {
        logger.info("Uploading review evidence to Cloudinary for review ID: {}", reviewId);
        return cloudinaryService.uploadReviewEvidence(file, reviewId);
    }

    /**
     * Upload service image to Cloudinary only
     */
    public String uploadServiceImage(MultipartFile file, Integer restaurantId, Integer serviceId) throws IOException {
        logger.info("Uploading service image to Cloudinary for restaurant ID: {}, service ID: {}", restaurantId,
                serviceId);
        return cloudinaryService.uploadServiceImage(file, restaurantId, serviceId);
    }

    // ============= UPDATE METHODS =============

    /**
     * Update single image in Cloudinary
     */
    public String updateImage(MultipartFile newFile, String oldImageUrl, String folder, String publicId) throws IOException {
        logger.info("Updating image in Cloudinary folder: {}, publicId: {}", folder, publicId);
        return cloudinaryService.updateImage(newFile, oldImageUrl, folder, publicId);
    }

    /**
     * Update restaurant image
     */
    public String updateRestaurantImage(MultipartFile newFile, String oldImageUrl, Integer restaurantId, String imageType) throws IOException {
        logger.info("Updating restaurant image for restaurant ID: {}, type: {}", restaurantId, imageType);
        return cloudinaryService.updateRestaurantImage(newFile, oldImageUrl, restaurantId, imageType);
    }

    /**
     * Update dish image
     */
    public String updateDishImage(MultipartFile newFile, String oldImageUrl, Integer restaurantId, Integer dishId) throws IOException {
        logger.info("Updating dish image for restaurant ID: {}, dish ID: {}", restaurantId, dishId);
        return cloudinaryService.updateDishImage(newFile, oldImageUrl, restaurantId, dishId);
    }

    /**
     * Update table image
     */
    public String updateTableImage(MultipartFile newFile, String oldImageUrl, Integer restaurantId, Integer tableId) throws IOException {
        logger.info("Updating table image for restaurant ID: {}, table ID: {}", restaurantId, tableId);
        return cloudinaryService.updateTableImage(newFile, oldImageUrl, restaurantId, tableId);
    }

    /**
     * Update user avatar
     */
    public String updateAvatar(MultipartFile newFile, String oldImageUrl, String userId) throws IOException {
        logger.info("Updating avatar for user ID: {}", userId);
        return cloudinaryService.updateAvatar(newFile, oldImageUrl, userId);
    }

    /**
     * Update review evidence
     */
    public String updateReviewEvidence(MultipartFile newFile, String oldImageUrl, Integer reviewId) throws IOException {
        logger.info("Updating review evidence for review ID: {}", reviewId);
        return cloudinaryService.updateReviewEvidence(newFile, oldImageUrl, reviewId);
    }

    /**
     * Update service image
     */
    public String updateServiceImage(MultipartFile newFile, String oldImageUrl, Integer restaurantId, Integer serviceId)
            throws IOException {
        logger.info("Updating service image for restaurant ID: {}, service ID: {}", restaurantId, serviceId);
        return cloudinaryService.updateServiceImage(newFile, oldImageUrl, restaurantId, serviceId);
    }

    /**
     * Delete image (works for both Cloudinary and local URLs)
     */
    public boolean deleteImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("http")) {
            // Cloudinary URL
            return cloudinaryService.deleteImage(imageUrl);
        } else {
            // Local file - implement local deletion if needed
            logger.info("Local file deletion not implemented for: {}", imageUrl);
            return true; // Assume success for local files
        }
    }

    /**
     * Delete entire folder from Cloudinary
     */
    public boolean deleteFolder(String folderPath) {
        logger.info("Deleting folder from Cloudinary: {}", folderPath);
        return cloudinaryService.deleteFolder(folderPath);
    }

    /**
     * Delete all resources in a folder from Cloudinary
     */
    public boolean deleteFolderResources(String folderPath) {
        logger.info("Deleting all resources in folder from Cloudinary: {}", folderPath);
        return cloudinaryService.deleteFolderResources(folderPath);
    }

    /**
     * Get optimized image URL
     */
    public String getOptimizedImageUrl(String originalUrl, int width, int height) {
        if (originalUrl != null && originalUrl.startsWith("http")) {
            return cloudinaryService.getOptimizedImageUrl(originalUrl, width, height);
        }
        return originalUrl;
    }

    /**
     * Get thumbnail URL
     */
    public String getThumbnailUrl(String originalUrl) {
        if (originalUrl != null && originalUrl.startsWith("http")) {
            return cloudinaryService.getThumbnailUrl(originalUrl);
        }
        return originalUrl;
    }

    /**
     * Check if URL is from Cloudinary
     */
    public boolean isCloudinaryUrl(String url) {
        return url != null && url.startsWith("http") && url.contains("cloudinary.com");
    }
}
