package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.Review;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;
import com.example.booking.service.NotificationService;
import com.example.booking.repository.RestaurantMediaRepository;

/**
 * Comprehensive unit tests for HomeController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HomeController Tests")
public class HomeControllerTest {

    @Mock
    private RestaurantManagementService restaurantService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private CustomerService customerService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestaurantMediaRepository restaurantMediaRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HomeController controller;

    private RestaurantProfile restaurant;
    private User user;
    private Customer customer;
    private ReviewStatisticsDto statistics;
    private RestaurantMedia media;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setCuisineType("Vietnamese");
        restaurant.setAddress("123 Test St");
        restaurant.setAveragePrice(new BigDecimal(200000));
        
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);
        customer.setFullName("Test Customer");

        statistics = new ReviewStatisticsDto(4.5, 100, Collections.emptyMap());

        media = new RestaurantMedia();
        media.setMediaId(1);
        media.setRestaurant(restaurant);
        media.setUrl("http://example.com/cover.jpg");
        media.setType("cover");
    }

    // ========== home() Tests ==========

    @Test
    @DisplayName("shouldDisplayHomePage_successfully")
    void shouldDisplayHomePage_successfully() {
        // Given
        List<RestaurantProfile> restaurants = List.of(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(restaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("pageTitle"), anyString());
        verify(model).addAttribute(eq("activeNav"), eq("home"));
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    @Test
    @DisplayName("shouldDisplayHomePage_withSearchParameters")
    void shouldDisplayHomePage_withSearchParameters() {
        // Given
        List<RestaurantProfile> restaurants = List.of(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(restaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.home("test search", "Vietnamese", "mid", model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute("search", "test search");
        verify(model).addAttribute("cuisineType", "Vietnamese");
        verify(model).addAttribute("priceRange", "mid");
    }

    @Test
    @DisplayName("shouldDisplayHomePage_WithSearchParams")
    void shouldDisplayHomePage_WithSearchParams() {
        // Given
        List<RestaurantProfile> topRestaurants = Arrays.asList(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(topRestaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);

        // When
        String view = controller.home("pizza", "Italian", "100000-300000", model, authentication);

        // Then
        assertEquals("public/home", view);
        verify(model, times(1)).addAttribute(eq("search"), eq("pizza"));
        verify(model, times(1)).addAttribute(eq("cuisineType"), eq("Italian"));
        verify(model, times(1)).addAttribute(eq("priceRange"), eq("100000-300000"));
    }

    @Test
    @DisplayName("shouldDisplayHomePage_WithPopularRestaurants")
    void shouldDisplayHomePage_WithPopularRestaurants() {
        // Given
        List<RestaurantProfile> topRestaurants = Arrays.asList(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(topRestaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);

        // When
        String view = controller.home(null, null, null, model, authentication);

        // Then
        assertEquals("public/home", view);
        verify(model, atLeastOnce()).addAttribute(eq("popularRestaurants"), any());
    }

    @Test
    @DisplayName("shouldDisplayHomePage_withAuthenticatedAdminUser")
    void shouldDisplayHomePage_withAuthenticatedAdminUser() {
        // Given
        List<RestaurantProfile> restaurants = List.of(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(restaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(user);
        when(notificationService.countUnreadByUserId(any())).thenReturn(5L);

        // When
        String view = controller.home(null, null, null, model, authentication);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute("userRole", "ADMIN");
        verify(model).addAttribute("unreadCount", 5L);
    }

    @Test
    @DisplayName("shouldDisplayHomePage_withAuthenticatedRestaurantOwner")
    void shouldDisplayHomePage_withAuthenticatedRestaurantOwner() {
        // Given
        List<RestaurantProfile> restaurants = List.of(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(restaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(user);
        when(notificationService.countUnreadByUserId(any())).thenReturn(3L);

        // When
        String view = controller.home(null, null, null, model, authentication);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute("userRole", "RESTAURANT_OWNER");
    }

    @Test
    @DisplayName("shouldDisplayHomePage_WithAuthenticatedUser")
    void shouldDisplayHomePage_WithAuthenticatedUser() {
        // Given
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_CUSTOMER");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        Collection<? extends GrantedAuthority> authorities = java.util.Collections.singleton(authority);
        doReturn(authorities).when(authentication).getAuthorities();
        when(notificationService.countUnreadByUserId(user.getId())).thenReturn(5L);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(Collections.emptyList());

        // When
        String view = controller.home(null, null, null, model, authentication);

        // Then
        assertEquals("public/home", view);
        verify(model, atLeastOnce()).addAttribute(eq("unreadCount"), eq(5L));
    }

    @Test
    @DisplayName("shouldDisplayHomePage_withEmptyRestaurantList")
    void shouldDisplayHomePage_withEmptyRestaurantList() {
        // Given
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(Collections.emptyList());
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    @Test
    @DisplayName("shouldHandleNotificationError_gracefully")
    void shouldHandleNotificationError_gracefully() {
        // Given
        List<RestaurantProfile> restaurants = List.of(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(restaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(notificationService.countUnreadByUserId(any()))
            .thenThrow(new RuntimeException("Notification error"));

        // When
        String view = controller.home(null, null, null, model, authentication);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute("unreadCount", 0L);
    }

    @Test
    @DisplayName("shouldHandleException_whenNotificationCountFails")
    void shouldHandleException_whenNotificationCountFails() {
        // Given
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_CUSTOMER");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        Collection<? extends GrantedAuthority> authorities = java.util.Collections.singleton(authority);
        doReturn(authorities).when(authentication).getAuthorities();
        when(notificationService.countUnreadByUserId(user.getId())).thenThrow(new RuntimeException("Error"));
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(Collections.emptyList());

        // When
        String view = controller.home(null, null, null, model, authentication);

        // Then
        assertEquals("public/home", view);
        verify(model, atLeastOnce()).addAttribute(eq("unreadCount"), eq(0L));
    }

    // ========== restaurants() Tests ==========

    @Test
    @DisplayName("shouldDisplayRestaurantsPage_withDefaultParameters")
    void shouldDisplayRestaurantsPage_withDefaultParameters() {
        // Given
        List<RestaurantProfile> restaurantList = List.of(restaurant);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(restaurantList);
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", null, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model).addAttribute(eq("pageTitle"), anyString());
        verify(model).addAttribute(eq("restaurants"), eq(restaurantPage));
    }

    @Test
    @DisplayName("shouldSearchRestaurants_withSearchTerm")
    void shouldSearchRestaurants_withSearchTerm() {
        // Given
        String searchTerm = "test";
        List<RestaurantProfile> restaurantList = List.of(restaurant);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(restaurantList);
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), eq(searchTerm), isNull(), isNull(), isNull()))
                .thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", searchTerm, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model).addAttribute("search", searchTerm);
        verify(restaurantService).getRestaurantsWithFilters(any(), eq(searchTerm), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("shouldSearchRestaurants_WithFilters")
    void shouldSearchRestaurants_WithFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 12);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(Arrays.asList(restaurant));

        when(restaurantService.getRestaurantsWithFilters(eq(pageable), eq("pizza"), 
            eq("Italian"), eq("100000-300000"), isNull())).thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", "pizza", 
            "Italian", "100000-300000", null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model, atLeastOnce()).addAttribute(eq("restaurants"), any());
    }

    @Test
    @DisplayName("shouldFilterRestaurants_byCuisineType")
    void shouldFilterRestaurants_byCuisineType() {
        // Given
        String cuisineType = "Vietnamese";
        List<RestaurantProfile> restaurantList = List.of(restaurant);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(restaurantList);
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), eq(cuisineType), isNull(), isNull()))
                .thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", null, cuisineType, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model).addAttribute("cuisineType", cuisineType);
    }

    @Test
    @DisplayName("shouldFilterRestaurants_byPriceRange")
    void shouldFilterRestaurants_byPriceRange() {
        // Given
        String priceRange = "mid";
        List<RestaurantProfile> restaurantList = List.of(restaurant);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(restaurantList);
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), isNull(), eq(priceRange), isNull()))
                .thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", null, null, priceRange, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model).addAttribute("priceRange", priceRange);
    }

    @Test
    @DisplayName("shouldFilterRestaurants_byRating")
    void shouldFilterRestaurants_byRating() {
        // Given
        String ratingFilter = "4";
        List<RestaurantProfile> restaurantList = List.of(restaurant);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(restaurantList);
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), isNull(), isNull(), eq(ratingFilter)))
                .thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", null, null, null, ratingFilter, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model).addAttribute("ratingFilter", ratingFilter);
    }

    @Test
    @DisplayName("shouldSortRestaurants_byNameDescending")
    void shouldSortRestaurants_byNameDescending() {
        // Given
        List<RestaurantProfile> restaurantList = List.of(restaurant);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(restaurantList);
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "desc", null, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model).addAttribute("sortBy", "restaurantName");
        verify(model).addAttribute("sortDir", "desc");
    }

    @Test
    @DisplayName("shouldPaginateRestaurants_secondPage")
    void shouldPaginateRestaurants_secondPage() {
        // Given
        List<RestaurantProfile> restaurantList = List.of(restaurant);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(restaurantList, PageRequest.of(1, 12), 25);
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(Collections.emptyList());

        // When
        String view = controller.restaurants(1, 12, "restaurantName", "asc", null, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("totalPages", 3);
    }

    @Test
    @DisplayName("shouldSearchRestaurants_WithPagination")
    void shouldSearchRestaurants_WithPagination() {
        // Given
        Pageable pageable = PageRequest.of(2, 10);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(Arrays.asList(restaurant));

        when(restaurantService.getRestaurantsWithFilters(eq(pageable), isNull(), 
            isNull(), isNull(), isNull())).thenReturn(restaurantPage);

        // When
        String view = controller.restaurants(2, 10, "restaurantName", "asc", null, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model, atLeastOnce()).addAttribute(eq("currentPage"), eq(2));
    }

    @Test
    @DisplayName("shouldLoadCoverImages_forRestaurants")
    void shouldLoadCoverImages_forRestaurants() {
        // Given
        List<RestaurantProfile> restaurantList = List.of(restaurant);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(restaurantList);
        
        RestaurantMedia coverImage = new RestaurantMedia();
        coverImage.setUrl("https://example.com/cover.jpg");
        coverImage.setType("cover");
        coverImage.setRestaurant(restaurant);
        
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(restaurantPage);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover")))
            .thenReturn(List.of(coverImage));

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", null, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(restaurantMediaRepository).findByRestaurantsAndType(anyList(), eq("cover"));
    }

    @Test
    @DisplayName("shouldHandleException_inRestaurantsListing")
    void shouldHandleException_inRestaurantsListing() {
        // Given
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), isNull(), isNull(), isNull()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", null, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model).addAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldSearchRestaurants_WithEmptyResults")
    void shouldSearchRestaurants_WithEmptyResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 12);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(Collections.emptyList());

        when(restaurantService.getRestaurantsWithFilters(eq(pageable), isNull(), 
            isNull(), isNull(), isNull())).thenReturn(restaurantPage);

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", null, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model, atLeastOnce()).addAttribute(eq("restaurants"), eq(Collections.emptyList()));
    }

    @Test
    @DisplayName("shouldSearchRestaurants_HandleException")
    void shouldSearchRestaurants_HandleException() {
        // Given
        when(restaurantService.getRestaurantsWithFilters(any(Pageable.class), isNull(), 
            isNull(), isNull(), isNull())).thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", null, null, null, null, null, null, null, null, model);

        // Then
        assertEquals("public/restaurants", view);
        verify(model, atLeastOnce()).addAttribute(eq("error"), anyString());
    }

    // ========== restaurantDetail() Tests ==========

    @Test
    @DisplayName("shouldDisplayRestaurantDetails_successfully")
    void shouldDisplayRestaurantDetails_successfully() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("restaurant"), eq(restaurant));
    }

    @Test
    @DisplayName("shouldDisplayRestaurantDetails_WithMedia")
    void shouldDisplayRestaurantDetails_WithMedia() {
        // Given
        List<RestaurantMedia> mediaList = Arrays.asList(media);

        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(mediaList);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);
        when(reviewService.getReviewsByRestaurant(anyInt(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model, atLeastOnce()).addAttribute(eq("restaurant"), any());
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldRedirect_whenRestaurantNotFound")
    void shouldRedirect_whenRestaurantNotFound() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.empty());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertTrue(view.contains("redirect:/restaurants"));
    }

    @Test
    @DisplayName("shouldLoadRestaurantMedia_byType")
    void shouldLoadRestaurantMedia_byType() {
        // Given
        RestaurantMedia logo = new RestaurantMedia();
        logo.setType("logo");
        logo.setUrl("logo.jpg");
        
        RestaurantMedia cover = new RestaurantMedia();
        cover.setType("cover");
        cover.setUrl("cover.jpg");
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant))
            .thenReturn(List.of(logo, cover));
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("logo"), any());
        verify(model).addAttribute(eq("cover"), any());
    }

    @Test
    @DisplayName("shouldCheckIfCustomerReviewed_forAuthenticatedUser")
    void shouldCheckIfCustomerReviewed_forAuthenticatedUser() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.hasCustomerReviewedRestaurant(customer.getCustomerId(), 1))
            .thenReturn(true);
        when(reviewService.getReviewsByCustomer(customer.getCustomerId()))
            .thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute("hasReviewed", true);
    }

    @Test
    @DisplayName("shouldLoadRecentReviews_forRestaurant")
    void shouldLoadRecentReviews_forRestaurant() {
        // Given
        List<ReviewDto> reviews = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ReviewDto review = new ReviewDto();
            review.setReviewId(i + 1);
            reviews.add(review);
        }
        Page<ReviewDto> reviewPage = new PageImpl<>(reviews);
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(reviewPage);
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("recentReviews"), anyList());
        verify(model).addAttribute(eq("totalReviews"), eq(3L));
    }

    @Test
    @DisplayName("shouldHandleReviewServiceError_gracefully")
    void shouldHandleReviewServiceError_gracefully() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenThrow(new RuntimeException("Review service error"));

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        // Should continue without review data
    }

    @Test
    @DisplayName("shouldLoadDishesWithImages_forRestaurant")
    void shouldLoadDishesWithImages_forRestaurant() {
        // Given
        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setName("Test Dish");
        restaurant.setDishes(List.of(dish));
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(restaurantOwnerService.getDishImageUrl(1, 1)).thenReturn("dish-image.jpg");
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("dishes"), anyList());
        verify(restaurantOwnerService).getDishImageUrl(1, 1);
    }

    @Test
    @DisplayName("shouldLoadTablesFor_restaurant")
    void shouldLoadTablesForRestaurant() {
        // Given
        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        restaurant.setTables(List.of(table));
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("tables"), anyList());
    }

    @Test
    @DisplayName("shouldHandleException_whenReviewServiceFails")
    void shouldHandleException_whenReviewServiceFails() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(reviewService.getRestaurantReviewStatistics(1)).thenThrow(new RuntimeException("Review error"));

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== Additional Edge Case Tests for buildPopularRestaurantCards ==========

    @Test
    @DisplayName("shouldBuildPopularRestaurantCards_withIndex0_BadgeTopRated")
    void shouldBuildPopularRestaurantCards_withIndex0_BadgeTopRated() {
        // Given - Create reviews to make reviewCount >= 10
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Review review = new Review();
            review.setReviewId(i + 1);
            reviews.add(review);
        }
        restaurant.setReviews(reviews);
        List<RestaurantProfile> topRestaurants = Arrays.asList(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(topRestaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    @Test
    @DisplayName("shouldBuildPopularRestaurantCards_withIndex1_BadgeKhachYeuThich")
    void shouldBuildPopularRestaurantCards_withIndex1_BadgeKhachYeuThich() {
        // Given - Need 2 restaurants to test index 1
        RestaurantProfile restaurant2 = new RestaurantProfile();
        restaurant2.setRestaurantId(2);
        restaurant2.setRestaurantName("Restaurant 2");
        List<RestaurantProfile> topRestaurants = Arrays.asList(restaurant, restaurant2);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(topRestaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);
        when(reviewService.getRestaurantReviewStatistics(2)).thenReturn(statistics);

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    @Test
    @DisplayName("shouldBuildPopularRestaurantCards_withIndex2_BadgeDuocDatNhieu")
    void shouldBuildPopularRestaurantCards_withIndex2_BadgeDuocDatNhieu() {
        // Given - Need 3 restaurants to test index 2
        RestaurantProfile restaurant2 = new RestaurantProfile();
        restaurant2.setRestaurantId(2);
        RestaurantProfile restaurant3 = new RestaurantProfile();
        restaurant3.setRestaurantId(3);
        List<RestaurantProfile> topRestaurants = Arrays.asList(restaurant, restaurant2, restaurant3);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(topRestaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(anyInt())).thenReturn(statistics);

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    @Test
    @DisplayName("shouldBuildPopularRestaurantCards_withIndex3_ReviewCount10Plus_BadgeYeuThich")
    void shouldBuildPopularRestaurantCards_withIndex3_ReviewCount10Plus_BadgeYeuThich() {
        // Given - Need 4 restaurants, last one with reviewCount >= 10
        RestaurantProfile restaurant2 = new RestaurantProfile();
        restaurant2.setRestaurantId(2);
        RestaurantProfile restaurant3 = new RestaurantProfile();
        restaurant3.setRestaurantId(3);
        RestaurantProfile restaurant4 = new RestaurantProfile();
        restaurant4.setRestaurantId(4);
        // Create reviews to make reviewCount >= 10
        List<Review> reviews4 = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Review review = new Review();
            review.setReviewId(i + 1);
            reviews4.add(review);
        }
        restaurant4.setReviews(reviews4); // >= 10
        List<RestaurantProfile> topRestaurants = Arrays.asList(restaurant, restaurant2, restaurant3, restaurant4);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(topRestaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(anyInt())).thenReturn(statistics);

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    @Test
    @DisplayName("shouldBuildPopularRestaurantCards_withIndex3_ReviewCountLessThan10_NoBadge")
    void shouldBuildPopularRestaurantCards_withIndex3_ReviewCountLessThan10_NoBadge() {
        // Given - Need 4 restaurants, last one with reviewCount < 10
        RestaurantProfile restaurant2 = new RestaurantProfile();
        restaurant2.setRestaurantId(2);
        RestaurantProfile restaurant3 = new RestaurantProfile();
        restaurant3.setRestaurantId(3);
        RestaurantProfile restaurant4 = new RestaurantProfile();
        restaurant4.setRestaurantId(4);
        // Create reviews to make reviewCount < 10
        List<Review> reviews4 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Review review = new Review();
            review.setReviewId(i + 1);
            reviews4.add(review);
        }
        restaurant4.setReviews(reviews4); // < 10
        List<RestaurantProfile> topRestaurants = Arrays.asList(restaurant, restaurant2, restaurant3, restaurant4);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(topRestaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(anyInt())).thenReturn(statistics);

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    @Test
    @DisplayName("shouldBuildPopularRestaurantCards_withNullAveragePrice_ShowsDefaultLabel")
    void shouldBuildPopularRestaurantCards_withNullAveragePrice_ShowsDefaultLabel() {
        // Given
        restaurant.setAveragePrice(null);
        List<RestaurantProfile> topRestaurants = Arrays.asList(restaurant);
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(topRestaurants);
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    @Test
    @DisplayName("shouldBuildPopularRestaurantCards_withStatisticsException_usesRestaurantDefaults")
    void shouldBuildPopularRestaurantCards_withStatisticsException_usesRestaurantDefaults() {
        // Given
        when(restaurantService.findTopRatedRestaurants(6)).thenReturn(Arrays.asList(restaurant));
        when(restaurantMediaRepository.findByRestaurantsAndType(any(), eq("cover")))
            .thenReturn(Arrays.asList(media));
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenThrow(new RuntimeException("Stats error"));

        // When
        String view = controller.home(null, null, null, model, null);

        // Then
        assertEquals("public/home", view);
        verify(model).addAttribute(eq("popularRestaurants"), anyList());
    }

    // ========== Additional Edge Case Tests for restaurantDetail ==========

    @Test
    @DisplayName("shouldLoadRestaurantDetail_withCustomerNotPresent")
    void shouldLoadRestaurantDetail_withCustomerNotPresent() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("hasReviewed"), eq(false));
    }

    @Test
    @DisplayName("shouldLoadRestaurantDetail_withHasReviewedFalse")
    void shouldLoadRestaurantDetail_withHasReviewedFalse() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.hasCustomerReviewedRestaurant(customer.getCustomerId(), 1))
            .thenReturn(false);
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("hasReviewed"), eq(false));
    }

    @Test
    @DisplayName("shouldLoadRestaurantDetail_withHasReviewedTrue_CustomerReviewFound")
    void shouldLoadRestaurantDetail_withHasReviewedTrue_CustomerReviewFound() {
        // Given
        ReviewDto customerReviewDto = new ReviewDto();
        customerReviewDto.setReviewId(1);
        customerReviewDto.setRestaurantId(1);
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.hasCustomerReviewedRestaurant(customer.getCustomerId(), 1))
            .thenReturn(true);
        when(reviewService.getReviewsByCustomer(customer.getCustomerId()))
            .thenReturn(Arrays.asList(customerReviewDto));
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("hasReviewed"), eq(true));
        verify(model).addAttribute(eq("customerReview"), eq(customerReviewDto));
    }

    @Test
    @DisplayName("shouldLoadRestaurantDetail_withHasReviewedTrue_CustomerReviewNotFound")
    void shouldLoadRestaurantDetail_withHasReviewedTrue_CustomerReviewNotFound() {
        // Given
        ReviewDto otherReview = new ReviewDto();
        otherReview.setReviewId(1);
        otherReview.setRestaurantId(2); // Different restaurant
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.hasCustomerReviewedRestaurant(customer.getCustomerId(), 1))
            .thenReturn(true);
        when(reviewService.getReviewsByCustomer(customer.getCustomerId()))
            .thenReturn(Arrays.asList(otherReview)); // Different restaurant ID
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("hasReviewed"), eq(true));
        verify(model).addAttribute(eq("customerReview"), isNull());
    }

    @Test
    @DisplayName("shouldLoadRestaurantDetail_withAllMediaTypes")
    void shouldLoadRestaurantDetail_withAllMediaTypes() {
        // Given
        RestaurantMedia logoMedia = new RestaurantMedia();
        logoMedia.setType("logo");
        RestaurantMedia coverMedia = new RestaurantMedia();
        coverMedia.setType("cover");
        RestaurantMedia galleryMedia = new RestaurantMedia();
        galleryMedia.setType("gallery");
        RestaurantMedia exteriorMedia = new RestaurantMedia();
        exteriorMedia.setType("exterior");
        RestaurantMedia interiorMedia = new RestaurantMedia();
        interiorMedia.setType("interior");
        RestaurantMedia menuMedia = new RestaurantMedia();
        menuMedia.setType("menu");
        RestaurantMedia tableLayoutMedia = new RestaurantMedia();
        tableLayoutMedia.setType("table_layout");
        
        List<RestaurantMedia> allMedia = Arrays.asList(
            logoMedia, coverMedia, galleryMedia, exteriorMedia, 
            interiorMedia, menuMedia, tableLayoutMedia
        );
        
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(allMedia);
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("logo"), any());
        verify(model).addAttribute(eq("cover"), any());
        verify(model).addAttribute(eq("gallery"), anyList());
        verify(model).addAttribute(eq("exterior"), anyList());
        verify(model).addAttribute(eq("interior"), anyList());
        verify(model).addAttribute(eq("menus"), anyList());
        verify(model).addAttribute(eq("tableLayouts"), anyList());
    }

    @Test
    @DisplayName("shouldLoadRestaurantDetail_withNullDishes")
    void shouldLoadRestaurantDetail_withNullDishes() {
        // Given
        restaurant.setDishes(null);
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("dishes"), anyList());
    }

    @Test
    @DisplayName("shouldLoadRestaurantDetail_withNullTables")
    void shouldLoadRestaurantDetail_withNullTables() {
        // Given
        restaurant.setTables(null);
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("tables"), anyList());
    }

    @Test
    @DisplayName("shouldHandleException_inRestaurantDetail_CatchBlock")
    void shouldHandleException_inRestaurantDetail_CatchBlock() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertTrue(view.contains("redirect:/restaurants"));
    }

    @Test
    @DisplayName("shouldLoadRestaurantDetail_withEmptyLogosAndCovers")
    void shouldLoadRestaurantDetail_withEmptyLogosAndCovers() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getMediaByRestaurant(restaurant)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(reviewService.getRestaurantReviewStatistics(1))
            .thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.restaurantDetail(1, model, null);

        // Then
        assertEquals("public/restaurant-detail-simple", view);
        verify(model).addAttribute(eq("logo"), isNull());
        verify(model).addAttribute(eq("cover"), isNull());
    }
}
