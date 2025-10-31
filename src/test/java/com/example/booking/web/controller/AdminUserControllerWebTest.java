package com.example.booking.web.controller;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerWebTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@MockBean
	private BookingRepository bookingRepository;

	@Test
	void list_shouldRenderView() throws Exception {
		when(userRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/admin/users"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/users"))
				.andExpect(model().attributeExists("users", "totalPages", "currentPage"));
	}

	@Test
	void toggleActive_shouldReturnSuccessJson() throws Exception {
		UUID id = UUID.randomUUID();
		User user = new User("u1", "u1@example.com", "password123", "U One");
		user.setId(id);
		user.setRole(UserRole.CUSTOMER);
		user.setActive(false);
		user.setEmailVerified(false);
		when(userRepository.findById(id)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		mockMvc.perform(post("/admin/users/" + id + "/toggle-active").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}
}


