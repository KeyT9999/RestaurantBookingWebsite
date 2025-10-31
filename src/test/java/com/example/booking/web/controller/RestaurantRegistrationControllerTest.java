package com.example.booking.web.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantNotificationService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("RestaurantRegistrationController Tests")
public class RestaurantRegistrationControllerTest {

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private RestaurantNotificationService restaurantNotificationService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private RestaurantRegistrationController restaurantRegistrationController;

    private User testCustomer;
    private User testRestaurantOwner;
    private User testAdmin;
    private RestaurantOwner testRestaurantOwnerEntity;

    @BeforeEach
    public void setUp() {
        // Create test customer user
        testCustomer = new User();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setUsername("customer@example.com");
        testCustomer.setEmail("customer@example.com");
        testCustomer.setFullName("Test Customer");
        testCustomer.setRole(UserRole.CUSTOMER);
        testCustomer.setEmailVerified(true);

        // Create test restaurant owner user
        testRestaurantOwner = new User();
        testRestaurantOwner.setId(UUID.randomUUID());
        testRestaurantOwner.setUsername("owner@example.com");
        testRestaurantOwner.setEmail("owner@example.com");
        testRestaurantOwner.setFullName("Test Owner");
        testRestaurantOwner.setRole(UserRole.RESTAURANT_OWNER);
        testRestaurantOwner.setEmailVerified(true);

        // Create test admin user
        testAdmin = new User();
        testAdmin.setId(UUID.randomUUID());
        testAdmin.setUsername("admin@example.com");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setFullName("Admin");
        testAdmin.setRole(UserRole.ADMIN);
        testAdmin.setEmailVerified(true);

        // Create test restaurant owner entity
        testRestaurantOwnerEntity = new RestaurantOwner(testCustomer);
        testRestaurantOwnerEntity.setOwnerId(UUID.randomUUID());
    }

    // ========== showRegistrationForm() (createRestaurantForm) Tests ==========

