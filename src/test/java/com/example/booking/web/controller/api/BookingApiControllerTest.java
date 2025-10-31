package com.example.booking.web.controller.api;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RestaurantManagementService restaurantManagementService;

	@MockBean
	private BookingService bookingService;

	@MockBean
	private CustomerService customerService;

	@MockBean
	private SimpleUserService userService;

	@Test
	void getTablesByRestaurant_returnsOk() throws Exception {
		when(restaurantManagementService.findTablesByRestaurant(1)).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/api/booking/restaurants/1/tables"))
				.andExpect(status().isOk());
	}

	@Test
	void getRestaurant_returnsNotFoundWhenMissing() throws Exception {
		when(restaurantManagementService.findRestaurantById(99)).thenReturn(Optional.empty());
		mockMvc.perform(get("/api/booking/restaurants/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void getBookingDetails_authorizedCustomer_returnsOk() throws Exception {
		User user = new User("u1", "u1@example.com", "password123", "U One");
		user.setId(UUID.randomUUID());
		Customer customer = new Customer(user);
		customer.setCustomerId(UUID.randomUUID());
		when(customerService.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(customer));

		Booking booking = new Booking();
		booking.setBookingId(10);
		RestaurantProfile rp = new RestaurantProfile();
		rp.setRestaurantId(1);
		booking.setRestaurant(rp);
		booking.setCustomer(customer);
		when(bookingService.findBookingById(10)).thenReturn(Optional.of(booking));
		when(bookingService.calculateTotalAmount(any(Booking.class))).thenReturn(java.math.BigDecimal.ZERO);

		mockMvc.perform(get("/api/booking/10/details")
				.principal(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookingId").value(10));
	}
}


