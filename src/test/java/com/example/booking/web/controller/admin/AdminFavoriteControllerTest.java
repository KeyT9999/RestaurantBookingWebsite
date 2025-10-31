package com.example.booking.web.controller.admin;

import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminFavoriteController.class)
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

    private FavoriteStatisticsDto stat1;
    private FavoriteStatisticsDto stat2;

    @BeforeEach
    void setUp() {
        stat1 = new FavoriteStatisticsDto();
        stat1.setRestaurantId(1);
        stat1.setRestaurantName("Restaurant 1");
        stat1.setFavoriteCount(10L);
        stat1.setAverageRating(4.5);
        stat1.setReviewCount(20L);

        stat2 = new FavoriteStatisticsDto();
        stat2.setRestaurantId(2);
        stat2.setRestaurantName("Restaurant 2");
        stat2.setFavoriteCount(5L);
        stat2.setAverageRating(4.0);
        stat2.setReviewCount(15L);
    }

    @Test
    void shouldDisplayFavoriteStatistics() throws Exception {
        List<FavoriteStatisticsDto> statistics = Arrays.asList(stat1, stat2);
        when(favoriteService.getFavoriteStatistics(any(PageRequest.class))).thenReturn(statistics);

        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/favorite-statistics-simple"))
                .andExpect(model().attributeExists("statistics"))
                .andExpect(model().attributeExists("totalFavorites"))
                .andExpect(model().attributeExists("averageRating"))
                .andExpect(model().attributeExists("totalReviews"));
    }

    @Test
    void shouldDisplayFavoriteStatistics_WithPagination() throws Exception {
        List<FavoriteStatisticsDto> statistics = Arrays.asList(stat1, stat2);
        when(favoriteService.getFavoriteStatistics(any(PageRequest.class))).thenReturn(statistics);

        mockMvc.perform(get("/admin/favorites")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/favorite-statistics-simple"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 10));
    }

    @Test
    void shouldCalculateSummaryStatistics() throws Exception {
        List<FavoriteStatisticsDto> statistics = Arrays.asList(stat1, stat2);
        when(favoriteService.getFavoriteStatistics(any(PageRequest.class))).thenReturn(statistics);

        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalFavorites", 15L))
                .andExpect(model().attribute("totalReviews", 35L));
    }

    @Test
    void shouldHandleException() throws Exception {
        when(favoriteService.getFavoriteStatistics(any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/favorite-statistics-simple"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void shouldTestQuery() throws Exception {
        List<Object[]> rawResults = Arrays.asList(
                new Object[]{1, "Restaurant 1", 10L},
                new Object[]{2, "Restaurant 2", 5L}
        );
        List<FavoriteStatisticsDto> serviceResults = Arrays.asList(stat1, stat2);

        when(favoriteRepository.getFavoriteStatistics(any(PageRequest.class))).thenReturn(rawResults);
        when(favoriteService.getFavoriteStatistics(any(PageRequest.class))).thenReturn(serviceResults);

        mockMvc.perform(get("/admin/favorites/test-query"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldHandleTestQueryException() throws Exception {
        when(favoriteRepository.getFavoriteStatistics(any(PageRequest.class)))
                .thenThrow(new RuntimeException("Query error"));

        mockMvc.perform(get("/admin/favorites/test-query"))
                .andExpect(status().isInternalServerError());
    }
}
