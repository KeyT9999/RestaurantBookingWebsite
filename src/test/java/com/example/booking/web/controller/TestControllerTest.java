package com.example.booking.web.controller;

import com.example.booking.test.OpenAITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for TestController
 */
@WebMvcTest(TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TestController Test Suite")
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAITest openAITest;

    @Nested
    @DisplayName("testOpenAI() Tests")
    class TestOpenAITests {

        @Test
        @DisplayName("Should test OpenAI API successfully")
        void testTestOpenAI_WithValidKey_ShouldReturnSuccess() throws Exception {
            doNothing().when(openAITest).testOpenAIKey();

            mockMvc.perform(get("/test/openai"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").exists());

            verify(openAITest).testOpenAIKey();
        }

        @Test
        @DisplayName("Should handle OpenAI API test failure")
        void testTestOpenAI_WhenTestFails_ShouldReturnError() throws Exception {
            doThrow(new RuntimeException("API key invalid")).when(openAITest).testOpenAIKey();

            mockMvc.perform(get("/test/openai"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").exists());

            verify(openAITest).testOpenAIKey();
        }
    }

    @Nested
    @DisplayName("testIntentParsing() Tests")
    class TestIntentParsingTests {

        @Test
        @DisplayName("Should test intent parsing successfully")
        void testTestIntentParsing_WithValidTest_ShouldReturnSuccess() throws Exception {
            doNothing().when(openAITest).testRestaurantIntentParsing();

            mockMvc.perform(get("/test/openai/intent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").exists());

            verify(openAITest).testRestaurantIntentParsing();
        }

        @Test
        @DisplayName("Should handle intent parsing test failure")
        void testTestIntentParsing_WhenTestFails_ShouldReturnError() throws Exception {
            doThrow(new RuntimeException("Intent parsing failed")).when(openAITest).testRestaurantIntentParsing();

            mockMvc.perform(get("/test/openai/intent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").exists());

            verify(openAITest).testRestaurantIntentParsing();
        }
    }

    @Nested
    @DisplayName("testCustomQuery() Tests")
    class TestCustomQueryTests {

        @Test
        @DisplayName("Should test custom query successfully")
        void testTestCustomQuery_WithValidQuery_ShouldReturnSuccess() throws Exception {
            mockMvc.perform(post("/test/openai/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"query\":\"Test query\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").exists());
        }

        @Test
        @DisplayName("Should handle empty query")
        void testTestCustomQuery_WithEmptyQuery_ShouldReturnError() throws Exception {
            mockMvc.perform(post("/test/openai/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"query\":\"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should handle missing query parameter")
        void testTestCustomQuery_WithMissingQuery_ShouldReturnError() throws Exception {
            mockMvc.perform(post("/test/openai/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should handle invalid JSON")
        void testTestCustomQuery_WithInvalidJson_ShouldReturnError() throws Exception {
            mockMvc.perform(post("/test/openai/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle null query in request")
        void testTestCustomQuery_WithNullQuery_ShouldReturnError() throws Exception {
            mockMvc.perform(post("/test/openai/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"query\":null}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should handle whitespace-only query")
        void testTestCustomQuery_WithWhitespaceQuery_ShouldReturnError() throws Exception {
            mockMvc.perform(post("/test/openai/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"query\":\"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
