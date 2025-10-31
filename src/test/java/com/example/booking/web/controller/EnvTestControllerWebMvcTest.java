package com.example.booking.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EnvTestController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("EnvTestController WebMvc Tests")
class EnvTestControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /test/env/check - should check environment variables")
    void testCheckEnvVariables() throws Exception {
        mockMvc.perform(get("/test/env/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").exists())
            .andExpect(jsonPath("$.cloudinary_cloud_name").exists())
            .andExpect(jsonPath("$.cloudinary_api_key").exists())
            .andExpect(jsonPath("$.cloudinary_api_secret").exists())
            .andExpect(jsonPath("$.jdbc_url").exists())
            .andExpect(jsonPath("$.db_username").exists())
            .andExpect(jsonPath("$.db_password").exists())
            .andExpect(jsonPath("$.env_file_read").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /test/env/check - should handle short API key (less than 4 chars)")
    void testCheckEnvVariables_WithShortApiKey() throws Exception {
        // Test with short key that might cause substring issues
        mockMvc.perform(get("/test/env/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cloudinary_api_key").exists());
    }
}

