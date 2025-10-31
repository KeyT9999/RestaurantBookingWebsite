package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.FavoriteService;

/**
 * Unit tests for AdminFavoriteController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminFavoriteController Tests")
public class AdminFavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private CustomerFavoriteRepository favoriteRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @Mock
    private Model model;

    @InjectMocks
    private AdminFavoriteController controller;

    // ========== favoriteStatistics() Tests ==========

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_successfully")
    void shouldDisplayFavoriteStatistics_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        FavoriteStatisticsDto stat = new FavoriteStatisticsDto();
        stat.setRestaurantName("Test Restaurant");
        stat.setFavoriteCount(10L);
        statistics.add(stat);
        
        when(favoriteService.getFavoriteStatistics(pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, model);

        // Then
        assertEquals("admin/favorites", view);
        verify(model, times(1)).addAttribute(eq("statistics"), any());
        verify(model, times(1)).addAttribute("currentPage", 0);
        verify(model, times(1)).addAttribute("pageSize", 20);
    }

    @Test
    @DisplayName("shouldCalculateSummaryStatistics_successfully")
    void shouldCalculateSummaryStatistics_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        FavoriteStatisticsDto stat = new FavoriteStatisticsDto();
        stat.setRestaurantName("Restaurant 1");
        stat.setFavoriteCount(5L);
        stat.setAverageRating(4.5);
        stat.setReviewCount(10L);
        statistics.add(stat);

        when(favoriteService.getFavoriteStatistics(pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, model);

        // Then
        assertEquals("admin/favorites", view);
        verify(model, times(1)).addAttribute("totalFavorites", anyLong());
        verify(model, times(1)).addAttribute("averageRating", anyString());
        verify(model, times(1)).addAttribute("totalReviews", anyLong());
    }

    // ========== getFavoriteStatisticsApi() Tests ==========

    @Test
    @DisplayName("shouldGetFavoriteStatisticsApi_successfully")
    void shouldGetFavoriteStatisticsApi_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        
        when(favoriteService.getFavoriteStatistics(pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, model);

        // Then
        assertEquals("admin/favorite-statistics-simple", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }
}

