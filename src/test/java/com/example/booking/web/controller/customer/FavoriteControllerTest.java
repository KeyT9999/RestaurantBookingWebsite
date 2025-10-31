package com.example.booking.web.controller.customer;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.customer.FavoriteRestaurantDto;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private CustomerRepository customerRepository;

    private Customer customer;
    private User user;
    private FavoriteRestaurantDto favoriteDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);

        favoriteDto = new FavoriteRestaurantDto();
        favoriteDto.setRestaurantId(1);
        favoriteDto.setRestaurantName("Test Restaurant");
        favoriteDto.setAddress("123 Test St");
        favoriteDto.setAverageRating(4.5);
        favoriteDto.setReviewCount(10);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldDisplayFavoritesPage() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        Page<FavoriteRestaurantDto> page = new PageImpl<>(Arrays.asList(favoriteDto));
        when(favoriteService.getFavoriteRestaurantsWithFilters(
                eq(customer.getCustomerId()), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/customer/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/favorites-advanced"))
                .andExpect(model().attributeExists("favorites"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldDisplayFavoritesPage_WithPagination() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        Page<FavoriteRestaurantDto> page = new PageImpl<>(Arrays.asList(favoriteDto));
        when(favoriteService.getFavoriteRestaurantsWithFilters(
                eq(customer.getCustomerId()), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/customer/favorites")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldDisplayFavoritesPage_WithFilters() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        Page<FavoriteRestaurantDto> page = new PageImpl<>(Arrays.asList(favoriteDto));
        when(favoriteService.getFavoriteRestaurantsWithFilters(
                eq(customer.getCustomerId()), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/customer/favorites")
                        .param("search", "restaurant")
                        .param("cuisineType", "Vietnamese")
                        .param("priceRange", "100000-200000")
                        .param("ratingFilter", "4"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", "restaurant"))
                .andExpect(model().attribute("cuisineType", "Vietnamese"));
    }

    @Test
    void shouldRedirectToLogin_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/customer/favorites"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldToggleFavorite_Add() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        ToggleFavoriteResponse response = ToggleFavoriteResponse.success(true, 15, 1);
        when(favoriteService.toggleFavorite(eq(customer.getCustomerId()), any(ToggleFavoriteRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/customer/favorites/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isFavorited").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldToggleFavorite_Remove() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        ToggleFavoriteResponse response = ToggleFavoriteResponse.success(false, 14, 1);
        when(favoriteService.toggleFavorite(eq(customer.getCustomerId()), any(ToggleFavoriteRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/customer/favorites/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isFavorited").value(false));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldToggleFavorite_CustomerNotFound() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        mockMvc.perform(post("/customer/favorites/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldCheckFavorite() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(favoriteService.isFavorited(customer.getCustomerId(), 1)).thenReturn(true);

        mockMvc.perform(get("/customer/favorites/check/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldCheckFavorite_NotFavorited() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(favoriteService.isFavorited(customer.getCustomerId(), 1)).thenReturn(false);

        mockMvc.perform(get("/customer/favorites/check/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldGetFavoritedRestaurantIds() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(favoriteService.getFavoritedRestaurantIds(customer.getCustomerId()))
                .thenReturn(Arrays.asList(1, 2, 3));

        mockMvc.perform(get("/customer/favorites/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2))
                .andExpect(jsonPath("$[2]").value(3));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldHandleException() throws Exception {
        when(customerRepository.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(favoriteService.getFavoriteRestaurantsWithFilters(
                eq(customer.getCustomerId()), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/customer/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/favorites-advanced"))
                .andExpect(model().attributeExists("error"));
    }
}
