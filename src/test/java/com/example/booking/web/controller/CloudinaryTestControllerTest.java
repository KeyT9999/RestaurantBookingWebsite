package com.example.booking.web.controller;

import com.example.booking.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CloudinaryTestController.class)
@DisplayName("CloudinaryTestController Test")
class CloudinaryTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageUploadService imageUploadService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("GET /test/cloudinary - Should return test page")
    void testTestPage_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/test/cloudinary"))
            .andExpect(status().isOk())
            .andExpect(view().name("test/cloudinary-test"))
            .andExpect(model().attribute("pageTitle", "Cloudinary Test"));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/restaurant - Should upload restaurant image")
    void testRestaurantUpload_ShouldUploadSuccessfully() throws Exception {
        String imageUrl = "https://cloudinary.com/test.jpg";
        
        when(imageUploadService.uploadRestaurantImage(any(), anyInt(), anyString()))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://cloudinary.com/test_thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(imageUrl, 400, 300))
            .thenReturn("https://cloudinary.com/test_opt.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/upload/restaurant")
                .file(mockFile)
                .param("restaurantId", "1")
                .param("imageType", "main"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.imageUrl").value(imageUrl))
            .andExpect(jsonPath("$.isCloudinary").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/restaurant - Should handle error")
    void testRestaurantUpload_ShouldHandleError() throws Exception {
        when(imageUploadService.uploadRestaurantImage(any(), anyInt(), anyString()))
            .thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/test/cloudinary/upload/restaurant")
                .file(mockFile)
                .param("restaurantId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/dish - Should upload dish image")
    void testDishUpload_ShouldUploadSuccessfully() throws Exception {
        String imageUrl = "https://cloudinary.com/dish.jpg";
        
        when(imageUploadService.uploadDishImage(any(), anyInt(), anyInt()))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://cloudinary.com/dish_thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(imageUrl, 300, 200))
            .thenReturn("https://cloudinary.com/dish_opt.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/upload/dish")
                .file(mockFile)
                .param("restaurantId", "1")
                .param("dishId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.imageUrl").value(imageUrl));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/table - Should upload table image")
    void testTableUpload_ShouldUploadSuccessfully() throws Exception {
        String imageUrl = "https://cloudinary.com/table.jpg";
        
        when(imageUploadService.uploadTableImage(any(), anyInt(), anyInt()))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://cloudinary.com/table_thumb.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/upload/table")
                .file(mockFile)
                .param("restaurantId", "1")
                .param("tableId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/avatar - Should upload avatar")
    void testAvatarUpload_ShouldUploadSuccessfully() throws Exception {
        String imageUrl = "https://cloudinary.com/avatar.jpg";
        
        when(imageUploadService.uploadAvatar(any(), anyInt()))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://cloudinary.com/avatar_thumb.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/upload/avatar")
                .file(mockFile)
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/delete - Should delete image")
    void testDeleteImage_ShouldDeleteSuccessfully() throws Exception {
        String imageUrl = "https://cloudinary.com/test.jpg";
        
        when(imageUploadService.deleteImage(imageUrl))
            .thenReturn(true);

        mockMvc.perform(post("/test/cloudinary/delete")
                .param("imageUrl", imageUrl))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Image deleted successfully"));
    }

    @Test
    @DisplayName("POST /test/cloudinary/update/restaurant - Should update restaurant image")
    void testRestaurantUpdate_ShouldUpdateSuccessfully() throws Exception {
        String oldImageUrl = "https://cloudinary.com/old.jpg";
        String newImageUrl = "https://cloudinary.com/new.jpg";
        
        when(imageUploadService.updateRestaurantImage(any(), eq(oldImageUrl), anyInt(), anyString()))
            .thenReturn(newImageUrl);
        when(imageUploadService.getThumbnailUrl(newImageUrl))
            .thenReturn("https://cloudinary.com/new_thumb.jpg");
        when(imageUploadService.isCloudinaryUrl(newImageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/update/restaurant")
                .file(mockFile)
                .param("oldImageUrl", oldImageUrl)
                .param("restaurantId", "1")
                .param("imageType", "main"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.imageUrl").value(newImageUrl));
    }

    @Test
    @DisplayName("POST /test/cloudinary/update/dish - Should update dish image")
    void testDishUpdate_ShouldUpdateSuccessfully() throws Exception {
        String oldImageUrl = "https://cloudinary.com/old_dish.jpg";
        String newImageUrl = "https://cloudinary.com/new_dish.jpg";
        
        when(imageUploadService.updateDishImage(any(), eq(oldImageUrl), anyInt(), anyInt()))
            .thenReturn(newImageUrl);
        when(imageUploadService.getThumbnailUrl(newImageUrl))
            .thenReturn("https://cloudinary.com/new_dish_thumb.jpg");
        when(imageUploadService.isCloudinaryUrl(newImageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/update/dish")
                .file(mockFile)
                .param("oldImageUrl", oldImageUrl)
                .param("restaurantId", "1")
                .param("dishId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /test/cloudinary/config - Should return config info")
    void testGetConfig_ShouldReturnConfig() throws Exception {
        mockMvc.perform(get("/test/cloudinary/config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cloudinaryConfigured").value(true))
            .andExpect(jsonPath("$.message").exists());
    }
}

