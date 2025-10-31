package com.example.booking.web.controller;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SetupController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SetupController WebMvc Tests")
class SetupControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RestaurantOwnerRepository restaurantOwnerRepository;

    @MockBean
    private RestaurantProfileRepository restaurantProfileRepository;

    @MockBean
    private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

    @MockBean
    private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

    private User ownerUser;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setUsername("owner");
        ownerUser.setEmail("owner@test.com");
        ownerUser.setRole(UserRole.RESTAURANT_OWNER);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        Page<User> userPage = new PageImpl<>(Arrays.asList(ownerUser));
        when(userRepository.findByRole(eq(UserRole.RESTAURANT_OWNER), any(Pageable.class)))
            .thenReturn(userPage);
    }

    @Test
    @DisplayName("GET /setup/restaurant-owner - should show setup form")
    void testSetupRestaurantOwner() throws Exception {
        mockMvc.perform(get("/setup/restaurant-owner"))
            .andExpect(status().isOk())
            .andExpect(view().name("setup/simple"));
    }

    @Test
    @DisplayName("POST /setup/restaurant-owner - should create restaurant owner")
    void testCreateRestaurantOwner_Success() throws Exception {
        // Given
        RestaurantOwner savedOwner = new RestaurantOwner();
        savedOwner.setOwnerId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        savedOwner.setUser(ownerUser);
        when(restaurantOwnerRepository.save(any())).thenReturn(savedOwner);
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantProfileRepository.save(any())).thenReturn(restaurant);

        // When & Then
        mockMvc.perform(post("/setup/restaurant-owner").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/setup/restaurant-owner"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /setup/restaurant-owner - should handle no owner user found")
    void testCreateRestaurantOwner_NoUserFound() throws Exception {
        // Given
        Page<User> emptyPage = new PageImpl<>(Arrays.asList());
        when(userRepository.findByRole(eq(UserRole.RESTAURANT_OWNER), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(post("/setup/restaurant-owner").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/setup/restaurant-owner"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /setup/restaurant-owner - should handle exception")
    void testCreateRestaurantOwner_Exception() throws Exception {
        // Given
        when(userRepository.findByRole(eq(UserRole.RESTAURANT_OWNER), any(Pageable.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/setup/restaurant-owner").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/setup/restaurant-owner"))
            .andExpect(flash().attributeExists("errorMessage"));
    }
}

