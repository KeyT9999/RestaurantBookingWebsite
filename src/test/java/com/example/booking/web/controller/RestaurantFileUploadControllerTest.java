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
    void shouldUploadBusinessLicense_successfully() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile("file", "license.pdf", 
            "application/pdf", "test content".getBytes());
        com.example.booking.domain.RestaurantOwner owner = new com.example.booking.domain.RestaurantOwner();
        com.example.booking.domain.User user = new com.example.booking.domain.User();
        user.setUsername("owner@test.com");
        owner.setUser(user);
        restaurant.setOwner(owner);
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(fileUploadService.uploadBusinessLicense(any(MultipartFile.class), eq(1)))
            .thenReturn("/uploads/license.pdf");
        doNothing().when(restaurantOwnerService).updateRestaurantProfile(any(RestaurantProfile.class));
        doNothing().when(restaurantOwnerService).createMedia(any(com.example.booking.domain.RestaurantMedia.class));
        doNothing().when(restaurantApprovalService).notifyNewRestaurantRegistration(any(RestaurantProfile.class));

        // When
        String view = fileUploadController.uploadBusinessLicense(file, 1, principal, redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("business-license"));
        verify(fileUploadService, times(1)).uploadBusinessLicense(file, 1);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenRestaurantNotFoundForUpload")
    void shouldReturnError_whenRestaurantNotFoundForUpload() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "license.pdf", 
            "application/pdf", "test content".getBytes());
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.empty());

        // When
        String view = fileUploadController.uploadBusinessLicense(file, 1, principal, redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("business-license"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenUnauthorizedOwner")
    void shouldReturnError_whenUnauthorizedOwner() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "license.pdf", 
            "application/pdf", "test content".getBytes());
        com.example.booking.domain.RestaurantOwner owner = new com.example.booking.domain.RestaurantOwner();
        com.example.booking.domain.User user = new com.example.booking.domain.User();
        user.setUsername("other@test.com");
        owner.setUser(user);
        restaurant.setOwner(owner);
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));

        // When
        String view = fileUploadController.uploadBusinessLicense(file, 1, principal, redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("business-license"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldHandleIOException_whenUploadFails")
    void shouldHandleIOException_whenUploadFails() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile("file", "license.pdf", 
            "application/pdf", "test content".getBytes());
        com.example.booking.domain.RestaurantOwner owner = new com.example.booking.domain.RestaurantOwner();
        com.example.booking.domain.User user = new com.example.booking.domain.User();
        user.setUsername("owner@test.com");
        owner.setUser(user);
        restaurant.setOwner(owner);
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(fileUploadService.uploadBusinessLicense(any(MultipartFile.class), eq(1)))
            .thenThrow(new java.io.IOException("Upload failed"));

        // When
        String view = fileUploadController.uploadBusinessLicense(file, 1, principal, redirectAttributes);

        // Then
        assertNotNull(view);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldDeleteBusinessLicense_successfully")
    void shouldDeleteBusinessLicense_successfully() {
        // Given
        com.example.booking.domain.RestaurantOwner owner = new com.example.booking.domain.RestaurantOwner();
        com.example.booking.domain.User user = new com.example.booking.domain.User();
        user.setUsername("owner@test.com");
        owner.setUser(user);
        restaurant.setOwner(owner);
        restaurant.setBusinessLicenseFile("/uploads/license.pdf");
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(fileUploadService.deleteFile("/uploads/license.pdf")).thenReturn(true);
        doNothing().when(restaurantOwnerService).updateRestaurantProfile(any(RestaurantProfile.class));
        when(restaurantOwnerService.getMediaByRestaurantAndType(any(RestaurantProfile.class), eq("business_license")))
            .thenReturn(java.util.Collections.emptyList());

        // When
        String view = fileUploadController.deleteBusinessLicense(1, principal, redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("business-license"));
        verify(fileUploadService, times(1)).deleteFile("/uploads/license.pdf");
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenDeleteBusinessLicenseNotFound")
    void shouldReturnError_whenDeleteBusinessLicenseNotFound() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.empty());

        // When
        String view = fileUploadController.deleteBusinessLicense(1, principal, redirectAttributes);

        // Then
        assertNotNull(view);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldUploadContractDocument_successfully")
    void shouldUploadContractDocument_successfully() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile("file", "contract.pdf", 
            "application/pdf", "test content".getBytes());
        com.example.booking.domain.RestaurantOwner owner = new com.example.booking.domain.RestaurantOwner();
        com.example.booking.domain.User user = new com.example.booking.domain.User();
        user.setUsername("owner@test.com");
        owner.setUser(user);
        restaurant.setOwner(owner);
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(fileUploadService.uploadContractDocument(any(MultipartFile.class), eq(1), anyString()))
            .thenReturn("/uploads/contract.pdf");
        doNothing().when(restaurantOwnerService).createMedia(any(com.example.booking.domain.RestaurantMedia.class));

        // When
        String view = fileUploadController.uploadContractDocument(file, 1, "standard", principal, redirectAttributes);

        // Then
        assertNotNull(view);
        assertTrue(view.contains("contract"));
        verify(fileUploadService, times(1)).uploadContractDocument(file, 1, "standard");
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("shouldHandleError_whenUploadContractDocumentFails")
    void shouldHandleError_whenUploadContractDocumentFails() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "contract.pdf", 
            "application/pdf", "test content".getBytes());
        com.example.booking.domain.RestaurantOwner owner = new com.example.booking.domain.RestaurantOwner();
        com.example.booking.domain.User user = new com.example.booking.domain.User();
        user.setUsername("owner@test.com");
        owner.setUser(user);
        restaurant.setOwner(owner);
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        try {
            when(fileUploadService.uploadContractDocument(any(MultipartFile.class), eq(1), anyString()))
                .thenThrow(new RuntimeException("Upload failed"));
        } catch (Exception e) {
            // Mock setup
        }

        // When
        String view = fileUploadController.uploadContractDocument(file, 1, "standard", principal, redirectAttributes);

        // Then
        assertNotNull(view);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldShowExistingLicense_whenLicenseExists")
    void shouldShowExistingLicense_whenLicenseExists() {
        // Given
        restaurant.setBusinessLicenseFile("/uploads/license.pdf");
        FileUploadService.FileInfo fileInfo = new FileUploadService.FileInfo(
            "license.pdf", 1024L, "application/pdf"
        );
        
        when(restaurantOwnerService.getRestaurantByOwnerUsername("owner@test.com"))
            .thenReturn(Optional.of(restaurant));
        when(fileUploadService.getFileInfo("/uploads/license.pdf")).thenReturn(fileInfo);

        // When
        String view = fileUploadController.businessLicenseUploadPage(principal, model);

        // Then
        assertNotNull(view);
        verify(model, times(1)).addAttribute(eq("existingLicense"), eq("/uploads/license.pdf"));
        verify(model, times(1)).addAttribute(eq("fileInfo"), eq(fileInfo));
    }

    @Test
    @DisplayName("shouldHandleUploadProgress_successfully")
    void shouldHandleUploadProgress_successfully() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.pdf", 
            "application/pdf", "test content".getBytes());

        // When
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> response = 
            fileUploadController.uploadProgress(file, "business_license", 1);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("fileInfo"));
    }

    @Test
    @DisplayName("shouldReturnError_whenUploadProgressFileEmpty")
    void shouldReturnError_whenUploadProgressFileEmpty() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "", "", new byte[0]);

        // When
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> response = 
            fileUploadController.uploadProgress(file, "business_license", 1);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertEquals(false, response.getBody().get("success"));
    }

    @Test
    @DisplayName("shouldHandleException_whenShowBusinessLicensePage")
    void shouldHandleException_whenShowBusinessLicensePage() {
        // Given
        when(restaurantOwnerService.getRestaurantByOwnerUsername("owner@test.com"))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = fileUploadController.businessLicenseUploadPage(principal, model);

        // Then
        assertNotNull(view);
        verify(model, times(1)).addAttribute(eq("error"), anyString());
    }
}

