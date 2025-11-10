package com.example.booking.web.controller;

import com.example.booking.service.ImageUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for Cloudinary integration
 * Remove this controller in production
 */
@Controller
@RequestMapping("/test/cloudinary")
public class CloudinaryTestController {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryTestController.class);

    @Autowired
    private ImageUploadService imageUploadService;

    /**
     * Show test page
     */
    @GetMapping
    public String testPage(Model model) {
        model.addAttribute("pageTitle", "Cloudinary Test");
        return "test/cloudinary-test";
    }

    /**
     * Test restaurant image upload
     */
    @PostMapping("/upload/restaurant")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testRestaurantUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "restaurantId", defaultValue = "1") Integer restaurantId,
            @RequestParam(value = "imageType", defaultValue = "main") String imageType) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String imageUrl = imageUploadService.uploadRestaurantImage(file, restaurantId, imageType);
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("thumbnailUrl", imageUploadService.getThumbnailUrl(imageUrl));
            response.put("optimizedUrl", imageUploadService.getOptimizedImageUrl(imageUrl, 400, 300));
            response.put("isCloudinary", imageUploadService.isCloudinaryUrl(imageUrl));
            
            logger.info("Test restaurant upload successful: {}", imageUrl);
            
        } catch (Exception e) {
            logger.error("Test restaurant upload failed", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test dish image upload
     */
    @PostMapping("/upload/dish")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testDishUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "restaurantId", defaultValue = "1") Integer restaurantId,
            @RequestParam(value = "dishId", defaultValue = "1") Integer dishId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String imageUrl = imageUploadService.uploadDishImage(file, restaurantId, dishId);
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("thumbnailUrl", imageUploadService.getThumbnailUrl(imageUrl));
            response.put("optimizedUrl", imageUploadService.getOptimizedImageUrl(imageUrl, 300, 200));
            response.put("isCloudinary", imageUploadService.isCloudinaryUrl(imageUrl));
            
            logger.info("Test dish upload successful: {}", imageUrl);
            
        } catch (Exception e) {
            logger.error("Test dish upload failed", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test table image upload
     */
    @PostMapping("/upload/table")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testTableUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "restaurantId", defaultValue = "1") Integer restaurantId,
            @RequestParam(value = "tableId", defaultValue = "1") Integer tableId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String imageUrl = imageUploadService.uploadTableImage(file, restaurantId, tableId);
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("thumbnailUrl", imageUploadService.getThumbnailUrl(imageUrl));
            response.put("optimizedUrl", imageUploadService.getOptimizedImageUrl(imageUrl, 400, 300));
            response.put("isCloudinary", imageUploadService.isCloudinaryUrl(imageUrl));
            
            logger.info("Test table upload successful: {}", imageUrl);
            
        } catch (Exception e) {
            logger.error("Test table upload failed", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test avatar upload
     */
    @PostMapping("/upload/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testAvatarUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", defaultValue = "test-user") String userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String imageUrl = imageUploadService.uploadAvatar(file, userId);
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("thumbnailUrl", imageUploadService.getThumbnailUrl(imageUrl));
            response.put("isCloudinary", imageUploadService.isCloudinaryUrl(imageUrl));
            
            logger.info("Test avatar upload successful: {}", imageUrl);
            
        } catch (Exception e) {
            logger.error("Test avatar upload failed", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test image deletion
     */
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testDeleteImage(@RequestParam("imageUrl") String imageUrl) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = imageUploadService.deleteImage(imageUrl);
            response.put("success", deleted);
            response.put("message", deleted ? "Image deleted successfully" : "Failed to delete image");
            
            logger.info("Test image deletion result: {} for URL: {}", deleted, imageUrl);
            
        } catch (Exception e) {
            logger.error("Test image deletion failed", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test restaurant image update
     */
    @PostMapping("/update/restaurant")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testRestaurantUpdate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("oldImageUrl") String oldImageUrl,
            @RequestParam(value = "restaurantId", defaultValue = "1") Integer restaurantId,
            @RequestParam(value = "imageType", defaultValue = "main") String imageType) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String imageUrl = imageUploadService.updateRestaurantImage(file, oldImageUrl, restaurantId, imageType);
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("thumbnailUrl", imageUploadService.getThumbnailUrl(imageUrl));
            response.put("optimizedUrl", imageUploadService.getOptimizedImageUrl(imageUrl, 400, 300));
            response.put("isCloudinary", imageUploadService.isCloudinaryUrl(imageUrl));
            response.put("oldImageUrl", oldImageUrl);
            
            logger.info("Test restaurant update successful: {}", imageUrl);
            
        } catch (Exception e) {
            logger.error("Test restaurant update failed", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test dish image update
     */
    @PostMapping("/update/dish")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testDishUpdate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("oldImageUrl") String oldImageUrl,
            @RequestParam(value = "restaurantId", defaultValue = "1") Integer restaurantId,
            @RequestParam(value = "dishId", defaultValue = "1") Integer dishId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String imageUrl = imageUploadService.updateDishImage(file, oldImageUrl, restaurantId, dishId);
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("thumbnailUrl", imageUploadService.getThumbnailUrl(imageUrl));
            response.put("optimizedUrl", imageUploadService.getOptimizedImageUrl(imageUrl, 300, 200));
            response.put("isCloudinary", imageUploadService.isCloudinaryUrl(imageUrl));
            response.put("oldImageUrl", oldImageUrl);
            
            logger.info("Test dish update successful: {}", imageUrl);
            
        } catch (Exception e) {
            logger.error("Test dish update failed", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get Cloudinary configuration info
     */
    @GetMapping("/config")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // This is just for testing - don't expose sensitive info in production
            response.put("cloudinaryConfigured", true);
            response.put("message", "Cloudinary service is configured and ready");
            
        } catch (Exception e) {
            logger.error("Error getting config", e);
            response.put("cloudinaryConfigured", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
