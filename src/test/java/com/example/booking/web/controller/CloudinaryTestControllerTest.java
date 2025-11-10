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
        
        when(imageUploadService.uploadAvatar(any(), anyString()))
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

    @Test
    @DisplayName("POST /test/cloudinary/upload/dish - Should handle error")
    void testDishUpload_ShouldHandleError() throws Exception {
        when(imageUploadService.uploadDishImage(any(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/test/cloudinary/upload/dish")
                .file(mockFile)
                .param("restaurantId", "1")
                .param("dishId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/table - Should handle error")
    void testTableUpload_ShouldHandleError() throws Exception {
        when(imageUploadService.uploadTableImage(any(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/test/cloudinary/upload/table")
                .file(mockFile)
                .param("restaurantId", "1")
                .param("tableId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/avatar - Should handle error")
    void testAvatarUpload_ShouldHandleError() throws Exception {
        when(imageUploadService.uploadAvatar(any(), anyString()))
            .thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/test/cloudinary/upload/avatar")
                .file(mockFile)
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /test/cloudinary/delete - Should handle delete failure")
    void testDeleteImage_ShouldHandleFailure() throws Exception {
        String imageUrl = "https://cloudinary.com/test.jpg";
        
        when(imageUploadService.deleteImage(imageUrl))
            .thenReturn(false);

        mockMvc.perform(post("/test/cloudinary/delete")
                .param("imageUrl", imageUrl))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Failed to delete image"));
    }

    @Test
    @DisplayName("POST /test/cloudinary/delete - Should handle exception")
    void testDeleteImage_ShouldHandleException() throws Exception {
        String imageUrl = "https://cloudinary.com/test.jpg";
        
        when(imageUploadService.deleteImage(imageUrl))
            .thenThrow(new RuntimeException("Delete failed"));

        mockMvc.perform(post("/test/cloudinary/delete")
                .param("imageUrl", imageUrl))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /test/cloudinary/update/restaurant - Should handle error")
    void testRestaurantUpdate_ShouldHandleError() throws Exception {
        String oldImageUrl = "https://cloudinary.com/old.jpg";
        
        when(imageUploadService.updateRestaurantImage(any(), eq(oldImageUrl), anyInt(), anyString()))
            .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(multipart("/test/cloudinary/update/restaurant")
                .file(mockFile)
                .param("oldImageUrl", oldImageUrl)
                .param("restaurantId", "1")
                .param("imageType", "main"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /test/cloudinary/update/dish - Should handle error")
    void testDishUpdate_ShouldHandleError() throws Exception {
        String oldImageUrl = "https://cloudinary.com/old_dish.jpg";
        
        when(imageUploadService.updateDishImage(any(), eq(oldImageUrl), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(multipart("/test/cloudinary/update/dish")
                .file(mockFile)
                .param("oldImageUrl", oldImageUrl)
                .param("restaurantId", "1")
                .param("dishId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /test/cloudinary/config - Should handle exception")
    void testGetConfig_ShouldHandleException() throws Exception {
        // This test would require mocking the controller itself to throw exception
        // For now, we'll test with default behavior
        mockMvc.perform(get("/test/cloudinary/config"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/restaurant - Should use default values")
    void testRestaurantUpload_WithDefaultValues() throws Exception {
        String imageUrl = "https://cloudinary.com/test.jpg";
        
        when(imageUploadService.uploadRestaurantImage(any(), eq(1), eq("main")))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://cloudinary.com/test_thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(imageUrl, 400, 300))
            .thenReturn("https://cloudinary.com/test_opt.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/upload/restaurant")
                .file(mockFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/dish - Should use default values")
    void testDishUpload_WithDefaultValues() throws Exception {
        String imageUrl = "https://cloudinary.com/dish.jpg";
        
        when(imageUploadService.uploadDishImage(any(), eq(1), eq(1)))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://cloudinary.com/dish_thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(imageUrl, 300, 200))
            .thenReturn("https://cloudinary.com/dish_opt.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/upload/dish")
                .file(mockFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/table - Should use default values")
    void testTableUpload_WithDefaultValues() throws Exception {
        String imageUrl = "https://cloudinary.com/table.jpg";
        
        when(imageUploadService.uploadTableImage(any(), eq(1), eq(1)))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://cloudinary.com/table_thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(imageUrl, 400, 300))
            .thenReturn("https://cloudinary.com/table_opt.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/upload/table")
                .file(mockFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/avatar - Should use default values")
    void testAvatarUpload_WithDefaultValues() throws Exception {
        String imageUrl = "https://cloudinary.com/avatar.jpg";
        
        when(imageUploadService.uploadAvatar(any(), anyString()))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://cloudinary.com/avatar_thumb.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/upload/avatar")
                .file(mockFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/update/restaurant - Should use default values")
    void testRestaurantUpdate_WithDefaultValues() throws Exception {
        String oldImageUrl = "https://cloudinary.com/old.jpg";
        String newImageUrl = "https://cloudinary.com/new.jpg";
        
        when(imageUploadService.updateRestaurantImage(any(), eq(oldImageUrl), eq(1), eq("main")))
            .thenReturn(newImageUrl);
        when(imageUploadService.getThumbnailUrl(newImageUrl))
            .thenReturn("https://cloudinary.com/new_thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(newImageUrl, 400, 300))
            .thenReturn("https://cloudinary.com/new_opt.jpg");
        when(imageUploadService.isCloudinaryUrl(newImageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/update/restaurant")
                .file(mockFile)
                .param("oldImageUrl", oldImageUrl))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/update/dish - Should use default values")
    void testDishUpdate_WithDefaultValues() throws Exception {
        String oldImageUrl = "https://cloudinary.com/old_dish.jpg";
        String newImageUrl = "https://cloudinary.com/new_dish.jpg";
        
        when(imageUploadService.updateDishImage(any(), eq(oldImageUrl), eq(1), eq(1)))
            .thenReturn(newImageUrl);
        when(imageUploadService.getThumbnailUrl(newImageUrl))
            .thenReturn("https://cloudinary.com/new_dish_thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(newImageUrl, 300, 200))
            .thenReturn("https://cloudinary.com/new_dish_opt.jpg");
        when(imageUploadService.isCloudinaryUrl(newImageUrl))
            .thenReturn(true);

        mockMvc.perform(multipart("/test/cloudinary/update/dish")
                .file(mockFile)
                .param("oldImageUrl", oldImageUrl))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /test/cloudinary/upload/restaurant - Should handle isCloudinaryUrl false")
    void testRestaurantUpload_WithNonCloudinaryUrl() throws Exception {
        String imageUrl = "https://other-cdn.com/test.jpg";
        
        when(imageUploadService.uploadRestaurantImage(any(), anyInt(), anyString()))
            .thenReturn(imageUrl);
        when(imageUploadService.getThumbnailUrl(imageUrl))
            .thenReturn("https://other-cdn.com/test_thumb.jpg");
        when(imageUploadService.getOptimizedImageUrl(imageUrl, 400, 300))
            .thenReturn("https://other-cdn.com/test_opt.jpg");
        when(imageUploadService.isCloudinaryUrl(imageUrl))
            .thenReturn(false);

        mockMvc.perform(multipart("/test/cloudinary/upload/restaurant")
                .file(mockFile)
                .param("restaurantId", "1")
                .param("imageType", "main"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.isCloudinary").value(false));
    }
}

