package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.service.FileUploadService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantOwnerService;

/**
 * Unit tests for RestaurantFileUploadController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantFileUploadController Tests")
public class RestaurantFileUploadControllerTest {

    @Mock
    private FileUploadService fileUploadService;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private RestaurantApprovalService restaurantApprovalService;

    @Mock
    private Model model;

    @Mock
    private Principal principal;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private RestaurantFileUploadController fileUploadController;

    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        when(principal.getName()).thenReturn("owner@test.com");
    }

    // ========== businessLicenseUploadPage() Tests ==========

    @Test
    @DisplayName("shouldShowBusinessLicenseUploadPage_successfully")
    void shouldShowBusinessLicenseUploadPage_successfully() {
        // Given
        when(restaurantOwnerService.getRestaurantByOwnerUsername("owner@test.com"))
            .thenReturn(Optional.of(restaurant));

        // When
        String view = fileUploadController.businessLicenseUploadPage(principal, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldShowError_whenRestaurantNotFound")
    void shouldShowError_whenRestaurantNotFound() {
        // Given
        when(restaurantOwnerService.getRestaurantByOwnerUsername("owner@test.com"))
            .thenReturn(Optional.empty());

        // When
        String view = fileUploadController.businessLicenseUploadPage(principal, model);

        // Then
        assertNotNull(view);
        verify(model, times(1)).addAttribute(eq("error"), anyString());
    }

    // ========== uploadBusinessLicense() Tests ==========

    @Test
    @DisplayName("shouldUploadBusinessLicense_successfully")
    void shouldUploadBusinessLicense_successfully() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "license.pdf", 
            "application/pdf", "test content".getBytes());
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        try {
            when(fileUploadService.uploadBusinessLicense(any(MultipartFile.class), eq(1)))
                .thenReturn("/uploads/license.pdf");
        } catch (java.io.IOException e) {
            // Mock setup
        }

        // When
        String view = null;
        try {
            view = fileUploadController.uploadBusinessLicense(file, 1, principal, redirectAttributes);
        } catch (Exception e) {
            // IOException is handled inside the controller
        }

        // Then
        assertNotNull(view);
        try {
            verify(fileUploadService, times(1)).uploadBusinessLicense(file, 1);
        } catch (java.io.IOException e) {
            // IOException is handled inside controller
        }
    }
}

