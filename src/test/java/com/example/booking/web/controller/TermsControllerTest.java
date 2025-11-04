package com.example.booking.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TermsController.class)
@AutoConfigureMockMvc(addFilters = false)
class TermsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void termsOfService_shouldRenderView() throws Exception {
		mockMvc.perform(get("/terms-of-service"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/terms-of-service"));
	}

	@Test
	void privacyPolicy_shouldRenderView() throws Exception {
		mockMvc.perform(get("/privacy-policy"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/privacy-policy"));
	}

	@Test
	void cookiePolicy_shouldRenderView() throws Exception {
		mockMvc.perform(get("/cookie-policy"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/cookie-policy"));
	}
}


