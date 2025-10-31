package com.example.booking.web.controller;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.service.CustomerService;
import com.example.booking.service.NotificationService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.ReviewService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RestaurantOwnerService restaurantOwnerService;

	@MockBean
	private RestaurantManagementService restaurantManagementService;

	@MockBean
	private CustomerService customerService;

	@MockBean
	private ReviewService reviewService;

	@MockBean
	private RestaurantMediaRepository restaurantMediaRepository;

	@MockBean
	private NotificationService notificationService;

	@Test
	void home_shouldRenderPublicHome() throws Exception {
		when(restaurantManagementService.findTopRatedRestaurants(6)).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/home"))
				.andExpect(model().attributeExists("popularRestaurants"));
	}
}
