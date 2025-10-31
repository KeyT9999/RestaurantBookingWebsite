package com.example.booking.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DemoController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DemoController WebMvc Integration Tests")
class DemoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

    @Test
    @DisplayName("GET /demo/home-demo should return view successfully")
    void testHomeDemo() throws Exception {
        mockMvc.perform(get("/demo/home-demo"))
            .andExpect(status().isOk())
            .andExpect(view().name("backup/home-demo"));
    }

    @Test
    @DisplayName("GET /demo/restaurant-home-demo should return view successfully")
    void testRestaurantHomeDemo() throws Exception {
        mockMvc.perform(get("/demo/restaurant-home-demo"))
            .andExpect(status().isOk())
            .andExpect(view().name("backup/restaurant-home-demo"));
    }

    @Test
    @DisplayName("GET /demo/resy-style-demo should return view successfully")
    void testResyStyleDemo() throws Exception {
        mockMvc.perform(get("/demo/resy-style-demo"))
            .andExpect(status().isOk())
            .andExpect(view().name("backup/resy-style-demo"));
    }
}

