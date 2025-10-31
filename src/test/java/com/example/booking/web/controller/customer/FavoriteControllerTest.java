package com.example.booking.web.controller.customer;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.context.annotation.Import;

import com.example.booking.config.TestRateLimitingConfig;
import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.customer.FavoriteRestaurantDto;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.service.FavoriteService;

@WebMvcTest(controllers = FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRateLimitingConfig.class)
class FavoriteControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private FavoriteService favoriteService;
    @MockBean private CustomerRepository customerRepository;

    private User authedUser;
    private Customer customer;
    private java.util.UUID customerId;

    @BeforeEach
    void setup() {
        authedUser = new User();
        UUID uid = UUID.randomUUID();
        authedUser.setId(uid);
        authedUser.setRole(com.example.booking.domain.UserRole.CUSTOMER);
        customer = new Customer();
        customerId = UUID.randomUUID();
        customer.setCustomerId(customerId);
        when(customerRepository.findByUserId(uid)).thenReturn(java.util.Optional.of(customer));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authedUser, "pwd", authedUser.getAuthorities()));
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();
    }

    // TC FC-001
    @Test
    @DisplayName("should render favorites page with filters (FC-001)")
    void shouldRenderFavoritesPage() throws Exception {
        Page<FavoriteRestaurantDto> page = new PageImpl<>(List.of());
        when(favoriteService.getFavoriteRestaurantsWithFilters(eq(customerId), any(), any(), any(), any(), any())).thenReturn(page);
        mockMvc.perform(get("/customer/favorites").param("search", "pho").param("cuisineType", "Viet"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/favorites-advanced"))
                .andExpect(model().attributeExists("favorites", "currentPage", "totalPages"));
    }

    // TC FC-002
    @Test
    @DisplayName("should handle service error and still render view (FC-002)")
    void shouldHandleServiceError() throws Exception {
        when(favoriteService.getFavoriteRestaurantsWithFilters(eq(customerId), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));
        mockMvc.perform(get("/customer/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/favorites-advanced"))
                .andExpect(model().attributeExists("error"));
    }

    // TC FC-003
    @Test
    @DisplayName("should redirect to login when unauthenticated (FC-003)")
    void shouldRedirectWhenUnauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/customer/favorites"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // TC FC-004
    @Test
    @DisplayName("should toggle favorite successfully (FC-004)")
    void shouldToggleFavorite() throws Exception {
        ToggleFavoriteResponse resp = ToggleFavoriteResponse.success(true, 5, 1);
        when(favoriteService.toggleFavorite(eq(customerId), org.mockito.ArgumentMatchers.<ToggleFavoriteRequest>any())).thenReturn(resp);
        mockMvc.perform(post("/customer/favorites/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // TC FC-005
    @Test
    @DisplayName("should return 400 when customer not found (FC-005)")
    void shouldReturnBadRequestWhenNoCustomer() throws Exception {
        // override to simulate unknown user
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new User(), "pwd"));
        when(customerRepository.findByUserId(any())).thenReturn(java.util.Optional.empty());
        mockMvc.perform(post("/customer/favorites/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    // TC FC-006
    @Test
    @DisplayName("checkFavorite returns false when unauthenticated (FC-006)")
    void checkFavorite_unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/customer/favorites/check/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // TC FC-007
    @Test
    @DisplayName("checkFavorite returns true when favorited (FC-007)")
    void checkFavorite_true() throws Exception {
        when(favoriteService.isFavorited(customerId, 1)).thenReturn(true);
        mockMvc.perform(get("/customer/favorites/check/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // TC FC-008
    @Test
    @DisplayName("getFavoritedRestaurantIds returns list (FC-008)")
    void getFavoritedIds_list() throws Exception {
        when(favoriteService.getFavoritedRestaurantIds(customerId)).thenReturn(List.of(1,2,3));
        mockMvc.perform(get("/customer/favorites/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is(1)))
                .andExpect(jsonPath("$[1]", is(2)))
                .andExpect(jsonPath("$[2]", is(3)));
    }

    // TC FC-009
    @Test
    @DisplayName("getFavoritedRestaurantIds handles error (FC-009)")
    void getFavoritedIds_error() throws Exception {
        when(favoriteService.getFavoritedRestaurantIds(customerId)).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(get("/customer/favorites/ids"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}


