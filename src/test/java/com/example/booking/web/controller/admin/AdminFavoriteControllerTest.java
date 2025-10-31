package com.example.booking.web.controller.admin;

import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminFavoriteController.class)
@DisplayName("AdminFavoriteController Test Suite")
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

    private FavoriteStatisticsDto testStatistic;
    private List<FavoriteStatisticsDto> statisticsList;

    @BeforeEach
    void setUp() {
        testStatistic = new FavoriteStatisticsDto();
        testStatistic.setRestaurantId(1);
        testStatistic.setRestaurantName("Test Restaurant");
        testStatistic.setFavoriteCount(10L);
        testStatistic.setAverageRating(4.5);
        testStatistic.setReviewCount(25L);

        statisticsList = Arrays.asList(testStatistic);
    }

    @Nested
    @DisplayName("favoriteStatistics() Tests")
    class FavoriteStatisticsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display favorite statistics successfully")
        void shouldDisplayStatistics() throws Exception {
            when(favoriteService.getFavoriteStatistics(any(Pageable.class))).thenReturn(statisticsList);

            mockMvc.perform(get("/admin/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/favorite-statistics-simple"))
                    .andExpect(model().attributeExists("statistics"))
                    .andExpect(model().attributeExists("currentPage"))
                    .andExpect(model().attributeExists("totalFavorites"))
                    .andExpect(model().attributeExists("averageRating"))
                    .andExpect(model().attributeExists("totalReviews"));

            verify(favoriteService).getFavoriteStatistics(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle pagination")
        void shouldHandlePagination() throws Exception {
            when(favoriteService.getFavoriteStatistics(any(Pageable.class))).thenReturn(statisticsList);

            mockMvc.perform(get("/admin/favorites")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 1));

            verify(favoriteService).getFavoriteStatistics(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should calculate statistics correctly")
        void shouldCalculateStatisticsCorrectly() throws Exception {
            FavoriteStatisticsDto stat1 = new FavoriteStatisticsDto();
            stat1.setFavoriteCount(5L);
            stat1.setAverageRating(4.0);
            stat1.setReviewCount(10L);

            FavoriteStatisticsDto stat2 = new FavoriteStatisticsDto();
            stat2.setFavoriteCount(15L);
            stat2.setAverageRating(5.0);
            stat2.setReviewCount(20L);

            List<FavoriteStatisticsDto> multipleStats = Arrays.asList(stat1, stat2);
            when(favoriteService.getFavoriteStatistics(any(Pageable.class))).thenReturn(multipleStats);

            mockMvc.perform(get("/admin/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("totalFavorites", 20L))
                    .andExpect(model().attribute("totalReviews", 30L));

            verify(favoriteService).getFavoriteStatistics(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            when(favoriteService.getFavoriteStatistics(any(Pageable.class)))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/admin/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/favorite-statistics-simple"))
                    .andExpect(model().attributeExists("error"));

            verify(favoriteService).getFavoriteStatistics(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("testQuery() Tests")
    class TestQueryTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return test query results")
        void shouldReturnTestQueryResults() throws Exception {
            Object[] rawResult1 = new Object[]{1, "Restaurant", 10L};
            Object[] rawResult2 = new Object[]{2, "Restaurant2", 5L};
            List<Object[]> rawResults = new ArrayList<>();
            rawResults.add(rawResult1);
            rawResults.add(rawResult2);
            when(favoriteRepository.getFavoriteStatistics(any(Pageable.class))).thenReturn(rawResults);
            when(favoriteService.getFavoriteStatistics(any(Pageable.class))).thenReturn(statisticsList);

            mockMvc.perform(get("/admin/favorites/test-query"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rawCount").exists())
                    .andExpect(jsonPath("$.serviceCount").exists())
                    .andExpect(jsonPath("$.rawResults").exists())
                    .andExpect(jsonPath("$.serviceResults").exists());

            verify(favoriteRepository).getFavoriteStatistics(any(Pageable.class));
            verify(favoriteService).getFavoriteStatistics(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle query exception")
        void shouldHandleQueryException() throws Exception {
            when(favoriteRepository.getFavoriteStatistics(any(Pageable.class)))
                    .thenThrow(new RuntimeException("Query error"));

            mockMvc.perform(get("/admin/favorites/test-query"))
                    .andExpect(status().is5xxServerError())
                    .andExpect(jsonPath("$.error").exists());

            verify(favoriteRepository).getFavoriteStatistics(any(Pageable.class));
        }
    }
}

