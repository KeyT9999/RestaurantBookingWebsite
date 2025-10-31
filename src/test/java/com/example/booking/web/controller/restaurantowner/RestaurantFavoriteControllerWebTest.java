package com.example.booking.web.controller.restaurantowner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.config.TestRateLimitingConfig;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

@WebMvcTest(RestaurantFavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRateLimitingConfig.class)
class RestaurantFavoriteControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService userService;

    private RestaurantOwner stubOwner(UUID ownerId) {
        User user = new User();
        user.setId(ownerId);
        user.setUsername("owner@example.com");
        user.setEmail("owner@example.com");
        user.setPassword("passw0rd!");
        user.setFullName("Owner Name");

        RestaurantOwner owner = new RestaurantOwner();
        owner.setOwnerId(ownerId);
        owner.setUser(user);
        owner.setOwnerName("Owner Name");
        return owner;
    }

    @Test
    @DisplayName("favorite statistics view should populate model for restaurant owner")
    void shouldRenderFavoriteStatisticsView() throws Exception {
        UUID ownerId = UUID.randomUUID();
        RestaurantOwner owner = stubOwner(ownerId);

        when(userService.loadUserByUsername("owner@example.com")).thenReturn(owner.getUser());
        when(restaurantOwnerService.getRestaurantOwnerByUserId(owner.getUser().getId()))
                .thenReturn(Optional.of(owner));

        FavoriteStatisticsDto dto = new FavoriteStatisticsDto(11, "Pho Delight", 25L,
                4.6, 12L, BigDecimal.TEN, "Vietnamese");
        when(favoriteService.getFavoriteStatisticsForOwner(eq(ownerId), any()))
                .thenReturn(List.of(dto));

        TestingAuthenticationToken auth = new TestingAuthenticationToken("owner@example.com", "pwd");

        mockMvc.perform(get("/restaurant-owner/favorites")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/favorite-statistics"))
                .andExpect(model().attribute("statistics", hasSize(1)))
                .andExpect(model().attribute("totalFavorites", 25L))
                .andExpect(model().attribute("ownerName", containsString("Owner")));
    }

    @Test
    @DisplayName("favorite statistics view should surface error when owner missing")
    void shouldReturnErrorWhenOwnerMissing() throws Exception {
        when(userService.loadUserByUsername("missing@example.com")).thenReturn(null);

        TestingAuthenticationToken auth = new TestingAuthenticationToken("missing@example.com", "pwd");

        mockMvc.perform(get("/restaurant-owner/favorites")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/favorite-statistics"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("test endpoint should return serialized statistics payload")
    void shouldReturnStatisticsPayload() throws Exception {
        UUID ownerId = UUID.randomUUID();
        RestaurantOwner owner = stubOwner(ownerId);

        when(userService.loadUserByUsername("owner@example.com")).thenReturn(owner.getUser());
        when(restaurantOwnerService.getRestaurantOwnerByUserId(owner.getUser().getId()))
                .thenReturn(Optional.of(owner));

        FavoriteStatisticsDto dto = new FavoriteStatisticsDto(99, "Sample", 3L,
                4.0, 2L, BigDecimal.ONE, "Fusion");
        when(favoriteService.getFavoriteStatisticsForOwner(eq(ownerId), any()))
                .thenReturn(List.of(dto));

        TestingAuthenticationToken auth = new TestingAuthenticationToken("owner@example.com", "pwd");

        mockMvc.perform(get("/restaurant-owner/favorites/test")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").exists())
                .andExpect(jsonPath("$.statsCount", is(1)))
                .andExpect(jsonPath("$.statistics[0].restaurantName", is("Sample")));
    }
}
