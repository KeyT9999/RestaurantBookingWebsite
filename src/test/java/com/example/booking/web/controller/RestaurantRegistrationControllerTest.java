package com.example.booking.web.controller;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

/**
 * Test cases for RestaurantRegistrationController
 */
@WebMvcTest(RestaurantRegistrationController.class)
class RestaurantRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService userService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateRestaurantForm_WithCustomerRole_ShouldReturnForm() throws Exception {
        // Given
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setRole(UserRole.CUSTOMER);
        mockUser.setUsername("testuser");

        when(userService.findByUsername(any())).thenReturn(Optional.of(mockUser));

        // When & Then
        mockMvc.perform(get("/restaurant-owner/restaurants/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/restaurant-form"))
                .andExpect(model().attribute("pageTitle", "Đăng ký nhà hàng"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void testCreateRestaurantForm_WithRestaurantOwnerRole_ShouldRedirect() throws Exception {
        // Given
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setRole(UserRole.RESTAURANT_OWNER);
        mockUser.setUsername("restaurantowner");

        when(userService.findByUsername(any())).thenReturn(Optional.of(mockUser));

        // When & Then
        mockMvc.perform(get("/restaurant-owner/restaurants/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?error=unauthorized"));
    }

    @Test
    void testCreateRestaurantForm_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(get("/restaurant-owner/restaurants/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateRestaurant_WithTermsAccepted_ShouldSuccess() throws Exception {
        // Given
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setRole(UserRole.CUSTOMER);
        mockUser.setUsername("testuser");

        when(userService.findByUsername(any())).thenReturn(Optional.of(mockUser));

        // When & Then
        mockMvc.perform(post("/restaurant-owner/restaurants/create")
                .param("restaurantName", "Test Restaurant")
                .param("address", "123 Test Street")
                .param("phone", "0123456789")
                .param("termsAccepted", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/restaurants/create?success=1"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateRestaurant_WithoutTermsAccepted_ShouldFail() throws Exception {
        // Given
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setRole(UserRole.CUSTOMER);
        mockUser.setUsername("testuser");

        when(userService.findByUsername(any())).thenReturn(Optional.of(mockUser));

        // When & Then
        mockMvc.perform(post("/restaurant-owner/restaurants/create")
                .param("restaurantName", "Test Restaurant")
                .param("address", "123 Test Street")
                .param("phone", "0123456789")
                .param("termsAccepted", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/restaurants/create"))
                .andExpect(flash().attribute("error", "Vui lòng đồng ý với điều khoản sử dụng để tiếp tục"));
    }
}
