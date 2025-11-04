package com.example.booking.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FaviconController.class)
@AutoConfigureMockMvc(addFilters = false)
class FaviconControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void favicon_shouldRedirectToIcon() throws Exception {
		mockMvc.perform(get("/favicon.ico"))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("twemoji")));
	}
}


