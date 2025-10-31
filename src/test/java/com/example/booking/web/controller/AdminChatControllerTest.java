package com.example.booking.web.controller;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.ChatService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ChatService chatService;

	@MockBean
	private UserRepository userRepository;

	@Test
	void adminChat_withAdminUser_shouldRenderView() throws Exception {
		User admin = new User("admin", "a@example.com", "password123", "Admin");
		admin.setRole(UserRole.ADMIN);
		admin.setId(UUID.randomUUID());

		mockMvc.perform(get("/admin/chat")
				.principal(new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/chat"))
				.andExpect(model().attributeExists("admin", "adminId", "adminName", "adminEmail"));
	}

	@Test
	void adminChat_withNonAdmin_shouldRedirectError() throws Exception {
		User user = new User("user", "u@example.com", "password123", "User");
		user.setRole(UserRole.CUSTOMER);
		user.setId(UUID.randomUUID());

		mockMvc.perform(get("/admin/chat")
				.principal(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error?message=Access denied. Admin role required."));
	}
}


