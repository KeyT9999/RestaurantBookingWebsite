package com.example.booking.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Cloudinary image management service
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.secure:true}")
    private boolean secure;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", secure);
        
        return new Cloudinary(config);
    }

    /**
     * Default upload options for restaurant images
     */
    @Bean("restaurantUploadOptions")
    @SuppressWarnings("unchecked")
    public Map<String, Object> restaurantUploadOptions() {
        return ObjectUtils.asMap(
            "folder", "restaurants",
            "use_filename", true,
            "unique_filename", true,
            "overwrite", false,
            "resource_type", "image",
            "transformation", new Object[]{
                ObjectUtils.asMap("width", 800, "height", 600, "crop", "fill"),
                ObjectUtils.asMap("quality", "auto:good")
            }
        );
    }

    /**
     * Default upload options for dish images
     */
    @Bean("dishUploadOptions")
    @SuppressWarnings("unchecked")
    public Map<String, Object> dishUploadOptions() {
        return ObjectUtils.asMap(
            "folder", "dishes",
            "use_filename", true,
            "unique_filename", true,
            "overwrite", false,
            "resource_type", "image",
            "transformation", new Object[]{
                ObjectUtils.asMap("width", 600, "height", 400, "crop", "fill"),
                ObjectUtils.asMap("quality", "auto:good")
            }
        );
    }

    /**
     * Default upload options for user avatars
     */
    @Bean("avatarUploadOptions")
    @SuppressWarnings("unchecked")
    public Map<String, Object> avatarUploadOptions() {
        return ObjectUtils.asMap(
            "folder", "avatars",
            "use_filename", true,
            "unique_filename", true,
            "overwrite", false,
            "resource_type", "image",
            "transformation", new Object[]{
                ObjectUtils.asMap("width", 300, "height", 300, "crop", "fill"),
                ObjectUtils.asMap("quality", "auto:good")
            }
        );
    }

    /**
     * Default upload options for review evidence images
     */
    @Bean("reviewEvidenceUploadOptions")
    @SuppressWarnings("unchecked")
    public Map<String, Object> reviewEvidenceUploadOptions() {
        return ObjectUtils.asMap(
            "folder", "review_evidence",
            "use_filename", true,
            "unique_filename", true,
            "overwrite", false,
            "resource_type", "image",
            "transformation", new Object[]{
                ObjectUtils.asMap("width", 800, "height", 600, "crop", "limit"),
                ObjectUtils.asMap("quality", "auto:good")
            }
        );
    }
}
