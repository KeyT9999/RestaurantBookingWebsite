package com.example.booking.web.controller.admin;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WorkingRateLimitingController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkingRateLimitingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RateLimitStatisticsRepository statisticsRepository;

	@Test
	void dashboard_shouldRenderView() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(java.util.Collections.emptyList());
		mockMvc.perform(get("/admin/rate-limiting"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/rate-limiting/dashboard"));
	}

	@Test
	void getStatistics_shouldReturnJson() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(java.util.Collections.emptyList());
		mockMvc.perform(get("/admin/rate-limiting/api/statistics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalRequests").exists());
	}

	@Test
	void blockIp_shouldReturnSuccess() throws Exception {
		when(statisticsRepository.findByIpAddress(eq("1.2.3.4"))).thenReturn(Optional.empty());
		when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));
		mockMvc.perform(post("/admin/rate-limiting/api/block-ip")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"1.2.3.4\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}
}


