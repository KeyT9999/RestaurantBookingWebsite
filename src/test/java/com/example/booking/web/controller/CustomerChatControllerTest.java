package com.example.booking.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.SimpleUserService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ChatService chatService;

	@MockBean
	private SimpleUserService userService;

	private User testUser;
	private ChatRoomDto testChatRoom;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(UUID.randomUUID());
		testUser.setUsername("customer@example.com");
		testUser.setEmail("customer@example.com");
		testUser.setFullName("Test Customer");
		testUser.setRole(UserRole.CUSTOMER);

		testChatRoom = new ChatRoomDto();
		testChatRoom.setRoomId("room-123");
		testChatRoom.setParticipantName("Restaurant Name");
	}

	// ========== chatPage() Tests ==========

	@Test
	@DisplayName("chatPage - should render chat page with rooms")
	void chatPage_ShouldRenderChatPage() throws Exception {
		when(chatService.getUserChatRooms(eq(testUser.getId()), eq(testUser.getRole())))
				.thenReturn(Arrays.asList(testChatRoom));

		mockMvc.perform(get("/customer/chat")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("customer/chat"))
				.andExpect(model().attributeExists("chatRooms", "currentUser"));
	}

	@Test
	@DisplayName("chatPage - should render chat page with empty rooms")
	void chatPage_WithEmptyRooms_ShouldRenderChatPage() throws Exception {
		when(chatService.getUserChatRooms(eq(testUser.getId()), eq(testUser.getRole())))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/customer/chat")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("customer/chat"))
				.andExpect(model().attribute("chatRooms", Collections.emptyList()));
	}

	@Test
	@DisplayName("chatPage - should render chat page with multiple rooms")
	void chatPage_WithMultipleRooms_ShouldRenderChatPage() throws Exception {
		ChatRoomDto room2 = new ChatRoomDto();
		room2.setRoomId("room-456");
		List<ChatRoomDto> rooms = Arrays.asList(testChatRoom, room2);

		when(chatService.getUserChatRooms(eq(testUser.getId()), eq(testUser.getRole())))
				.thenReturn(rooms);

		mockMvc.perform(get("/customer/chat")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attribute("chatRooms", rooms));
	}
}

