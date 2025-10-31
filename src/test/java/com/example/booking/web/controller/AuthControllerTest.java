package com.example.booking.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.service.AuthRateLimitingService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SimpleUserService simpleUserService;

	@MockBean
	private RestaurantOwnerService restaurantOwnerService;

	@MockBean
	private AuthRateLimitingService authRateLimitingService;

	@MockBean
	private ImageUploadService imageUploadService;

	@Test
	void showRegisterForm_shouldRenderView() throws Exception {
		mockMvc.perform(get("/auth/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/register"));
	}
}


