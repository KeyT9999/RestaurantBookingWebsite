package com.example.booking.web.controller.restaurantowner;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import com.example.booking.config.TestRateLimitingConfig;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import({TestRateLimitingConfig.class, RestaurantFavoriteControllerViewIntegrationTest.RealViewResolverConfig.class})
class RestaurantFavoriteControllerViewIntegrationTest {

    static class RealViewResolverConfig {
        @Bean
        @Primary
        ViewResolver realViewResolver(ThymeleafViewResolver delegate) {
            return delegate;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService userService;

    @Test
    @DisplayName("favorites page should render thymeleaf template without errors")
    void shouldRenderFavoritesTemplate() throws Exception {
        UUID ownerId = UUID.randomUUID();

        User user = new User();
        user.setId(ownerId);
        user.setUsername("owner@example.com");
        user.setEmail("owner@example.com");
        user.setPassword("password");
        user.setFullName("Owner Name");

        RestaurantOwner owner = new RestaurantOwner();
        owner.setOwnerId(ownerId);
        owner.setUser(user);
        owner.setOwnerName("Owner Name");

        when(userService.loadUserByUsername("owner@example.com")).thenReturn(user);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(ownerId)).thenReturn(Optional.of(owner));

        FavoriteStatisticsDto dto = new FavoriteStatisticsDto(1, "Pho Delight", 12L,
                4.5, 3L, BigDecimal.TEN, "Vietnamese");
        when(favoriteService.getFavoriteStatisticsForOwner(eq(ownerId), any()))
                .thenReturn(List.of(dto));

        TestingAuthenticationToken auth = new TestingAuthenticationToken("owner@example.com", "pwd");

        mockMvc.perform(get("/restaurant-owner/favorites")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thống kê yêu thích")))
                .andExpect(content().string(containsString("Pho Delight")));
    }
}
