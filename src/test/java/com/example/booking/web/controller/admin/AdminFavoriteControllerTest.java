package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        assertEquals("admin/favorite-statistics-simple", view);
        verify(model, times(1)).addAttribute(eq("statistics"), any());
        verify(model, times(1)).addAttribute("currentPage", 0);
        verify(model, times(1)).addAttribute("pageSize", 20);
    }

    @Test
    @DisplayName("favoriteStatistics - should handle empty statistics")
    void favoriteStatistics_WithEmptyStats_ShouldDisplay() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        when(favoriteService.getFavoriteStatistics(pageable)).thenReturn(Collections.emptyList());

        // When
        String view = controller.favoriteStatistics(0, 20, model);

        // Then
        assertEquals("admin/favorite-statistics-simple", view);
        verify(model, times(1)).addAttribute("totalFavorites", 0L);
    }

    @Test
    @DisplayName("favoriteStatistics - should handle null values")
    void favoriteStatistics_WithNullValues_ShouldHandle() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        FavoriteStatisticsDto stat = new FavoriteStatisticsDto();
        stat.setRestaurantName("Test Restaurant");
        stat.setFavoriteCount(null);
        stat.setAverageRating(null);
        stat.setReviewCount(null);
        statistics.add(stat);
        
        when(favoriteService.getFavoriteStatistics(pageable)).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, model);

        // Then
        assertEquals("admin/favorite-statistics-simple", view);
        verify(model, times(1)).addAttribute("totalFavorites", 0L);
    }

    @Test
    @DisplayName("favoriteStatistics - should handle exception")
    void favoriteStatistics_WithException_ShouldHandle() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        when(favoriteService.getFavoriteStatistics(pageable)).thenThrow(new RuntimeException("DB error"));

        // When
        String view = controller.favoriteStatistics(0, 20, model);

        // Then
        assertEquals("admin/favorite-statistics-simple", view);
        verify(model, times(1)).addAttribute(eq("error"), anyString());
    }

    // ========== testQuery() Tests ==========

    @Test
    @DisplayName("testQuery - should return test results successfully")
    void testQuery_ShouldReturnResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> rawResults = new ArrayList<>();
        rawResults.add(new Object[]{"Restaurant 1", 10L, 4.5, 5L});
        
        List<FavoriteStatisticsDto> serviceResults = new ArrayList<>();
        FavoriteStatisticsDto dto = new FavoriteStatisticsDto();
        dto.setRestaurantName("Restaurant 1");
        dto.setFavoriteCount(10L);
        serviceResults.add(dto);
        
        when(favoriteRepository.getFavoriteStatistics(pageable)).thenReturn(rawResults);
        when(favoriteService.getFavoriteStatistics(pageable)).thenReturn(serviceResults);

        // When
        ResponseEntity<?> response = controller.testQuery();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("testQuery - should handle exception")
    void testQuery_WithException_ShouldReturnError() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(favoriteRepository.getFavoriteStatistics(pageable)).thenThrow(new RuntimeException("DB error"));

        // When
        ResponseEntity<?> response = controller.testQuery();

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("testQuery - should handle empty results")
    void testQuery_WithEmptyResults_ShouldReturn() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(favoriteRepository.getFavoriteStatistics(pageable)).thenReturn(Collections.emptyList());
        when(favoriteService.getFavoriteStatistics(pageable)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<?> response = controller.testQuery();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
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
        assertEquals("admin/favorite-statistics-simple", view);
        verify(model, times(1)).addAttribute(eq("totalFavorites"), anyLong());
        verify(model, times(1)).addAttribute(eq("averageRating"), anyString());
        verify(model, times(1)).addAttribute(eq("totalReviews"), anyLong());
    }
}

