package com.example.booking.web.controller.customer;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.dto.customer.FavoriteRestaurantDto;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FavoriteController.
 * Coverage Target: 90%
 * Test Cases: 15
 *
 * @author Professional Test Engineer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteController Tests")
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private FavoriteController favoriteController;

    private User testUser;
    private Customer testCustomer;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        // Setup test user and customer
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");

        customerId = UUID.randomUUID();
        testCustomer = new Customer();
        testCustomer.setCustomerId(customerId);
        testCustomer.setUser(testUser);
    }

    @Nested
    @DisplayName("Favorites Page Tests")
    class FavoritesPageTests {

        @Test
        @DisplayName("Should display favorites page successfully")
        void favoritesPage_AuthenticatedUser_ReturnsPage() {
            // Given
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));

            Page<FavoriteRestaurantDto> favoritesPage = new PageImpl<>(List.of());
            when(favoriteService.getFavoriteRestaurantsWithFilters(
                any(), any(Pageable.class), any(), any(), any(), any()
            )).thenReturn(favoritesPage);

            // When
            String viewName = favoriteController.favoritesPage(
                0, 12, "createdAt", "desc", null, null, null, null, null, null, null, null,
                authentication, model
            );

            // Then
            assertThat(viewName).isEqualTo("customer/favorites-advanced");
            verify(model).addAttribute("favorites", favoritesPage);
            verify(model).addAttribute("totalElements", 0L);
            verify(model).addAttribute("currentPage", 0);
        }

        @Test
        @DisplayName("Should redirect to login when not authenticated")
        void favoritesPage_NotAuthenticated_RedirectsToLogin() {
            // Given
            when(authentication.getPrincipal()).thenReturn(null);

            // When
            String viewName = favoriteController.favoritesPage(
                0, 12, "createdAt", "desc", null, null, null, null, null, null, null, null,
                authentication, model
            );

            // Then
            assertThat(viewName).isEqualTo("redirect:/login");
            verify(favoriteService, never()).getFavoriteRestaurantsWithFilters(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should redirect to login when customer not found")
        void favoritesPage_CustomerNotFound_RedirectsToLogin() {
            // Given
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

            // When
            String viewName = favoriteController.favoritesPage(
                0, 12, "createdAt", "desc", null, null, null, null, null, null, null, null,
                authentication, model
            );

            // Then
            assertThat(viewName).isEqualTo("redirect:/login");
        }

        @Test
        @DisplayName("Should handle filters correctly")
        void favoritesPage_WithFilters_AppliesFilters() {
            // Given
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));

            Page<FavoriteRestaurantDto> favoritesPage = new PageImpl<>(List.of());
            when(favoriteService.getFavoriteRestaurantsWithFilters(
                any(), any(Pageable.class), any(), any(), any(), any()
            )).thenReturn(favoritesPage);

            // When
            String viewName = favoriteController.favoritesPage(
                0, 12, "averageRating", "asc", "pizza", "Italian", "$$", "4+", null, null, null, null,
                authentication, model
            );

            // Then
            assertThat(viewName).isEqualTo("customer/favorites-advanced");
            verify(model).addAttribute("search", "pizza");
            verify(model).addAttribute("cuisineType", "Italian");
            verify(model).addAttribute("priceRange", "$$");
            verify(model).addAttribute("ratingFilter", "4+");
        }

        @Test
        @DisplayName("Should handle exception and show error")
        void favoritesPage_ServiceException_ShowsError() {
            // Given
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));
            when(favoriteService.getFavoriteRestaurantsWithFilters(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

            // When
            String viewName = favoriteController.favoritesPage(
                0, 12, "createdAt", "desc", null, null, null, null, null, null, null, null,
                authentication, model
            );

            // Then
            assertThat(viewName).isEqualTo("customer/favorites-advanced");
            verify(model).addAttribute(eq("error"), argThat(msg -> msg.toString().contains("Có lỗi xảy ra")));
        }
    }

    @Nested
    @DisplayName("Toggle Favorite Tests")
    class ToggleFavoriteTests {

        @Test
        @DisplayName("Should toggle favorite successfully")
        void toggleFavorite_ValidRequest_ReturnsSuccess() {
            // Given
            ToggleFavoriteRequest request = new ToggleFavoriteRequest(123);
            ToggleFavoriteResponse expectedResponse = ToggleFavoriteResponse.success(true, 5);

            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));
            when(favoriteService.toggleFavorite(customerId, request)).thenReturn(expectedResponse);

            // When
            ResponseEntity<ToggleFavoriteResponse> response = favoriteController.toggleFavorite(request, authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            verify(favoriteService).toggleFavorite(customerId, request);
        }

        @Test
        @DisplayName("Should return error when customer not found")
        void toggleFavorite_CustomerNotFound_ReturnsError() {
            // Given
            ToggleFavoriteRequest request = new ToggleFavoriteRequest(123);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

            // When
            ResponseEntity<ToggleFavoriteResponse> response = favoriteController.toggleFavorite(request, authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("Khách hàng không tồn tại");
        }

        @Test
        @DisplayName("Should handle exception and return error")
        void toggleFavorite_ServiceException_ReturnsError() {
            // Given
            ToggleFavoriteRequest request = new ToggleFavoriteRequest(123);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));
            when(favoriteService.toggleFavorite(customerId, request))
                .thenThrow(new RuntimeException("Service error"));

            // When
            ResponseEntity<ToggleFavoriteResponse> response = favoriteController.toggleFavorite(request, authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Check Favorite Tests")
    class CheckFavoriteTests {

        @Test
        @DisplayName("Should check favorite status successfully")
        void checkFavorite_ValidRequest_ReturnsTrue() {
            // Given
            Integer restaurantId = 123;
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));
            when(favoriteService.isFavorited(customerId, restaurantId)).thenReturn(true);

            // When
            ResponseEntity<Boolean> response = favoriteController.checkFavorite(restaurantId, authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
        }

        @Test
        @DisplayName("Should return false when customer not found")
        void checkFavorite_CustomerNotFound_ReturnsFalse() {
            // Given
            Integer restaurantId = 123;
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

            // When
            ResponseEntity<Boolean> response = favoriteController.checkFavorite(restaurantId, authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
        }

        @Test
        @DisplayName("Should handle exception and return 500")
        void checkFavorite_ServiceException_Returns500() {
            // Given
            Integer restaurantId = 123;
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));
            when(favoriteService.isFavorited(customerId, restaurantId))
                .thenThrow(new RuntimeException("Service error"));

            // When
            ResponseEntity<Boolean> response = favoriteController.checkFavorite(restaurantId, authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isFalse();
        }
    }

    @Nested
    @DisplayName("Get Favorited IDs Tests")
    class GetFavoritedIdsTests {

        @Test
        @DisplayName("Should get favorited restaurant IDs successfully")
        void getFavoritedRestaurantIds_ValidRequest_ReturnsIds() {
            // Given
            List<Integer> expectedIds = List.of(1, 2, 3, 4, 5);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));
            when(favoriteService.getFavoritedRestaurantIds(customerId)).thenReturn(expectedIds);

            // When
            ResponseEntity<List<Integer>> response = favoriteController.getFavoritedRestaurantIds(authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should return empty list when customer not found")
        void getFavoritedRestaurantIds_CustomerNotFound_ReturnsEmptyList() {
            // Given
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

            // When
            ResponseEntity<List<Integer>> response = favoriteController.getFavoritedRestaurantIds(authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should handle exception and return 500")
        void getFavoritedRestaurantIds_ServiceException_Returns500() {
            // Given
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(customerRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCustomer));
            when(favoriteService.getFavoritedRestaurantIds(customerId))
                .thenThrow(new RuntimeException("Service error"));

            // When
            ResponseEntity<List<Integer>> response = favoriteController.getFavoritedRestaurantIds(authentication);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isEmpty();
        }
    }
}

