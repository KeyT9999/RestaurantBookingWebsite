package com.example.booking.web.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantProfileRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(SimpleAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class SimpleAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RestaurantProfileRepository restaurantProfileRepository;

	@Test
	void simpleRestaurantList_shouldRenderPendingList() throws Exception {
		RestaurantProfile p1 = new RestaurantProfile();
		p1.setRestaurantId(1);
		p1.setRestaurantName("A");
		p1.setApprovalStatus(RestaurantApprovalStatus.PENDING);
		RestaurantProfile p2 = new RestaurantProfile();
		p2.setRestaurantId(2);
		p2.setRestaurantName("B");
		p2.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
		RestaurantProfile p3 = new RestaurantProfile();
		p3.setRestaurantId(3);
		p3.setRestaurantName("C");
		p3.setApprovalStatus(RestaurantApprovalStatus.PENDING);

		when(restaurantProfileRepository.findAll()).thenReturn(List.of(p1, p2, p3));

		mockMvc.perform(get("/admin/simple/restaurants"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/simple-restaurant-list"))
				.andExpect(model().attributeExists("restaurants", "pendingCount", "approvedCount", "rejectedCount", "suspendedCount"));
	}

	@Test
	void simpleApproveRestaurant_shouldRedirectWithSuccess() throws Exception {
		RestaurantProfile p = new RestaurantProfile();
		p.setRestaurantId(10);
		p.setRestaurantName("Test R");
		p.setApprovalStatus(RestaurantApprovalStatus.PENDING);
		when(restaurantProfileRepository.findById(eq(10))).thenReturn(Optional.of(p));
		when(restaurantProfileRepository.save(any(RestaurantProfile.class))).thenAnswer(inv -> inv.getArgument(0));

		mockMvc.perform(post("/admin/simple/approve/10"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/simple/restaurants"))
				.andExpect(flash().attributeExists("success"));
	}

	@Test
	void simpleRejectRestaurant_shouldRedirectWithSuccess() throws Exception {
		RestaurantProfile p = new RestaurantProfile();
		p.setRestaurantId(11);
		p.setRestaurantName("Test R2");
		p.setApprovalStatus(RestaurantApprovalStatus.PENDING);
		when(restaurantProfileRepository.findById(eq(11))).thenReturn(Optional.of(p));
		when(restaurantProfileRepository.save(any(RestaurantProfile.class))).thenAnswer(inv -> inv.getArgument(0));

		mockMvc.perform(post("/admin/simple/reject/11").param("reason", "Not good"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/simple/restaurants"))
				.andExpect(flash().attributeExists("success"));
	}
}


