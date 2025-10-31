package com.example.booking.web.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.FileUploadService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantOwnerService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
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
	private Principal principal;

	@Mock
	private Model model;

	@Mock
	private RedirectAttributes redirectAttributes;

	@InjectMocks
	private RestaurantFileUploadController controller;

	private User testUser;
	private RestaurantOwner testRestaurantOwner;
	private RestaurantProfile testRestaurant;
	private FileUploadService.FileInfo testFileInfo;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(UUID.randomUUID());
		testUser.setUsername("owner@example.com");
		testUser.setEmail("owner@example.com");
		testUser.setFullName("Restaurant Owner");
		testUser.setRole(UserRole.RESTAURANT_OWNER);

		testRestaurantOwner = new RestaurantOwner(testUser);
		testRestaurantOwner.setOwnerId(UUID.randomUUID());
		testRestaurantOwner.setOwnerName("Restaurant Owner");

		testRestaurant = new RestaurantProfile();
		testRestaurant.setRestaurantId(1);
		testRestaurant.setRestaurantName("Test Restaurant");
		testRestaurant.setOwner(testRestaurantOwner);

		testFileInfo = new FileUploadService.FileInfo("license.pdf", 1024L, "application/pdf");
	}

	// ========== businessLicenseUploadPage() Tests ==========

	@Test
	@DisplayName("businessLicenseUploadPage - should return upload page")
	void businessLicenseUploadPage_ShouldReturnPage() {
		// Given
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantByOwnerUsername(testUser.getUsername()))
				.thenReturn(Optional.of(testRestaurant));
		when(fileUploadService.getFileInfo(anyString())).thenReturn(testFileInfo);

		// When
		String result = controller.businessLicenseUploadPage(principal, model);

		// Then
		assertEquals("restaurant-owner/business-license-upload", result);
		verify(model).addAttribute("restaurant", testRestaurant);
	}

	@Test
	@DisplayName("businessLicenseUploadPage - should handle restaurant not found")
	void businessLicenseUploadPage_RestaurantNotFound_ShouldReturnError() {
		// Given
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantByOwnerUsername(testUser.getUsername()))
				.thenReturn(Optional.empty());

		// When
		String result = controller.businessLicenseUploadPage(principal, model);

		// Then
		assertEquals("restaurant-owner/file-upload", result);
		verify(model).addAttribute(eq("error"), anyString());
	}

	@Test
	@DisplayName("businessLicenseUploadPage - should load file info when license exists")
	void businessLicenseUploadPage_WithExistingLicense_ShouldLoadFileInfo() {
		// Given
		String licenseUrl = "http://example.com/license.pdf";
		testRestaurant.setBusinessLicenseFile(licenseUrl);
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantByOwnerUsername(testUser.getUsername()))
				.thenReturn(Optional.of(testRestaurant));
		when(fileUploadService.getFileInfo(licenseUrl)).thenReturn(testFileInfo);

		// When
		String result = controller.businessLicenseUploadPage(principal, model);

		// Then
		assertEquals("restaurant-owner/business-license-upload", result);
		verify(model).addAttribute("fileInfo", testFileInfo);
		verify(model).addAttribute("existingLicense", licenseUrl);
	}

	// ========== uploadBusinessLicense() Tests ==========

	@Test
	@DisplayName("uploadBusinessLicense - should upload successfully")
	void uploadBusinessLicense_WithValidFile_ShouldUpload() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "license.pdf", "application/pdf", "content".getBytes());
		String fileUrl = "http://example.com/license.pdf";
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));
		try {
			when(fileUploadService.uploadBusinessLicense(file, 1)).thenReturn(fileUrl);
		} catch (IOException e) {
			// Mock handles this
		}
		when(restaurantOwnerService.updateRestaurantProfile(any(RestaurantProfile.class)))
				.thenReturn(testRestaurant);

		// When
		String result = controller.uploadBusinessLicense(file, 1, principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/business-license"));
		verify(restaurantOwnerService).updateRestaurantProfile(any(RestaurantProfile.class));
		verify(restaurantOwnerService).createMedia(any(RestaurantMedia.class));
		verify(restaurantApprovalService).notifyNewRestaurantRegistration(testRestaurant);
		verify(redirectAttributes).addFlashAttribute("success", anyString());
	}

	@Test
	@DisplayName("uploadBusinessLicense - should handle restaurant not found")
	void uploadBusinessLicense_RestaurantNotFound_ShouldRedirect() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "license.pdf", "application/pdf", "content".getBytes());
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantById(999)).thenReturn(Optional.empty());

		// When
		String result = controller.uploadBusinessLicense(file, 999, principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/business-license"));
		verify(redirectAttributes).addFlashAttribute("error", anyString());
	}

	@Test
	@DisplayName("uploadBusinessLicense - should handle unauthorized access")
	void uploadBusinessLicense_Unauthorized_ShouldRedirect() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "license.pdf", "application/pdf", "content".getBytes());
		User differentUser = new User();
		differentUser.setUsername("different@example.com");
		when(principal.getName()).thenReturn("different@example.com");
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));

		// When
		String result = controller.uploadBusinessLicense(file, 1, principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/business-license"));
		verify(redirectAttributes).addFlashAttribute("error", anyString());
	}

	@Test
	@DisplayName("uploadBusinessLicense - should handle IOException")
	void uploadBusinessLicense_IOException_ShouldHandle() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "license.pdf", "application/pdf", "content".getBytes());
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));
		try {
			doThrow(new IOException("Upload failed")).when(fileUploadService).uploadBusinessLicense(file, 1);
		} catch (IOException e) {
			// Mock handles this
		}

		// When
		String result = controller.uploadBusinessLicense(file, 1, principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/business-license"));
		verify(redirectAttributes).addFlashAttribute("error", anyString());
	}

	// ========== deleteBusinessLicense() Tests ==========

	@Test
	@DisplayName("deleteBusinessLicense - should delete successfully")
	void deleteBusinessLicense_WithValidId_ShouldDelete() {
		// Given
		String licenseUrl = "http://example.com/license.pdf";
		testRestaurant.setBusinessLicenseFile(licenseUrl);
		RestaurantMedia media = new RestaurantMedia();
		media.setMediaId(1);
		media.setType("business_license");
		media.setUrl(licenseUrl);

		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));
		when(fileUploadService.deleteFile(licenseUrl)).thenReturn(true);
		when(restaurantOwnerService.getMediaByRestaurantAndType(testRestaurant, "business_license"))
				.thenReturn(Collections.singletonList(media));
		when(restaurantOwnerService.updateRestaurantProfile(any(RestaurantProfile.class)))
				.thenReturn(testRestaurant);

		// When
		String result = controller.deleteBusinessLicense(1, principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/business-license"));
		verify(fileUploadService).deleteFile(licenseUrl);
		verify(restaurantOwnerService).deleteMedia(1);
		verify(redirectAttributes).addFlashAttribute("success", anyString());
	}

	@Test
	@DisplayName("deleteBusinessLicense - should handle restaurant not found")
	void deleteBusinessLicense_RestaurantNotFound_ShouldRedirect() {
		// Given
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantById(999)).thenReturn(Optional.empty());

		// When
		String result = controller.deleteBusinessLicense(999, principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/business-license"));
		verify(redirectAttributes).addFlashAttribute("error", anyString());
	}

	@Test
	@DisplayName("deleteBusinessLicense - should handle no file to delete")
	void deleteBusinessLicense_NoFile_ShouldRedirect() {
		// Given
		testRestaurant.setBusinessLicenseFile(null);
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));

		// When
		String result = controller.deleteBusinessLicense(1, principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/business-license"));
		verify(redirectAttributes).addFlashAttribute("error", anyString());
	}

	// ========== uploadContractDocument() Tests ==========

	@Test
	@DisplayName("uploadContractDocument - should upload successfully")
	void uploadContractDocument_WithValidFile_ShouldUpload() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", "content".getBytes());
		String fileUrl = "http://example.com/contract.pdf";
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));
		try {
			when(fileUploadService.uploadContractDocument(file, 1, "lease")).thenReturn(fileUrl);
		} catch (IOException e) {
			// Mock handles this
		}

		// When
		String result = controller.uploadContractDocument(file, 1, "lease", principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/contract"));
		verify(restaurantOwnerService).createMedia(any(RestaurantMedia.class));
		verify(redirectAttributes).addFlashAttribute("success", anyString());
	}

	@Test
	@DisplayName("uploadContractDocument - should handle restaurant not found")
	void uploadContractDocument_RestaurantNotFound_ShouldRedirect() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", "content".getBytes());
		when(principal.getName()).thenReturn(testUser.getUsername());
		when(restaurantOwnerService.getRestaurantById(999)).thenReturn(Optional.empty());

		// When
		String result = controller.uploadContractDocument(file, 999, "lease", principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/contract"));
		verify(redirectAttributes).addFlashAttribute("error", anyString());
	}

	@Test
	@DisplayName("uploadContractDocument - should handle unauthorized access")
	void uploadContractDocument_Unauthorized_ShouldRedirect() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", "content".getBytes());
		when(principal.getName()).thenReturn("different@example.com");
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));

		// When
		String result = controller.uploadContractDocument(file, 1, "lease", principal, redirectAttributes);

		// Then
		assertTrue(result.contains("redirect:/restaurant-owner/files/contract"));
		verify(redirectAttributes).addFlashAttribute("error", anyString());
	}

	// ========== uploadProgress() Tests ==========

	@Test
	@DisplayName("uploadProgress - should return file info for valid file")
	void uploadProgress_WithValidFile_ShouldReturnFileInfo() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

		// When
		ResponseEntity<Map<String, Object>> result = controller.uploadProgress(file, "business_license", 1);

		// Then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		Map<String, Object> body = result.getBody();
		assertNotNull(body);
		assertEquals(true, body.get("success"));
		assertNotNull(body.get("fileInfo"));
	}

	@Test
	@DisplayName("uploadProgress - should return error for empty file")
	void uploadProgress_WithEmptyFile_ShouldReturnError() {
		// Given
		MockMultipartFile emptyFile = new MockMultipartFile("file", "", "application/pdf", new byte[0]);

		// When
		ResponseEntity<Map<String, Object>> result = controller.uploadProgress(emptyFile, "business_license", 1);

		// Then
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
		Map<String, Object> body = result.getBody();
		assertNotNull(body);
		assertEquals(false, body.get("success"));
	}

	@Test
	@DisplayName("uploadProgress - should return success for valid file")
	void uploadProgress_WithValidFile_ShouldReturnSuccess() {
		// Given
		MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
		
		// When
		ResponseEntity<Map<String, Object>> result = controller.uploadProgress(file, "business_license", 1);

		// Then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		Map<String, Object> body = result.getBody();
		assertNotNull(body);
		assertEquals(true, body.get("success"));
	}
}

