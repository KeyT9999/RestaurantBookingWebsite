package com.example.booking.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminSetupController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSetupControllerTest2 {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;

	@Test
	void setupPage_shouldRenderView() throws Exception {
		mockMvc.perform(get("/admin-setup"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/setup"));
	}

	@Test
	void createAdmin_whenNoAdmin_shouldReturnViewWithMessage() throws Exception {
		when(userRepository.findByRole(eq(UserRole.ADMIN), any())).thenReturn(Page.empty());
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
		mockMvc.perform(post("/admin-setup/create-admin"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/setup"))
				.andExpect(model().attributeExists("message"));
	}
}


