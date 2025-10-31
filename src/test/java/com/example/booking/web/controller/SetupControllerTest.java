package com.example.booking.web.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(SetupController.class)
@AutoConfigureMockMvc(addFilters = false)
class SetupControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RestaurantOwnerRepository restaurantOwnerRepository;

	@MockBean
	private RestaurantProfileRepository restaurantProfileRepository;

	@Test
	void getRestaurantOwnerSetup_shouldRenderSimpleView() throws Exception {
		mockMvc.perform(get("/setup/restaurant-owner"))
				.andExpect(status().isOk())
				.andExpect(view().name("setup/simple"));
	}

	@Test
	void postRestaurantOwner_noUserFound_shouldRedirectWithError() throws Exception {
		when(userRepository.findByRole(eq(UserRole.RESTAURANT_OWNER), any(Pageable.class)))
				.thenReturn(Page.empty());
		mockMvc.perform(post("/setup/restaurant-owner"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/setup/restaurant-owner"))
				.andExpect(flash().attributeExists("errorMessage"));
	}

	@Test
	void postRestaurantOwner_success_shouldRedirectWithSuccessMessage() throws Exception {
		User user = new User("owner", "o@example.com", "password123", "Owner Name");
		user.setId(UUID.randomUUID());
		Page<User> page = new PageImpl<>(java.util.List.of(user));
		when(userRepository.findByRole(eq(UserRole.RESTAURANT_OWNER), any(Pageable.class)))
				.thenReturn(page);

		RestaurantOwner savedOwner = new RestaurantOwner();
		savedOwner.setOwnerId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		savedOwner.setUser(user);
		savedOwner.setCreatedAt(LocalDateTime.now());
		when(restaurantOwnerRepository.save(any(RestaurantOwner.class))).thenReturn(savedOwner);

		RestaurantProfile rp = new RestaurantProfile();
		rp.setRestaurantId(1);
		when(restaurantProfileRepository.findById(eq(1))).thenReturn(Optional.of(rp));

		mockMvc.perform(post("/setup/restaurant-owner"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/setup/restaurant-owner"))
				.andExpect(flash().attributeExists("successMessage"));
	}
}


