package com.example.booking.web.controller.api;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.ChatService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ChatService chatService;

	@MockBean
	private RestaurantManagementService restaurantManagementService;

	@MockBean
	private RestaurantOwnerService restaurantOwnerService;

	@MockBean
	private SimpleUserService simpleUserService;

	@Test
	void availableRestaurants_customer_returnsOk() throws Exception {
		User user = new User("u1", "u1@example.com", "password123", "U One");
		user.setId(UUID.randomUUID());
		user.setRole(UserRole.CUSTOMER);
		when(restaurantManagementService.findAllRestaurants()).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/api/chat/available-restaurants")
				.principal(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())))
				.andExpect(status().isOk());
	}

	@Test
	void createChatRoom_customer_success() throws Exception {
		User user = new User("u2", "u2@example.com", "password123", "U Two");
		user.setId(UUID.randomUUID());
		user.setRole(UserRole.CUSTOMER);
		when(chatService.canUserChatWithRestaurant(eq(user.getId()), eq(user.getRole()), eq(5))).thenReturn(true);
		doNothing().when(chatService).createCustomerRestaurantRoom(eq(user.getId()), eq(5));
		when(chatService.getRoomId(eq(user.getId()), eq(user.getRole()), eq(5))).thenReturn("room-1");

		mockMvc.perform(post("/api/chat/rooms").param("restaurantId", "5")
				.principal(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roomId").value("room-1"));
	}
}
