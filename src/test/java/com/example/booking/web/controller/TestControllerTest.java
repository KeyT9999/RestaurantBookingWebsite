package com.example.booking.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.test.OpenAITest;

import static org.mockito.Mockito.doNothing;
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

	@Test
	void testOpenAI_shouldReturnSuccess() throws Exception {
		doNothing().when(openAITest).testOpenAIKey();
		mockMvc.perform(get("/test/openai"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"));
	}

	@Test
	void testIntentParsing_shouldReturnSuccess() throws Exception {
		doNothing().when(openAITest).testRestaurantIntentParsing();
		mockMvc.perform(get("/test/openai/intent"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"));
	}

	@Test
	void testCustomQuery_missingQuery_returnsBadRequest() throws Exception {
		mockMvc.perform(
				post("/test/openai/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}")
		)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("error"));
	}
}


