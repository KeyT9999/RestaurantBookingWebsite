package com.example.booking.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DemoController.class)
@DisplayName("DemoController Test")
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /demo/home-demo - Should return home demo template")
    void testHomeDemo_ShouldReturnTemplate() throws Exception {
        mockMvc.perform(get("/demo/home-demo"))
            .andExpect(status().isOk())
            .andExpect(view().name("backup/home-demo"));
    }

    @Test
    @DisplayName("GET /demo/restaurant-home-demo - Should return restaurant home demo template")
    void testRestaurantHomeDemo_ShouldReturnTemplate() throws Exception {
        mockMvc.perform(get("/demo/restaurant-home-demo"))
            .andExpect(status().isOk())
            .andExpect(view().name("backup/restaurant-home-demo"));
    }

    @Test
    @DisplayName("GET /demo/resy-style-demo - Should return resy style demo template")
    void testResyStyleDemo_ShouldReturnTemplate() throws Exception {
        mockMvc.perform(get("/demo/resy-style-demo"))
            .andExpect(status().isOk())
            .andExpect(view().name("backup/resy-style-demo"));
    }
}