    @Test
    @DisplayName("Should return registration form for authenticated customer")
    public void testShowRegistrationForm_WithAuthenticatedCustomer_ShouldReturnRegistrationForm() {
        // Given
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));

        // When
        String result = restaurantRegistrationController.createRestaurantForm(model, authentication, null);

        // Then
        assertEquals("restaurant-owner/restaurant-form", result);
    }

    @Test
    @DisplayName("Should return registration form for authenticated restaurant owner")
    public void testShowRegistrationForm_WithAuthenticatedRestaurantOwner_ShouldReturnRegistrationForm() {
        // Given
        when(authentication.getName()).thenReturn(testRestaurantOwner.getUsername());
        when(authentication.getPrincipal()).thenReturn(testRestaurantOwner);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(userService.findByUsername(testRestaurantOwner.getUsername()))
            .thenReturn(Optional.of(testRestaurantOwner));

        // When
        String result = restaurantRegistrationController.createRestaurantForm(model, authentication, null);

        // Then
        assertEquals("restaurant-owner/restaurant-form", result);
    }

    @Test
    @DisplayName("Should redirect to login for unauthenticated user")
    public void testShowRegistrationForm_WithUnauthenticatedUser_ShouldRedirectToLogin() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        String result = restaurantRegistrationController.createRestaurantForm(model, authentication, null);

        // Then
        assertEquals("redirect:/login", result);
    }

    @Test
    @DisplayName("Should redirect with error for invalid role (ADMIN)")
    public void testShowRegistrationForm_WithInvalidRole_ShouldRedirectWithError() {
        // Given
        when(authentication.getName()).thenReturn(testAdmin.getUsername());
        when(authentication.getPrincipal()).thenReturn(testAdmin);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        
        when(userService.findByUsername(testAdmin.getUsername()))
            .thenReturn(Optional.of(testAdmin));

        // When
        String result = restaurantRegistrationController.createRestaurantForm(model, authentication, null);

        // Then
        assertEquals("redirect:/?error=unauthorized", result);
    }

    @Test
    @DisplayName("Should display info message for message parameter")
    public void testShowRegistrationForm_WithMessageParameter_ShouldDisplayInfoMessage() {
        // Given
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));

        // When
        String result = restaurantRegistrationController.createRestaurantForm(model, authentication, "no_approved_restaurant");

        // Then
        assertEquals("restaurant-owner/restaurant-form", result);
    }

    @Test
    @DisplayName("Should redirect to login for null authentication")
    public void testShowRegistrationForm_WithNullAuthentication_ShouldReturnLoginRedirect() {
        // Given
        Authentication nullAuth = null;

        // When
        String result = restaurantRegistrationController.createRestaurantForm(model, nullAuth, null);

        // Then
        assertEquals("redirect:/login", result);
    }

    // ========== submitRegistration() (createRestaurant) Tests ==========

    @Test
    @DisplayName("Should create restaurant with valid data and files")
    public void testSubmitRegistration_WithValidDataAndFiles_ShouldCreateRestaurant() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("Test Address");
        
        MockMultipartFile logo = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile cover = new MockMultipartFile("cover", "cover.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile businessLicense = new MockMultipartFile("businessLicense", "license.pdf", "application/pdf", new byte[]{1, 2, 3});
        
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));
        
        when(restaurantOwnerService.ensureRestaurantOwnerExists(testCustomer.getId()))
            .thenReturn(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testCustomer.getId()))
            .thenReturn(Optional.of(testRestaurantOwnerEntity));
        
        RestaurantProfile savedRestaurant = new RestaurantProfile();
        savedRestaurant.setRestaurantId(1);
        savedRestaurant.setRestaurantName("Test Restaurant");
        savedRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        savedRestaurant.setOwner(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.createRestaurantProfile(any(RestaurantProfile.class)))
            .thenReturn(savedRestaurant);
        
        try {
            when(imageUploadService.uploadRestaurantImage(any(), any(), any()))
                .thenReturn("http://example.com/logo.jpg");
        } catch (IOException e) {
            // Mock handles this
        }

        // When
        String result = restaurantRegistrationController.createRestaurant(
            restaurant, logo, cover, businessLicense, true, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurant-owner/restaurants/create?success=1", result);
    }

    @Test
    @DisplayName("Should create restaurant without files")
    public void testSubmitRegistration_WithValidDataOnly_ShouldCreateRestaurantWithoutFiles() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("Test Address");
        
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));
        
        when(restaurantOwnerService.ensureRestaurantOwnerExists(testCustomer.getId()))
            .thenReturn(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testCustomer.getId()))
            .thenReturn(Optional.of(testRestaurantOwnerEntity));
        
        RestaurantProfile savedRestaurant = new RestaurantProfile();
        savedRestaurant.setRestaurantId(1);
        savedRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        savedRestaurant.setOwner(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.createRestaurantProfile(any(RestaurantProfile.class)))
            .thenReturn(savedRestaurant);

        // When
        String result = restaurantRegistrationController.createRestaurant(
            restaurant, null, null, null, true, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurant-owner/restaurants/create?success=1", result);
    }

    @Test
    @DisplayName("Should reject registration without terms accepted")
    public void testSubmitRegistration_WithoutTermsAccepted_ShouldRejectRegistration() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));

        // When
        String result = restaurantRegistrationController.createRestaurant(
            restaurant, null, null, null, false, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurant-owner/restaurants/create", result);
    }

    @Test
    @DisplayName("Should create restaurant and accept terms")
    public void testSubmitRegistration_ShouldAcceptTermsVersion_ShouldCreateRestaurant() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));
        
        when(restaurantOwnerService.ensureRestaurantOwnerExists(testCustomer.getId()))
            .thenReturn(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testCustomer.getId()))
            .thenReturn(Optional.of(testRestaurantOwnerEntity));
        
        RestaurantProfile savedRestaurant = new RestaurantProfile();
        savedRestaurant.setRestaurantId(1);
        savedRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        savedRestaurant.setOwner(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.createRestaurantProfile(any(RestaurantProfile.class)))
            .thenReturn(savedRestaurant);

        // When
        String result = restaurantRegistrationController.createRestaurant(
            restaurant, null, null, null, true, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurant-owner/restaurants/create?success=1", result);
        assertNotNull(savedRestaurant);
    }

    @Test
    @DisplayName("Should ensure restaurant owner exists")
    public void testSubmitRegistration_WithOwnerCreation_ShouldEnsureRestaurantOwnerExists() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));
        
        when(restaurantOwnerService.ensureRestaurantOwnerExists(testCustomer.getId()))
            .thenReturn(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testCustomer.getId()))
            .thenReturn(Optional.of(testRestaurantOwnerEntity));
        
        RestaurantProfile savedRestaurant = new RestaurantProfile();
        savedRestaurant.setRestaurantId(1);
        savedRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        savedRestaurant.setOwner(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.createRestaurantProfile(any(RestaurantProfile.class)))
            .thenReturn(savedRestaurant);

        // When
        String result = restaurantRegistrationController.createRestaurant(
            restaurant, null, null, null, true, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurant-owner/restaurants/create?success=1", result);
    }

    @Test
    @DisplayName("Should handle database exception gracefully")
    public void testSubmitRegistration_WithDatabaseException_ShouldHandleGracefully() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));
        
        when(restaurantOwnerService.ensureRestaurantOwnerExists(testCustomer.getId()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String result = restaurantRegistrationController.createRestaurant(
            restaurant, null, null, null, true, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurant-owner/restaurants/create", result);
    }

    @Test
    @DisplayName("Should not duplicate restaurant owner")
    public void testSubmitRegistration_WithAlreadyExistedRestaurantOwner_ShouldNotDuplicate() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));
        
        when(restaurantOwnerService.ensureRestaurantOwnerExists(testCustomer.getId()))
            .thenReturn(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testCustomer.getId()))
            .thenReturn(Optional.of(testRestaurantOwnerEntity));
        
        RestaurantProfile savedRestaurant = new RestaurantProfile();
        savedRestaurant.setRestaurantId(1);
        savedRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        savedRestaurant.setOwner(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.createRestaurantProfile(any(RestaurantProfile.class)))
            .thenReturn(savedRestaurant);

        // When
        String result = restaurantRegistrationController.createRestaurant(
            restaurant, null, null, null, true, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurant-owner/restaurants/create?success=1", result);
    }

    @Test
    @DisplayName("Should create media records with upload")
    public void testSubmitRegistration_WithMediaUpload_ShouldCreateMediaRecords() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        
        MockMultipartFile logo = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", "test".getBytes());
        MockMultipartFile cover = new MockMultipartFile("cover", "cover.jpg", "image/jpeg", "test".getBytes());
        
        when(authentication.getName()).thenReturn(testCustomer.getUsername());
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        
        when(userService.findByUsername(testCustomer.getUsername()))
            .thenReturn(Optional.of(testCustomer));
        
        when(restaurantOwnerService.ensureRestaurantOwnerExists(testCustomer.getId()))
            .thenReturn(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testCustomer.getId()))
            .thenReturn(Optional.of(testRestaurantOwnerEntity));
        
        RestaurantProfile savedRestaurant = new RestaurantProfile();
        savedRestaurant.setRestaurantId(1);
        savedRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        savedRestaurant.setOwner(testRestaurantOwnerEntity);
        
        when(restaurantOwnerService.createRestaurantProfile(any(RestaurantProfile.class)))
            .thenReturn(savedRestaurant);
        
        try {
            when(imageUploadService.uploadRestaurantImage(any(), any(), any()))
                .thenReturn("http://example.com/image.jpg");
        } catch (IOException e) {
            // Mock handles this
        }

        // When
        String result = restaurantRegistrationController.createRestaurant(
            restaurant, logo, cover, null, true, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurant-owner/restaurants/create?success=1", result);
    }

    // ========== editRestaurantForm() Tests ==========

    @Test
    @DisplayName("editRestaurantForm - should return edit form for valid restaurant")
    void editRestaurantForm_WithValidId_ShouldReturnForm() {
        // Given
        Integer restaurantId = 1;
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        restaurant.setOwner(testRestaurantOwnerEntity);
        testRestaurantOwnerEntity.getUser().setId(testCustomer.getId());

        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        when(restaurantOwnerService.getRestaurantByIdForAdmin(restaurantId))
                .thenReturn(Optional.of(restaurant));

        // When
        String result = restaurantRegistrationController.editRestaurantForm(restaurantId, model, authentication);

        // Then
        assertEquals("restaurant-owner/restaurant-form", result);
    }

    @Test
    @DisplayName("editRestaurantForm - should redirect when restaurant not found")
    void editRestaurantForm_RestaurantNotFound_ShouldRedirect() {
        // Given
        Integer restaurantId = 999;
        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        when(restaurantOwnerService.getRestaurantByIdForAdmin(restaurantId))
                .thenReturn(Optional.empty());

        // When
        String result = restaurantRegistrationController.editRestaurantForm(restaurantId, model, authentication);

        // Then
        assertEquals("redirect:/?error=restaurant_not_found", result);
    }

    @Test
    @DisplayName("editRestaurantForm - should redirect when not owner")
    void editRestaurantForm_NotOwner_ShouldRedirect() {
        // Given
        Integer restaurantId = 1;
        UUID differentUserId = UUID.randomUUID();
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        User differentUser = new User();
        differentUser.setId(differentUserId);
        RestaurantOwner differentOwner = new RestaurantOwner(differentUser);
        restaurant.setOwner(differentOwner);

        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        when(restaurantOwnerService.getRestaurantByIdForAdmin(restaurantId))
                .thenReturn(Optional.of(restaurant));

        // When
        String result = restaurantRegistrationController.editRestaurantForm(restaurantId, model, authentication);

        // Then
        assertEquals("redirect:/?error=unauthorized", result);
    }

    @Test
    @DisplayName("editRestaurantForm - should redirect when restaurant is approved")
    void editRestaurantForm_ApprovedRestaurant_ShouldRedirect() {
        // Given
        Integer restaurantId = 1;
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        restaurant.setOwner(testRestaurantOwnerEntity);
        testRestaurantOwnerEntity.getUser().setId(testCustomer.getId());

        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        when(restaurantOwnerService.getRestaurantByIdForAdmin(restaurantId))
                .thenReturn(Optional.of(restaurant));

        // When
        String result = restaurantRegistrationController.editRestaurantForm(restaurantId, model, authentication);

        // Then
        assertTrue(result.contains("redirect"));
    }

    // ========== updateRestaurant() Tests ==========

    @Test
    @DisplayName("updateRestaurant - should update restaurant successfully")
    void updateRestaurant_WithValidData_ShouldUpdate() {
        // Given
        Integer restaurantId = 1;
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Updated Restaurant");
        restaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        restaurant.setOwner(testRestaurantOwnerEntity);
        testRestaurantOwnerEntity.getUser().setId(testCustomer.getId());

        when(authentication.getPrincipal()).thenReturn(testCustomer);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(restaurantOwnerService.getRestaurantByIdForAdmin(restaurantId))
                .thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.updateRestaurantProfile(any(RestaurantProfile.class)))
                .thenReturn(restaurant);

        // When
        String result = restaurantRegistrationController.updateRestaurant(
                restaurantId, restaurant, null, null, null, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
    }

    @Test
    @DisplayName("updateRestaurant - should redirect to login when unauthenticated")
    void updateRestaurant_Unauthenticated_ShouldRedirectToLogin() {
        // Given
        Integer restaurantId = 1;
        RestaurantProfile restaurant = new RestaurantProfile();
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        String result = restaurantRegistrationController.updateRestaurant(
                restaurantId, restaurant, null, null, null, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/login", result);
    }
}
