package com.example.booking.web.controller;

import com.example.booking.test.OpenAITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TestController WebMvc Tests")
class TestControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAITest openAITest;

    @Test
    @DisplayName("GET /test/openai - should test OpenAI API")
    void testTestOpenAI() throws Exception {
        // Given
        doNothing().when(openAITest).testOpenAIKey();

        // When & Then
        mockMvc.perform(get("/test/openai"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("GET /test/openai/intent - should test intent parsing")
    void testTestIntentParsing() throws Exception {
        // Given
        doNothing().when(openAITest).testRestaurantIntentParsing();

        // When & Then
        mockMvc.perform(get("/test/openai/intent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));
    }
}

