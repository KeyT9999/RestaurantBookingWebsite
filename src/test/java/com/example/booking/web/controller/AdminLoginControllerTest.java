package com.example.booking.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminLoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminLoginControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminLogin_shouldRenderLoginView() throws Exception {
		mockMvc.perform(get("/admin-login"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/login"));
	}
}


