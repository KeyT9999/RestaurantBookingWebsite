package com.example.booking.web.controller.admin;

import com.example.booking.domain.Customer;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.EndpointRateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminFavoriteController.class)
@DisplayName("AdminFavoriteController Tests")
class AdminFavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private CustomerFavoriteRepository favoriteRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private RestaurantProfileRepository restaurantRepository;

    @MockBean
    private EndpointRateLimitingService endpointRateLimitingService;

    private List<FavoriteStatisticsDto> mockStatistics;
    private List<Object[]> mockRawResults;

    @BeforeEach
    void setUp() {
        // Setup mock statistics
        mockStatistics = new ArrayList<>();
        FavoriteStatisticsDto dto1 = new FavoriteStatisticsDto(
                1, "Restaurant A", 10L, 4.5, 5L, null, null
        );
        mockStatistics.add(dto1);

        FavoriteStatisticsDto dto2 = new FavoriteStatisticsDto(
                2, "Restaurant B", 5L, 4.0, 3L, null, null
        );
        mockStatistics.add(dto2);

        // Setup mock raw results
        mockRawResults = new ArrayList<>();
        Object[] raw1 = {1, "Restaurant A", 10L, 4.5, 5L};
        mockRawResults.add(raw1);
        Object[] raw2 = {2, "Restaurant B", 5L, 4.0, 3L};
        mockRawResults.add(raw2);
    }

    // Test TC AFC-001: View favorite statistics
    @Test
    @DisplayName("TC AFC-001: Should display favorite statistics with summary")
    void shouldDisplayFavoriteStatistics() throws Exception {
        when(favoriteService.getFavoriteStatistics(any())).thenReturn(mockStatistics);

        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/favorite-statistics-simple"))
                .andExpect(model().attributeExists("statistics"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("pageSize"))
                .andExpect(model().attributeExists("totalFavorites"))
                .andExpect(model().attributeExists("averageRating"))
                .andExpect(model().attributeExists("totalReviews"));

        verify(favoriteService, times(1)).getFavoriteStatistics(any());
    }

    // Test TC AFC-002: View statistics with pagination
    @Test
    @DisplayName("TC AFC-002: Should handle pagination parameters")
    void shouldHandlePaginationParameters() throws Exception {
        when(favoriteService.getFavoriteStatistics(any())).thenReturn(mockStatistics);

        mockMvc.perform(get("/admin/favorites")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 20));

        verify(favoriteService, times(1)).getFavoriteStatistics(any());
    }

    // Test TC AFC-003: Calculate statistics correctly
    @Test
    @DisplayName("TC AFC-003: Should calculate total favorites correctly")
    void shouldCalculateTotalFavoritesCorrectly() throws Exception {
        when(favoriteService.getFavoriteStatistics(any())).thenReturn(mockStatistics);

        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalFavorites", 15L))
                .andExpect(model().attribute("averageRating", "4.2"))
                .andExpect(model().attribute("totalReviews", 8L));
    }

    // Test TC AFC-004: Test query endpoint
    @Test
    @DisplayName("TC AFC-004: Should return JSON data for test query")
    void shouldReturnJsonDataForTestQuery() throws Exception {
        when(favoriteRepository.getFavoriteStatistics(any())).thenReturn(mockRawResults);
        when(favoriteService.getFavoriteStatistics(any())).thenReturn(mockStatistics);

        mockMvc.perform(get("/admin/favorites/test-query"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rawCount").value(2))
                .andExpect(jsonPath("$.serviceCount").value(2));

        verify(favoriteRepository, times(1)).getFavoriteStatistics(any());
        verify(favoriteService, times(1)).getFavoriteStatistics(any());
    }

    // Test TC AFC-005: Handle exception in statistics
    @Test
    @DisplayName("TC AFC-005: Should handle exception gracefully")
    void shouldHandleExceptionGracefully() throws Exception {
        when(favoriteService.getFavoriteStatistics(any())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/favorite-statistics-simple"))
                .andExpect(model().attributeExists("error"));

        verify(favoriteService, times(1)).getFavoriteStatistics(any());
    }

    // Test TC AFC-006: Empty statistics
    @Test
    @DisplayName("TC AFC-006: Should handle empty statistics gracefully")
    void shouldHandleEmptyStatistics() throws Exception {
        when(favoriteService.getFavoriteStatistics(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalFavorites", 0L))
                .andExpect(model().attribute("averageRating", "0.0"))
                .andExpect(model().attribute("totalReviews", 0L));
    }

    // Test TC AFC-007: Test query with exception
    @Test
    @DisplayName("TC AFC-007: Should handle exception in test query")
    void shouldHandleExceptionInTestQuery() throws Exception {
        when(favoriteRepository.getFavoriteStatistics(any())).thenThrow(new RuntimeException("Query error"));

        mockMvc.perform(get("/admin/favorites/test-query"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").exists());

        verify(favoriteRepository, times(1)).getFavoriteStatistics(any());
    }
}

