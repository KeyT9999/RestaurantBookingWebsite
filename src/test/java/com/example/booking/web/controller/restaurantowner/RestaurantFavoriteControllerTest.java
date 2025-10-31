package com.example.booking.web.controller.restaurantowner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantFavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RestaurantFavoriteController Test Suite")
class RestaurantFavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService userService;

    private RestaurantOwner testOwner;
    private User testUser;
    private FavoriteStatisticsDto testStatistic;
    private List<FavoriteStatisticsDto> statisticsList;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User("owner", "owner@test.com", "password", "Owner Name");
        testUser.setId(UUID.randomUUID());

        // Setup test restaurant owner
        testOwner = new RestaurantOwner();
        testOwner.setOwnerId(UUID.randomUUID());
        testOwner.setUser(testUser);

        // Setup test statistics
        testStatistic = new FavoriteStatisticsDto();
        testStatistic.setRestaurantId(1);
        testStatistic.setRestaurantName("Test Restaurant");
        testStatistic.setFavoriteCount(10L);
        testStatistic.setReviewCount(5L);
        testStatistic.setAverageRating(4.5);

        statisticsList = Arrays.asList(testStatistic);
    }

    @Nested
    @DisplayName("favoriteStatistics() Tests")
    class FavoriteStatisticsTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should display favorite statistics successfully")
        void shouldDisplayFavoriteStatistics() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.of(testOwner));
            when(favoriteService.getFavoriteStatisticsForOwner(any(), any()))
                    .thenReturn(statisticsList);

            mockMvc.perform(get("/restaurant-owner/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/favorite-statistics"))
                    .andExpect(model().attributeExists("statistics"))
                    .andExpect(model().attributeExists("totalFavorites"))
                    .andExpect(model().attributeExists("averageRating"))
                    .andExpect(model().attributeExists("totalReviews"))
                    .andExpect(model().attributeExists("ownerName"));

            verify(favoriteService).getFavoriteStatisticsForOwner(any(), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle pagination parameters")
        void shouldHandlePaginationParameters() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.of(testOwner));
            when(favoriteService.getFavoriteStatisticsForOwner(any(), any()))
                    .thenReturn(statisticsList);

            mockMvc.perform(get("/restaurant-owner/favorites")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 1))
                    .andExpect(model().attribute("pageSize", 10));

            verify(favoriteService).getFavoriteStatisticsForOwner(eq(testOwner.getOwnerId()), 
                    argThat(pageable -> pageable.getPageNumber() == 1 && pageable.getPageSize() == 10));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle restaurant owner not found")
        void shouldHandleRestaurantOwnerNotFound() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/restaurant-owner/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/favorite-statistics"))
                    .andExpect(model().attributeExists("error"));

            verify(favoriteService, never()).getFavoriteStatisticsForOwner(any(), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle user not found")
        void shouldHandleUserNotFound() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(null);

            mockMvc.perform(get("/restaurant-owner/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("error"));

            verify(favoriteService, never()).getFavoriteStatisticsForOwner(any(), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle empty statistics list")
        void shouldHandleEmptyStatisticsList() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.of(testOwner));
            when(favoriteService.getFavoriteStatisticsForOwner(any(), any()))
                    .thenReturn(new ArrayList<>());

            mockMvc.perform(get("/restaurant-owner/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("totalFavorites", 0L))
                    .andExpect(model().attribute("averageRating", "0.0"))
                    .andExpect(model().attribute("totalReviews", 0L));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.of(testOwner));
            when(favoriteService.getFavoriteStatisticsForOwner(any(), any()))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/restaurant-owner/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("error"));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should calculate summary statistics correctly")
        void shouldCalculateSummaryStatisticsCorrectly() throws Exception {
            FavoriteStatisticsDto stat1 = new FavoriteStatisticsDto();
            stat1.setFavoriteCount(10L);
            stat1.setReviewCount(5L);
            stat1.setAverageRating(4.0);

            FavoriteStatisticsDto stat2 = new FavoriteStatisticsDto();
            stat2.setFavoriteCount(20L);
            stat2.setReviewCount(10L);
            stat2.setAverageRating(5.0);

            List<FavoriteStatisticsDto> multipleStats = Arrays.asList(stat1, stat2);

            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.of(testOwner));
            when(favoriteService.getFavoriteStatisticsForOwner(any(), any()))
                    .thenReturn(multipleStats);

            mockMvc.perform(get("/restaurant-owner/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("totalFavorites", 30L))
                    .andExpect(model().attribute("totalReviews", 15L));
        }
    }

    @Nested
    @DisplayName("testRestaurantOwnerStats() Tests")
    class TestRestaurantOwnerStatsTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should return test statistics as JSON")
        void shouldReturnTestStatisticsAsJson() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.of(testOwner));
            when(favoriteService.getFavoriteStatisticsForOwner(any(), any()))
                    .thenReturn(statisticsList);

            mockMvc.perform(get("/restaurant-owner/favorites/test"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.ownerId").exists())
                    .andExpect(jsonPath("$.ownerName").exists())
                    .andExpect(jsonPath("$.statsCount").value(1))
                    .andExpect(jsonPath("$.statistics").isArray());

            verify(favoriteService).getFavoriteStatisticsForOwner(any(), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle restaurant owner not found in test endpoint")
        void shouldHandleRestaurantOwnerNotFoundInTest() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/restaurant-owner/favorites/test"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            verify(favoriteService, never()).getFavoriteStatisticsForOwner(any(), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle user not found in test endpoint")
        void shouldHandleUserNotFoundInTest() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(null);

            mockMvc.perform(get("/restaurant-owner/favorites/test"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            verify(favoriteService, never()).getFavoriteStatisticsForOwner(any(), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle service exception in test endpoint")
        void shouldHandleServiceExceptionInTest() throws Exception {
            when(userService.loadUserByUsername("owner")).thenReturn(testUser);
            when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                    .thenReturn(Optional.of(testOwner));
            when(favoriteService.getFavoriteStatisticsForOwner(any(), any()))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/restaurant-owner/favorites/test"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER", username = "owner")
        @DisplayName("Should handle null authentication")
        void shouldHandleNullAuthentication() throws Exception {
            mockMvc.perform(get("/restaurant-owner/favorites/test")
                    .principal(null))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }
}

