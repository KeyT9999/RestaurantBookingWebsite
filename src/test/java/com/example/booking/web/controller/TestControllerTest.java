package com.example.booking.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.test.OpenAITest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@AutoConfigureMockMvc(addFilters = false)
class TestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OpenAITest openAITest;

	@BeforeEach
	void setUp() {
		// Reset mocks before each test
		reset(openAITest);
	}

	// ========== testOpenAI() Tests ==========

	@Test
	@DisplayName("testOpenAI - should return success when API key works")
	void testOpenAI_ShouldReturnSuccess() throws Exception {
		doNothing().when(openAITest).testOpenAIKey();
		mockMvc.perform(get("/test/openai"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"))
				.andExpect(jsonPath("$.message").value("OpenAI API key is working"));
	}

	@Test
	@DisplayName("testOpenAI - should handle exceptions gracefully")
	void testOpenAI_WithException_ShouldReturnError() throws Exception {
		doThrow(new RuntimeException("API key invalid")).when(openAITest).testOpenAIKey();
		mockMvc.perform(get("/test/openai"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("error"))
				.andExpect(jsonPath("$.message").exists());
	}

	// ========== testIntentParsing() Tests ==========

	@Test
	@DisplayName("testIntentParsing - should return success")
	void testIntentParsing_ShouldReturnSuccess() throws Exception {
		doNothing().when(openAITest).testRestaurantIntentParsing();
		mockMvc.perform(get("/test/openai/intent"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"))
				.andExpect(jsonPath("$.message").value("Intent parsing test completed"));
	}

	@Test
	@DisplayName("testIntentParsing - should handle exceptions gracefully")
	void testIntentParsing_WithException_ShouldReturnError() throws Exception {
		doThrow(new RuntimeException("Intent parsing failed")).when(openAITest).testRestaurantIntentParsing();
		mockMvc.perform(get("/test/openai/intent"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("error"))
				.andExpect(jsonPath("$.message").exists());
	}

	// ========== testCustomQuery() Tests ==========

	@Test
	@DisplayName("testCustomQuery - should handle missing query")
	void testCustomQuery_MissingQuery_ReturnsBadRequest() throws Exception {
		mockMvc.perform(
				post("/test/openai/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}")
		)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("error"))
				.andExpect(jsonPath("$.message").value("Query is required"));
	}

	@Test
	@DisplayName("testCustomQuery - should handle empty query")
	void testCustomQuery_EmptyQuery_ReturnsBadRequest() throws Exception {
		mockMvc.perform(
				post("/test/openai/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"query\":\"\"}")
		)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("error"));
	}

	@Test
	@DisplayName("testCustomQuery - should handle whitespace-only query")
	void testCustomQuery_WhitespaceQuery_ReturnsBadRequest() throws Exception {
		mockMvc.perform(
				post("/test/openai/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"query\":\"   \"}")
		)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("error"));
	}

	@Test
	@DisplayName("testCustomQuery - should handle invalid JSON")
	void testCustomQuery_InvalidJson_ReturnsBadRequest() throws Exception {
		mockMvc.perform(
				post("/test/openai/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content("invalid json")
		)
				.andExpect(status().isBadRequest());
	}
}



