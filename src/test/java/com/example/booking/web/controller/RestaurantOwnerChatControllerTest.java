package com.example.booking.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.BookingService;
import com.example.booking.service.ChatService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantOwnerChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantOwnerChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ChatService chatService;

	@MockBean
	private SimpleUserService userService;

	@MockBean
	private RestaurantOwnerService restaurantOwnerService;

	@MockBean
	private BookingService bookingService;

	private User testUser;
	private RestaurantProfile testRestaurant;
	private Booking testBooking;
	private ChatRoomDto testChatRoom;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(UUID.randomUUID());
		testUser.setUsername("owner@example.com");
		testUser.setEmail("owner@example.com");
		testUser.setFullName("Restaurant Owner");
		testUser.setRole(UserRole.RESTAURANT_OWNER);

		testRestaurant = new RestaurantProfile();
		testRestaurant.setRestaurantId(1);
		testRestaurant.setRestaurantName("Test Restaurant");

		Customer testCustomer = new Customer();
		testCustomer.setCustomerId(UUID.randomUUID());
		User customerUser = new User();
		customerUser.setId(UUID.randomUUID());
		customerUser.setRole(UserRole.CUSTOMER);
		testCustomer.setUser(customerUser);

		testBooking = new Booking();
		testBooking.setBookingId(1);
		testBooking.setRestaurant(testRestaurant);
		testBooking.setCustomer(testCustomer);

		testChatRoom = new ChatRoomDto();
		testChatRoom.setRoomId("room-123");
		testChatRoom.setParticipantName("Customer Name");
	}

	// ========== chatPage() Tests ==========

	@Test
	@DisplayName("chatPage - should render chat page with rooms")
	void chatPage_ShouldRenderChatPage() throws Exception {
		when(chatService.getUserChatRooms(eq(testUser.getId()), eq(testUser.getRole())))
				.thenReturn(Arrays.asList(testChatRoom));
		when(restaurantOwnerService.getRestaurantsByUserId(eq(testUser.getId())))
				.thenReturn(Arrays.asList(testRestaurant));

		mockMvc.perform(get("/restaurant-owner/chat")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurant-owner/chat"))
				.andExpect(model().attributeExists("chatRooms", "currentUser", "restaurants"));
	}

	@Test
	@DisplayName("chatPage - should auto-select restaurant when only one exists")
	void chatPage_WithSingleRestaurant_ShouldAutoSelect() throws Exception {
		when(chatService.getUserChatRooms(any(), any())).thenReturn(Collections.emptyList());
		when(restaurantOwnerService.getRestaurantsByUserId(eq(testUser.getId())))
				.thenReturn(Arrays.asList(testRestaurant));

		mockMvc.perform(get("/restaurant-owner/chat")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("autoSelectRestaurant"));
	}

	@Test
	@DisplayName("chatPage - should create chat room when bookingId provided")
	void chatPage_WithBookingId_ShouldCreateChatRoom() throws Exception {
		when(bookingService.findBookingById(1)).thenReturn(Optional.of(testBooking));
		when(restaurantOwnerService.getRestaurantsByUserId(any())).thenReturn(Arrays.asList(testRestaurant));
		when(chatService.getRoomId(any(), any(), any())).thenReturn(null);
		when(chatService.getUserChatRooms(any(), any())).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/restaurant-owner/chat")
				.param("bookingId", "1")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurant-owner/chat"));
	}

	@Test
	@DisplayName("chatPage - should not create duplicate chat room")
	void chatPage_WithExistingChatRoom_ShouldNotCreateDuplicate() throws Exception {
		when(bookingService.findBookingById(1)).thenReturn(Optional.of(testBooking));
		when(restaurantOwnerService.getRestaurantsByUserId(any())).thenReturn(Arrays.asList(testRestaurant));
		when(chatService.getRoomId(any(), any(), any())).thenReturn("existing-room-id");
		when(chatService.getUserChatRooms(any(), any())).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/restaurant-owner/chat")
				.param("bookingId", "1")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurant-owner/chat"));
	}

	// ========== chatRoom() Tests ==========

	@Test
	@DisplayName("chatRoom - should render chat room page")
	void chatRoom_WithValidRoom_ShouldRenderRoomPage() throws Exception {
		when(chatService.canUserAccessRoom(eq("room-123"), eq(testUser.getId()), eq(testUser.getRole())))
				.thenReturn(true);
		when(chatService.getChatRoomById("room-123")).thenReturn(Optional.of(new com.example.booking.domain.ChatRoom()));
		when(chatService.convertToDto(any())).thenReturn(testChatRoom);

		mockMvc.perform(get("/restaurant-owner/chat/room/room-123")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurant-owner/chat-room"))
				.andExpect(model().attributeExists("roomId", "currentUser", "room"));
	}

	@Test
	@DisplayName("chatRoom - should return 403 when user cannot access")
	void chatRoom_WithUnauthorizedAccess_ShouldReturn403() throws Exception {
		when(chatService.canUserAccessRoom(eq("room-123"), eq(testUser.getId()), eq(testUser.getRole())))
				.thenReturn(false);

		mockMvc.perform(get("/restaurant-owner/chat/room/room-123")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("error/403"));
	}

	@Test
	@DisplayName("chatRoom - should return 404 when room not found")
	void chatRoom_WithNonExistentRoom_ShouldReturn404() throws Exception {
		when(chatService.canUserAccessRoom(anyString(), any(), any())).thenReturn(true);
		when(chatService.getChatRoomById("non-existent")).thenReturn(Optional.empty());

		mockMvc.perform(get("/restaurant-owner/chat/room/non-existent")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("error/404"));
	}
}

