package com.example.booking.web.controller.admin;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class WorkingRateLimitingControllerUnitTest {

	@Test
	void getStatistics_shouldReturnMapEvenWhenEmpty() {
		RateLimitStatisticsRepository repo = Mockito.mock(RateLimitStatisticsRepository.class);
		when(repo.findAll()).thenReturn(Collections.emptyList());

		WorkingRateLimitingController ctrl = new WorkingRateLimitingController();
		// inject mock
		try {
			var f = WorkingRateLimitingController.class.getDeclaredField("statisticsRepository");
			f.setAccessible(true);
			f.set(ctrl, repo);
		} catch (Exception e) { fail(e); }

		Map<String, Object> stats = ctrl.getStatistics();
		assertNotNull(stats);
		assertTrue(stats.containsKey("totalRequests"));
	}

	@Test
	void dashboard_shouldPopulateModel() {
		RateLimitStatisticsRepository repo = Mockito.mock(RateLimitStatisticsRepository.class);
		when(repo.findAll()).thenReturn(Collections.<RateLimitStatistics>emptyList());

		WorkingRateLimitingController ctrl = new WorkingRateLimitingController();
		try {
			var f = WorkingRateLimitingController.class.getDeclaredField("statisticsRepository");
			f.setAccessible(true);
			f.set(ctrl, repo);
		} catch (Exception e) { fail(e); }

		Model model = new ConcurrentModel();
		String view = ctrl.dashboard(model);
		assertEquals("admin/rate-limiting/dashboard", view);
		assertTrue(model.containsAttribute("overallStats"));
	}
}


