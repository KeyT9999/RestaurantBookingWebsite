package com.example.booking.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.config.TestRateLimitingConfig;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.dto.PopularRestaurantDto;
import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.service.CustomerService;
import com.example.booking.service.NotificationService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.ReviewService;

@WebMvcTest(controllers = HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRateLimitingConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private RestaurantOwnerService restaurantOwnerService;
    @MockBean private RestaurantManagementService restaurantManagementService;
    @MockBean private CustomerService customerService;
    @MockBean private ReviewService reviewService;
    @MockBean private NotificationService notificationService;
    @MockBean private RestaurantMediaRepository restaurantMediaRepository;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    // TC HL-004
    @Test
    @DisplayName("should render restaurants listing with filters (HL-004)")
    void shouldRenderRestaurantsListing() throws Exception {
        var page = new org.springframework.data.domain.PageImpl<RestaurantProfile>(List.of());
        when(restaurantManagementService.getRestaurantsWithFilters(any(), any(), any(), any(), any())).thenReturn(page);
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover"))).thenReturn(List.of());

        mockMvc.perform(get("/restaurants")
                        .param("page", "0").param("size", "12")
                        .param("sortBy", "restaurantName").param("sortDir", "asc")
                        .param("search", "pho").param("cuisineType", "Vietnamese")
                        .param("priceRange", "$").param("ratingFilter", ">=4"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/restaurants"))
                .andExpect(model().attributeExists("restaurants", "currentPage", "totalPages"));
    }

    // TC HL-008
    @Test
    @DisplayName("should redirect when restaurant not found (HL-008)")
    void shouldRedirectWhenRestaurantNotFound() throws Exception {
        when(restaurantOwnerService.getRestaurantById(999)).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/restaurants/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurants?error=notfound"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // TC HL-001
    @Test
    @DisplayName("should render home with popular restaurants and admin role (HL-001)")
    void shouldRenderHomeWithPopularRestaurants_andAdminRole() throws Exception {
        // Given
        User domainUser = new User();
        java.util.UUID uid = java.util.UUID.randomUUID();
        domainUser.setId(uid);
        var auth = new UsernamePasswordAuthenticationToken(domainUser, "pwd",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        RestaurantProfile r1 = new RestaurantProfile();
        r1.setRestaurantId(1);
        r1.setRestaurantName("A");
        RestaurantProfile r2 = new RestaurantProfile();
        r2.setRestaurantId(2);
        r2.setRestaurantName("B");
        when(restaurantManagementService.findTopRatedRestaurants(6)).thenReturn(List.of(r1, r2));
        when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), eq("cover"))).thenReturn(List.of());
        when(notificationService.countUnreadByUserId(uid)).thenReturn(5L);

        // When/Then
        mockMvc.perform(get("/").param("search", "pho").param("cuisineType", "Vietnamese").param("priceRange", "$"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/home"))
                .andExpect(model().attributeExists("popularRestaurants"))
                .andExpect(model().attribute("userRole", "ADMIN"))
                .andExpect(model().attribute("unreadCount", 5L));
    }

    // TC HL-002
    @Test
    @DisplayName("should render home for anonymous with empty popular list (HL-002)")
    void shouldRenderHomeForAnonymous() throws Exception {
        when(restaurantManagementService.findTopRatedRestaurants(6)).thenReturn(List.of());
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/home"))
                .andExpect(model().attributeExists("popularRestaurants"));
    }

    // TC HL-003
    @Test
    @DisplayName("should default unreadCount to 0 when notificationService fails (HL-003)")
    void shouldDefaultUnreadCountOnError() throws Exception {
        User domainUser = new User();
        java.util.UUID uid = java.util.UUID.randomUUID();
        domainUser.setId(uid);
        var auth = new UsernamePasswordAuthenticationToken(domainUser, "pwd",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(restaurantManagementService.findTopRatedRestaurants(6)).thenReturn(List.of());
        when(notificationService.countUnreadByUserId(uid)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/home"))
                .andExpect(model().attribute("unreadCount", 0L));
    }
}


