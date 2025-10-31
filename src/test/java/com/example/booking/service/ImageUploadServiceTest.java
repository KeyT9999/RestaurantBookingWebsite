package com.example.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageUploadService imageUploadService;

    @BeforeEach
    void setUp() {
        // Basic setup if needed
    }

    @Test
    void shouldUploadImage() throws IOException {
        String expectedUrl = "https://cloudinary.com/image.jpg";
        when(cloudinaryService.uploadImage(any(MultipartFile.class), eq("folder"), eq("publicId")))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadImage(multipartFile, "folder", "publicId");

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadImage(multipartFile, "folder", "publicId");
    }

    @Test
    void shouldUploadRestaurantImage() throws IOException {
        String expectedUrl = "https://cloudinary.com/restaurant.jpg";
        when(cloudinaryService.uploadRestaurantImage(any(MultipartFile.class), eq(1), eq("main"), eq(1)))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadRestaurantImage(multipartFile, 1, "main", 1);

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadRestaurantImage(multipartFile, 1, "main", 1);
    }

    @Test
    void shouldUploadRestaurantImage_SingleImage() throws IOException {
        String expectedUrl = "https://cloudinary.com/restaurant.jpg";
        when(cloudinaryService.uploadRestaurantImage(any(MultipartFile.class), eq(1), eq("main")))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadRestaurantImage(multipartFile, 1, "main");

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadRestaurantImage(multipartFile, 1, "main");
    }

    @Test
    void shouldUploadBusinessLicense() throws IOException {
        String expectedUrl = "https://cloudinary.com/license.pdf";
        when(cloudinaryService.uploadBusinessLicense(any(MultipartFile.class), eq(1)))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadBusinessLicense(multipartFile, 1);

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadBusinessLicense(multipartFile, 1);
    }

    @Test
    void shouldUploadDishImage() throws IOException {
        String expectedUrl = "https://cloudinary.com/dish.jpg";
        when(cloudinaryService.uploadDishImage(any(MultipartFile.class), eq(1), eq(100)))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadDishImage(multipartFile, 1, 100);

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadDishImage(multipartFile, 1, 100);
    }

    @Test
    void shouldUploadTableImage() throws IOException {
        String expectedUrl = "https://cloudinary.com/table.jpg";
        when(cloudinaryService.uploadTableImage(any(MultipartFile.class), eq(1), eq(10), eq(1)))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadTableImage(multipartFile, 1, 10, 1);

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadTableImage(multipartFile, 1, 10, 1);
    }

    @Test
    void shouldUploadTableImage_DefaultImageNumber() throws IOException {
        String expectedUrl = "https://cloudinary.com/table.jpg";
        when(cloudinaryService.uploadTableImage(any(MultipartFile.class), eq(1), eq(10)))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadTableImage(multipartFile, 1, 10);

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadTableImage(multipartFile, 1, 10);
    }

    @Test
    void shouldUploadAvatar() throws IOException {
        String expectedUrl = "https://cloudinary.com/avatar.jpg";
        when(cloudinaryService.uploadAvatar(any(MultipartFile.class), eq(100)))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadAvatar(multipartFile, 100);

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadAvatar(multipartFile, 100);
    }

    @Test
    void shouldUploadReviewEvidence() throws IOException {
        String expectedUrl = "https://cloudinary.com/review.jpg";
        when(cloudinaryService.uploadReviewEvidence(any(MultipartFile.class), eq(50)))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadReviewEvidence(multipartFile, 50);

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadReviewEvidence(multipartFile, 50);
    }

    @Test
    void shouldUploadServiceImage() throws IOException {
        String expectedUrl = "https://cloudinary.com/service.jpg";
        when(cloudinaryService.uploadServiceImage(any(MultipartFile.class), eq(1), eq(200)))
                .thenReturn(expectedUrl);

        String result = imageUploadService.uploadServiceImage(multipartFile, 1, 200);

        assertThat(result).isEqualTo(expectedUrl);
        verify(cloudinaryService).uploadServiceImage(multipartFile, 1, 200);
    }
}

