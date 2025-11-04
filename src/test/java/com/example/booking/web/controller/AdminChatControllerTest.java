package com.example.booking.web.controller;

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

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.ChatService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
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

	private User testAdmin;
	private User testCustomer;

	@BeforeEach
	void setUp() {
		testAdmin = new User();
		testAdmin.setId(UUID.randomUUID());
		testAdmin.setUsername("admin@example.com");
		testAdmin.setEmail("admin@example.com");
		testAdmin.setFullName("Admin User");
		testAdmin.setRole(UserRole.ADMIN);

		testCustomer = new User();
		testCustomer.setId(UUID.randomUUID());
		testCustomer.setUsername("customer@example.com");
		testCustomer.setEmail("customer@example.com");
		testCustomer.setFullName("Customer User");
		testCustomer.setRole(UserRole.CUSTOMER);
	}

	// ========== adminChatPage() Tests ==========

	@Test
	@DisplayName("adminChatPage - should render view for admin user")
	void adminChatPage_WithAdminUser_ShouldRenderView() throws Exception {
		mockMvc.perform(get("/admin/chat")
				.principal(new UsernamePasswordAuthenticationToken(testAdmin, null, testAdmin.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/chat"))
				.andExpect(model().attributeExists("admin", "adminId", "adminName", "adminEmail"))
				.andExpect(model().attribute("adminId", testAdmin.getId()))
				.andExpect(model().attribute("adminName", testAdmin.getFullName()))
				.andExpect(model().attribute("adminEmail", testAdmin.getEmail()));
	}

	@Test
	@DisplayName("adminChatPage - should redirect for non-admin user")
	void adminChatPage_WithNonAdmin_ShouldRedirectError() throws Exception {
		mockMvc.perform(get("/admin/chat")
				.principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error?message=Access denied. Admin role required."));
	}

	@Test
	@DisplayName("adminChatPage - should redirect for restaurant owner")
	void adminChatPage_WithRestaurantOwner_ShouldRedirectError() throws Exception {
		User owner = new User();
		owner.setId(UUID.randomUUID());
		owner.setUsername("owner@example.com");
		owner.setRole(UserRole.RESTAURANT_OWNER);

		mockMvc.perform(get("/admin/chat")
				.principal(new UsernamePasswordAuthenticationToken(owner, null, owner.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error?message=Access denied. Admin role required."));
	}

	@Test
	@DisplayName("adminChatPage - should handle authentication token with username")
	void adminChatPage_WithUsernameAuthentication_ShouldRenderView() throws Exception {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				testAdmin, null, testAdmin.getAuthorities());
		when(userRepository.findByEmail(testAdmin.getEmail())).thenReturn(Optional.of(testAdmin));

		mockMvc.perform(get("/admin/chat")
				.principal(token))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/chat"));
	}

	@Test
	@DisplayName("adminChatPage - should handle null authentication")
	void adminChatPage_WithNullAuthentication_ShouldRedirectError() throws Exception {
		mockMvc.perform(get("/admin/chat"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error?message=Error loading admin chat: No authentication found"));
	}

	@Test
	@DisplayName("adminChatPage - should handle exceptions gracefully")
	void adminChatPage_WithException_ShouldRedirectError() throws Exception {
		when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("Database error"));
		
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				"test@example.com", null, Collections.emptyList());

		mockMvc.perform(get("/admin/chat")
				.principal(token))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error?message=Error loading admin chat: Database error"));
	}

	@Test
	@DisplayName("adminChatPage - should handle unsupported authentication type")
	void adminChatPage_WithUnsupportedAuth_ShouldRedirectError() throws Exception {
		// Create a token with unsupported principal type
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				new Object(), null, Collections.emptyList());

		mockMvc.perform(get("/admin/chat")
				.principal(token))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("*/error*"));
	}

	@Test
	@DisplayName("adminChatPage - should handle user not found in repository")
	void adminChatPage_WithUserNotFound_ShouldRedirectError() throws Exception {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				"nonexistent@example.com", null, Collections.emptyList());
		
		when(userRepository.findByEmail("nonexistent@example.com"))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/admin/chat")
				.principal(token))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("*/error*"));
	}

	@Test
	@DisplayName("adminChatPage - should handle admin role (lowercase)")
	void adminChatPage_WithAdminRoleLowercase_ShouldRenderView() throws Exception {
		User admin = new User();
		admin.setId(UUID.randomUUID());
		admin.setUsername("admin@example.com");
		admin.setEmail("admin@example.com");
		admin.setFullName("Admin User");
		admin.setRole(UserRole.admin); // lowercase admin

		mockMvc.perform(get("/admin/chat")
				.principal(new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/chat"));
	}

	@Test
	@DisplayName("adminChatPage - should handle authentication token principal")
	void adminChatPage_WithAuthTokenPrincipal_ShouldHandleCorrectly() throws Exception {
		// Test when principal is a User object directly
		mockMvc.perform(get("/admin/chat")
				.principal(new UsernamePasswordAuthenticationToken(testAdmin, null, testAdmin.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/chat"));
	}
}


