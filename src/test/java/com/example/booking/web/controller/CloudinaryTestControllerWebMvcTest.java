package com.example.booking.web.controller;

import com.example.booking.service.ImageUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CloudinaryTestController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CloudinaryTestController WebMvc Tests")
class CloudinaryTestControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageUploadService imageUploadService;

    @Test
    @DisplayName("GET /test/cloudinary - should show test page")
    void testTestPage() throws Exception {
        mockMvc.perform(get("/test/cloudinary"))
            .andExpect(status().isOk())
            .andExpect(view().name("test/cloudinary-test"));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/restaurant - should upload restaurant image")
    void testRestaurantUpload() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", 
            "image/jpeg", "test".getBytes());
        when(imageUploadService.uploadRestaurantImage(any(), anyInt(), anyString()))
            .thenReturn("https://cloudinary.com/test.jpg");
        when(imageUploadService.getThumbnailUrl(anyString())).thenReturn("https://cloudinary.com/thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(anyString(), anyInt(), anyInt()))
            .thenReturn("https://cloudinary.com/opt.jpg");
        when(imageUploadService.isCloudinaryUrl(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(multipart("/test/cloudinary/upload/restaurant")
                .file(file)
                .param("restaurantId", "1")
                .param("imageType", "main")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/dish - should upload dish image")
    void testDishUpload() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "dish.jpg", 
            "image/jpeg", "test".getBytes());
        when(imageUploadService.uploadDishImage(any(), anyInt(), anyInt()))
            .thenReturn("https://cloudinary.com/dish.jpg");
        when(imageUploadService.getThumbnailUrl(anyString())).thenReturn("https://cloudinary.com/thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(anyString(), anyInt(), anyInt()))
            .thenReturn("https://cloudinary.com/opt.jpg");
        when(imageUploadService.isCloudinaryUrl(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(multipart("/test/cloudinary/upload/dish")
                .file(file)
                .param("restaurantId", "1")
                .param("dishId", "1")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/delete - should delete image")
    void testDeleteImage() throws Exception {
        // Given
        when(imageUploadService.deleteImage(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/test/cloudinary/delete")
                .param("imageUrl", "https://cloudinary.com/test.jpg")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /test/cloudinary/config - should get config")
    void testGetConfig() throws Exception {
        mockMvc.perform(get("/test/cloudinary/config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cloudinaryConfigured").value(true));
    }
}

