package com.example.booking.web.controller.restaurantowner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for RestaurantFavoriteController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantFavoriteController Tests")
public class RestaurantFavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private RestaurantFavoriteController controller;

    private User ownerUser;
    private RestaurantOwner restaurantOwner;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setUsername("owner");
        ownerUser.setFullName("Test Owner");

        restaurantOwner = new RestaurantOwner();
        restaurantOwner.setOwnerId(UUID.randomUUID());
        restaurantOwner.setUser(ownerUser);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
    }

    // ========== favoriteStatistics() Tests ==========

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_successfully")
    void shouldDisplayFavoriteStatistics_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        FavoriteStatisticsDto stat1 = new FavoriteStatisticsDto();
        stat1.setRestaurantName("Restaurant 1");
        stat1.setFavoriteCount(10L);
        stat1.setAverageRating(4.5);
        stat1.setReviewCount(20L);
        statistics.add(stat1);
        
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.of(restaurantOwner));
        when(favoriteService.getFavoriteStatisticsForOwner(restaurantOwner.getOwnerId(), pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute("statistics", statistics);
        verify(model).addAttribute("totalFavorites", 10L);
        verify(model).addAttribute("averageRating", "4.5");
        verify(model).addAttribute("totalReviews", 20L);
        verify(model).addAttribute("ownerName", ownerUser.getFullName());
    }

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_withNullOwner_returnsError")
    void shouldDisplayFavoriteStatistics_withNullOwner_returnsError() {
        // Given
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.empty());

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute("error", "Không tìm thấy thông tin chủ nhà hàng");
    }

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_withNullAuthentication_returnsError")
    void shouldDisplayFavoriteStatistics_withNullAuthentication_returnsError() {
        // When
        String view = controller.favoriteStatistics(0, 20, null, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute("error", "Không tìm thấy thông tin chủ nhà hàng");
    }

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_withNullFavoriteCount")
    void shouldDisplayFavoriteStatistics_withNullFavoriteCount() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        FavoriteStatisticsDto stat1 = new FavoriteStatisticsDto();
        stat1.setRestaurantName("Restaurant 1");
        stat1.setFavoriteCount(null); // null favorite count
        stat1.setAverageRating(4.5);
        stat1.setReviewCount(20L);
        statistics.add(stat1);
        
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.of(restaurantOwner));
        when(favoriteService.getFavoriteStatisticsForOwner(restaurantOwner.getOwnerId(), pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute("totalFavorites", 0L);
    }

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_withNullAverageRating")
    void shouldDisplayFavoriteStatistics_withNullAverageRating() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        FavoriteStatisticsDto stat1 = new FavoriteStatisticsDto();
        stat1.setRestaurantName("Restaurant 1");
        stat1.setFavoriteCount(10L);
        stat1.setAverageRating(null); // null average rating
        stat1.setReviewCount(20L);
        statistics.add(stat1);
        
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.of(restaurantOwner));
        when(favoriteService.getFavoriteStatisticsForOwner(restaurantOwner.getOwnerId(), pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute("averageRating", "0.0");
    }

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_withNullReviewCount")
    void shouldDisplayFavoriteStatistics_withNullReviewCount() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        FavoriteStatisticsDto stat1 = new FavoriteStatisticsDto();
        stat1.setRestaurantName("Restaurant 1");
        stat1.setFavoriteCount(10L);
        stat1.setAverageRating(4.5);
        stat1.setReviewCount(null); // null review count
        statistics.add(stat1);
        
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.of(restaurantOwner));
        when(favoriteService.getFavoriteStatisticsForOwner(restaurantOwner.getOwnerId(), pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute("totalReviews", 0L);
    }

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_withNullUser")
    void shouldDisplayFavoriteStatistics_withNullUser() {
        // Given
        RestaurantOwner ownerWithoutUser = new RestaurantOwner();
        ownerWithoutUser.setOwnerId(UUID.randomUUID());
        ownerWithoutUser.setUser(null); // null user
        
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.of(ownerWithoutUser));
        when(favoriteService.getFavoriteStatisticsForOwner(ownerWithoutUser.getOwnerId(), pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute("ownerName", "Chủ nhà hàng");
    }

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_withException")
    void shouldDisplayFavoriteStatistics_withException() {
        // Given
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_withEmptyStatistics")
    void shouldDisplayFavoriteStatistics_withEmptyStatistics() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>(); // empty list
        
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.of(restaurantOwner));
        when(favoriteService.getFavoriteStatisticsForOwner(restaurantOwner.getOwnerId(), pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertEquals("restaurant-owner/favorite-statistics", view);
        verify(model).addAttribute("totalFavorites", 0L);
        verify(model).addAttribute("averageRating", "0.0");
        verify(model).addAttribute("totalReviews", 0L);
    }

    // ========== testRestaurantOwnerStats() Tests ==========

    @Test
    @DisplayName("shouldTestRestaurantOwnerStats_successfully")
    void shouldTestRestaurantOwnerStats_successfully() {
        // Given
        List<FavoriteStatisticsDto> stats = new ArrayList<>();
        FavoriteStatisticsDto stat1 = new FavoriteStatisticsDto();
        stat1.setRestaurantName("Restaurant 1");
        stat1.setFavoriteCount(10L);
        stats.add(stat1);
        
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.of(restaurantOwner));
        when(favoriteService.getFavoriteStatisticsForOwner(restaurantOwner.getOwnerId(), PageRequest.of(0, 10))).thenReturn(stats);

        // When
        ResponseEntity<?> response = controller.testRestaurantOwnerStats(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldTestRestaurantOwnerStats_withNullOwner")
    void shouldTestRestaurantOwnerStats_withNullOwner() {
        // Given
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = controller.testRestaurantOwnerStats(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldTestRestaurantOwnerStats_withNullUser")
    void shouldTestRestaurantOwnerStats_withNullUser() {
        // Given
        RestaurantOwner ownerWithoutUser = new RestaurantOwner();
        ownerWithoutUser.setOwnerId(UUID.randomUUID());
        ownerWithoutUser.setUser(null);
        
        List<FavoriteStatisticsDto> stats = new ArrayList<>();
        
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerUser.getId())).thenReturn(Optional.of(ownerWithoutUser));
        when(favoriteService.getFavoriteStatisticsForOwner(ownerWithoutUser.getOwnerId(), PageRequest.of(0, 10))).thenReturn(stats);

        // When
        ResponseEntity<?> response = controller.testRestaurantOwnerStats(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldTestRestaurantOwnerStats_withException")
    void shouldTestRestaurantOwnerStats_withException() {
        // Given
        when(authentication.getName()).thenReturn("owner");
        when(userService.loadUserByUsername("owner")).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = controller.testRestaurantOwnerStats(authentication);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldTestRestaurantOwnerStats_withNullAuthentication")
    void shouldTestRestaurantOwnerStats_withNullAuthentication() {
        // When
        ResponseEntity<?> response = controller.testRestaurantOwnerStats(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

