package com.example.booking.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service for handling image uploads to Cloudinary
 * Replaces local file storage with cloud-based image management
 */
@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    @Autowired
    private Cloudinary cloudinary;

    // File validation constants
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Upload single image to Cloudinary - Universal method
     */
    public String uploadImage(MultipartFile file, String folder, String publicId) throws IOException {
        logger.info("Uploading image to Cloudinary folder: {}, publicId: {}", folder, publicId);
        
        validateImageFile(file);
        
        // Create custom options
        @SuppressWarnings("unchecked")
        Map<String, Object> options = ObjectUtils.asMap(
            "folder", folder,
            "public_id", publicId,
            "use_filename", true,
            "unique_filename", true,
            "overwrite", false,
            "resource_type", "image",
            "transformation", "c_fill,w_800,h_600,q_auto:good"
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
        String imageUrl = (String) result.get("secure_url");
        
        logger.info("Image uploaded successfully: {}", imageUrl);
        return imageUrl;
    }

    /**
     * Upload restaurant image to Cloudinary (supports multiple images per type)
     */
    public String uploadRestaurantImage(MultipartFile file, Integer restaurantId, String imageType, Integer imageNumber) throws IOException {
        String folder = "restaurants/" + restaurantId;
        String publicId = imageType + "_" + imageNumber + "_" + System.currentTimeMillis();
        return uploadImage(file, folder, publicId);
    }

    /**
     * Upload restaurant image to Cloudinary (default image number = 1)
     */
    public String uploadRestaurantImage(MultipartFile file, Integer restaurantId, String imageType) throws IOException {
        return uploadRestaurantImage(file, restaurantId, imageType, 1);
    }


    /**
     * Upload dish image to Cloudinary (single image only)
     */
    public String uploadDishImage(MultipartFile file, Integer restaurantId, Integer dishId) throws IOException {
        String folder = "restaurants/" + restaurantId + "/dishes";
        String publicId = "dish_" + dishId + "_" + System.currentTimeMillis();
        return uploadImage(file, folder, publicId);
    }

    /**
     * Upload user avatar to Cloudinary
     */
    public String uploadAvatar(MultipartFile file, Integer userId) throws IOException {
        String folder = "avatars/" + userId;
        String publicId = "avatar_" + System.currentTimeMillis();
        return uploadImage(file, folder, publicId);
    }

    /**
     * Upload table image to Cloudinary (supports multiple images per table)
     */
    public String uploadTableImage(MultipartFile file, Integer restaurantId, Integer tableId, Integer imageNumber) throws IOException {
        String folder = "restaurants/" + restaurantId + "/tables";
        String publicId = "table_" + tableId + "_" + imageNumber + "_" + System.currentTimeMillis();
        return uploadImage(file, folder, publicId);
    }

    /**
     * Upload table image to Cloudinary (default image number = 1)
     */
    public String uploadTableImage(MultipartFile file, Integer restaurantId, Integer tableId) throws IOException {
        return uploadTableImage(file, restaurantId, tableId, 1);
    }


    /**
     * Upload review evidence image to Cloudinary
     */
    public String uploadReviewEvidence(MultipartFile file, Integer reviewId) throws IOException {
        String folder = "reviews/" + reviewId;
        String publicId = "evidence_" + System.currentTimeMillis();
        return uploadImage(file, folder, publicId);
    }

    /**
     * Update image in Cloudinary (delete old and upload new)
     */
    public String updateImage(MultipartFile newFile, String oldImageUrl, String folder, String publicId) throws IOException {
        logger.info("Updating image in Cloudinary: {}", oldImageUrl);
        
        // Delete old image first
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            deleteImage(oldImageUrl);
        }
        
        // Upload new image
        return uploadImage(newFile, folder, publicId);
    }

    /**
     * Update restaurant image
     */
    public String updateRestaurantImage(MultipartFile newFile, String oldImageUrl, Integer restaurantId, String imageType) throws IOException {
        String folder = "restaurants/" + restaurantId;
        String publicId = imageType + "_" + System.currentTimeMillis();
        return updateImage(newFile, oldImageUrl, folder, publicId);
    }

    /**
     * Update dish image
     */
    public String updateDishImage(MultipartFile newFile, String oldImageUrl, Integer restaurantId, Integer dishId) throws IOException {
        String folder = "restaurants/" + restaurantId + "/dishes";
        String publicId = "dish_" + dishId + "_" + System.currentTimeMillis();
        return updateImage(newFile, oldImageUrl, folder, publicId);
    }

    /**
     * Update table image
     */
    public String updateTableImage(MultipartFile newFile, String oldImageUrl, Integer restaurantId, Integer tableId) throws IOException {
        String folder = "restaurants/" + restaurantId + "/tables";
        String publicId = "table_" + tableId + "_" + System.currentTimeMillis();
        return updateImage(newFile, oldImageUrl, folder, publicId);
    }

    /**
     * Update user avatar
     */
    public String updateAvatar(MultipartFile newFile, String oldImageUrl, Integer userId) throws IOException {
        String folder = "avatars/" + userId;
        String publicId = "avatar_" + System.currentTimeMillis();
        return updateImage(newFile, oldImageUrl, folder, publicId);
    }

    /**
     * Update review evidence
     */
    public String updateReviewEvidence(MultipartFile newFile, String oldImageUrl, Integer reviewId) throws IOException {
        String folder = "reviews/" + reviewId;
        String publicId = "evidence_" + System.currentTimeMillis();
        return updateImage(newFile, oldImageUrl, folder, publicId);
    }

    /**
     * Delete image from Cloudinary
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Extract public_id from URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                logger.warn("Could not extract public_id from URL: {}", imageUrl);
                return false;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
            
            boolean success = "ok".equals(resultStatus);
            logger.info("Image deletion result: {} for public_id: {}", resultStatus, publicId);
            return success;
            
        } catch (Exception e) {
            logger.error("Error deleting image from Cloudinary: {}", imageUrl, e);
            return false;
        }
    }

    /**
     * Get optimized image URL with transformations
     */
    public String getOptimizedImageUrl(String originalUrl, int width, int height) {
        try {
            String publicId = extractPublicIdFromUrl(originalUrl);
            if (publicId == null) {
                return originalUrl;
            }

            return cloudinary.url()
                .transformation(new com.cloudinary.Transformation<>()
                    .width(width)
                    .height(height)
                    .crop("fill")
                    .quality("auto:good"))
                .generate(publicId);
                
        } catch (Exception e) {
            logger.error("Error generating optimized URL for: {}", originalUrl, e);
            return originalUrl;
        }
    }

    /**
     * Get thumbnail URL
     */
    public String getThumbnailUrl(String originalUrl) {
        return getOptimizedImageUrl(originalUrl, 150, 150);
    }

    // ============= VALIDATION METHODS =============

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa là " + (MAX_IMAGE_SIZE / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_IMAGE_TYPES.contains(extension)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + String.join(", ", ALLOWED_IMAGE_TYPES));
        }
    }

    /**
     * Extract public_id from Cloudinary URL
     */
    private String extractPublicIdFromUrl(String url) {
        try {
            // Cloudinary URL format: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/public_id.jpg
            String[] parts = url.split("/");
            if (parts.length < 2) {
                return null;
            }

            // Find the part after "upload" or "v" (version)
            int uploadIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i]) || (parts[i].startsWith("v") && parts[i].length() > 1)) {
                    uploadIndex = i;
                    break;
                }
            }

            if (uploadIndex == -1 || uploadIndex >= parts.length - 1) {
                return null;
            }

            // Get the public_id (everything after upload/v)
            StringBuilder publicId = new StringBuilder();
            for (int i = uploadIndex + 1; i < parts.length; i++) {
                if (i > uploadIndex + 1) {
                    publicId.append("/");
                }
                publicId.append(parts[i]);
            }

            // Remove file extension
            String result = publicId.toString();
            int lastDotIndex = result.lastIndexOf(".");
            if (lastDotIndex > 0) {
                result = result.substring(0, lastDotIndex);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error extracting public_id from URL: {}", url, e);
            return null;
        }
    }
}
