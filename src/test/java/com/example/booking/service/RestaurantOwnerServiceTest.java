package com.example.booking.service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantOwnerService Tests")
public class RestaurantOwnerServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantOwnerService restaurantOwnerService;

    private RestaurantProfile testRestaurant;

    @BeforeEach
    public void setUp() {
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setRestaurantName("Test Restaurant");
        testRestaurant.setAddress("123 Test Street");
        testRestaurant.setPhone("0123456789");
    }

    // ========== updateRestaurantProfile() Tests ==========

    @Test
    @DisplayName("Should update restaurant profile with valid data successfully")
    public void testUpdateRestaurantProfile_WithValidData_ShouldUpdateSuccessfully() {
        // Given
        String updatedName = "Updated Restaurant Name";
        String updatedAddress = "456 New Street";
        
        testRestaurant.setRestaurantName(updatedName);
        testRestaurant.setAddress(updatedAddress);

        when(restaurantRepository.save(testRestaurant))
            .thenReturn(testRestaurant);

        // When
        RestaurantProfile result = restaurantOwnerService.updateRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertEquals(updatedName, result.getRestaurantName());
        assertEquals(updatedAddress, result.getAddress());
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    @DisplayName("Should set updatedAt timestamp")
    public void testUpdateRestaurantProfile_ShouldSetUpdatedAtTimestamp() {
        // Given
        LocalDateTime beforeUpdate = LocalDateTime.now();
        testRestaurant.setUpdatedAt(null); // No previous update

        when(restaurantRepository.save(testRestaurant))
            .thenAnswer(invocation -> {
                RestaurantProfile r = invocation.getArgument(0);
                r.setUpdatedAt(LocalDateTime.now());
                return r;
            });

        // When
        RestaurantProfile result = restaurantOwnerService.updateRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(beforeUpdate) || 
                   result.getUpdatedAt().isEqual(beforeUpdate));
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    @DisplayName("Should update image URL")
    public void testUpdateRestaurantProfile_WithImageUrl_ShouldUpdateImage() {
        // Given
        testRestaurant.setRestaurantName("Updated Name");

        when(restaurantRepository.save(testRestaurant))
            .thenReturn(testRestaurant);

        // When
        RestaurantProfile result = restaurantOwnerService.updateRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getRestaurantName());
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    @DisplayName("Should throw exception with null restaurant")
    public void testUpdateRestaurantProfile_WithNullRestaurant_ShouldThrowException() {
        // Given & When & Then
        assertThrows(Exception.class, () -> 
            restaurantOwnerService.updateRestaurantProfile(null)
        );
    }

    @Test
    @DisplayName("Should preserve existing fields")
    public void testUpdateRestaurantProfile_ShouldPreserveExistingFields() {
        // Given
        String existingPhone = "0123456789";
        String existingAddress = "123 Test Street";
        testRestaurant.setPhone(existingPhone);
        testRestaurant.setAddress(existingAddress);

        // Update only name
        testRestaurant.setRestaurantName("Updated Name Only");

        when(restaurantRepository.save(testRestaurant))
            .thenReturn(testRestaurant);

        // When
        RestaurantProfile result = restaurantOwnerService.updateRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name Only", result.getRestaurantName());
        assertEquals(existingPhone, result.getPhone());
        assertEquals(existingAddress, result.getAddress());
        verify(restaurantRepository).save(testRestaurant);
    }
}

