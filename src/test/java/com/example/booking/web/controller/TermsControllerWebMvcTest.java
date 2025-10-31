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

@WebMvcTest(controllers = TermsController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TermsController WebMvc Integration Tests")
class TermsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

    @Test
    @DisplayName("GET /terms-of-service should return view successfully")
    void testTermsOfService() throws Exception {
        mockMvc.perform(get("/terms-of-service"))
            .andExpect(status().isOk())
            .andExpect(view().name("public/terms-of-service"));
    }

    @Test
    @DisplayName("GET /privacy-policy should return view successfully")
    void testPrivacyPolicy() throws Exception {
        mockMvc.perform(get("/privacy-policy"))
            .andExpect(status().isOk())
            .andExpect(view().name("public/privacy-policy"));
    }

    @Test
    @DisplayName("GET /cookie-policy should return view successfully")
    void testCookiePolicy() throws Exception {
        mockMvc.perform(get("/cookie-policy"))
            .andExpect(status().isOk())
            .andExpect(view().name("public/cookie-policy"));
    }
}

