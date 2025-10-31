package com.example.booking.web.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.TestPropertySource(properties = {
	"spring.main.allow-bean-definition-overriding=true"
})
class AdminApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;
	
	@MockBean(name = "advancedRateLimitingInterceptor")
	private com.example.booking.config.AdvancedRateLimitingInterceptor advancedRateLimitingInterceptor;

	@Test
	void createAdmin_whenNoExisting_shouldCreate() throws Exception {
		when(userRepository.findByRole(eq(UserRole.ADMIN), any())).thenReturn(Page.empty());
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
		mockMvc.perform(post("/api/admin/create-admin").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("created successfully")));
	}

	@Test
	void createAdmin_whenExists_shouldReturnExistsMessage() throws Exception {
		when(userRepository.findByRole(eq(UserRole.ADMIN), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(new User())));
		mockMvc.perform(post("/api/admin/create-admin"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("already exists")));
	}

	@Test
	void createAdmin_whenException_shouldReturnError() throws Exception {
		when(userRepository.findByRole(eq(UserRole.ADMIN), any())).thenThrow(new RuntimeException("Database error"));
		mockMvc.perform(post("/api/admin/create-admin"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Error creating admin user")));
	}
}


