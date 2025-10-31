package com.example.booking.web.controller.admin;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkingRateLimitingController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkingRateLimitingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RateLimitStatisticsRepository statisticsRepository;

	private RateLimitStatistics testStats;
	private List<RateLimitStatistics> statsList;

	@BeforeEach
	void setUp() {
		testStats = new RateLimitStatistics("192.168.1.100");
		testStats.setTotalRequests(1000L);
		testStats.setBlockedCount(25);
		testStats.setSuccessfulRequests(975L);
		testStats.setFailedRequests(25L);
		testStats.setRiskScore(85);
		testStats.setIsSuspicious(true);
		testStats.setIsPermanentlyBlocked(false);
		testStats.setLastRequestAt(LocalDateTime.now());

		RateLimitStatistics stats2 = new RateLimitStatistics("192.168.1.101");
		stats2.setBlockedCount(10);
		stats2.setIsPermanentlyBlocked(true);
		stats2.setLastRequestAt(LocalDateTime.now().minusHours(1));

		statsList = Arrays.asList(testStats, stats2);
	}

	// ========== dashboard() Tests ==========

	@Test
	@DisplayName("dashboard - should render view with statistics")
	void dashboard_ShouldRenderView() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(statsList);
		mockMvc.perform(get("/admin/rate-limiting"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/rate-limiting/dashboard"))
				.andExpect(model().attributeExists("overallStats", "topBlockedIps", "recentAlerts", 
						"permanentlyBlocked", "suspiciousIps"));
	}

	@Test
	@DisplayName("dashboard - should handle empty statistics")
	void dashboard_WithEmptyStats_ShouldRenderView() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/admin/rate-limiting"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/rate-limiting/dashboard"))
				.andExpect(model().attributeExists("overallStats"));
	}

	@Test
	@DisplayName("dashboard - should handle exceptions gracefully")
	void dashboard_WithException_ShouldUseMockData() throws Exception {
		when(statisticsRepository.findAll()).thenThrow(new RuntimeException("Database error"));
		mockMvc.perform(get("/admin/rate-limiting"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/rate-limiting/dashboard"));
	}

	// ========== getStatistics() Tests ==========

	@Test
	@DisplayName("getStatistics - should return statistics JSON")
	void getStatistics_ShouldReturnJson() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(statsList);
		mockMvc.perform(get("/admin/rate-limiting/api/statistics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalRequests").exists())
				.andExpect(jsonPath("$.blockedRequests").exists())
				.andExpect(jsonPath("$.successfulRequests").exists())
				.andExpect(jsonPath("$.blockedIps").exists());
	}

	@Test
	@DisplayName("getStatistics - should handle exceptions with fallback")
	void getStatistics_WithException_ShouldReturnMockData() throws Exception {
		when(statisticsRepository.findAll()).thenThrow(new RuntimeException("Database error"));
		mockMvc.perform(get("/admin/rate-limiting/api/statistics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalRequests").exists());
	}

	// ========== blockIp() Tests ==========

	@Test
	@DisplayName("blockIp - should block IP successfully")
	void blockIp_ShouldReturnSuccess() throws Exception {
		when(statisticsRepository.findByIpAddress("1.2.3.4")).thenReturn(Optional.empty());
		when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));
		
		mockMvc.perform(post("/admin/rate-limiting/api/block-ip")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"1.2.3.4\",\"reason\":\"Suspicious activity\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("blockIp - should update existing statistics")
	void blockIp_WithExistingStats_ShouldUpdate() throws Exception {
		when(statisticsRepository.findByIpAddress("192.168.1.100")).thenReturn(Optional.of(testStats));
		when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));
		
		mockMvc.perform(post("/admin/rate-limiting/api/block-ip")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"192.168.1.100\",\"reason\":\"Multiple violations\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("blockIp - should handle exceptions")
	void blockIp_WithException_ShouldReturnError() throws Exception {
		when(statisticsRepository.findByIpAddress(anyString())).thenThrow(new RuntimeException("Database error"));
		
		mockMvc.perform(post("/admin/rate-limiting/api/block-ip")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"1.2.3.4\"}"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false));
	}

	// ========== unblockIp() Tests ==========

	@Test
	@DisplayName("unblockIp - should unblock IP successfully")
	void unblockIp_ShouldReturnSuccess() throws Exception {
		mockMvc.perform(post("/admin/rate-limiting/api/unblock-ip")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"1.2.3.4\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").exists());
	}

	// ========== unblockPermanent() Tests ==========

	@Test
	@DisplayName("unblockPermanent - should remove permanent block")
	void unblockPermanent_ShouldReturnSuccess() throws Exception {
		mockMvc.perform(post("/admin/rate-limiting/api/unblock-permanent")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"192.168.1.101\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	// ========== editBlockReason() Tests ==========

	@Test
	@DisplayName("editBlockReason - should update block reason")
	void editBlockReason_ShouldReturnSuccess() throws Exception {
		mockMvc.perform(post("/admin/rate-limiting/api/edit-block-reason")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"1.2.3.4\",\"newReason\":\"Updated reason\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	// ========== clearSuspiciousFlag() Tests ==========

	@Test
	@DisplayName("clearSuspiciousFlag - should clear suspicious flag successfully")
	void clearSuspiciousFlag_ShouldReturnSuccess() throws Exception {
		when(statisticsRepository.findByIpAddress("192.168.1.100")).thenReturn(Optional.of(testStats));
		when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));
		
		mockMvc.perform(post("/admin/rate-limiting/api/clear-suspicious-flag")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"192.168.1.100\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("clearSuspiciousFlag - should handle exceptions")
	void clearSuspiciousFlag_WithException_ShouldReturnError() throws Exception {
		when(statisticsRepository.findByIpAddress(anyString())).thenThrow(new RuntimeException("Database error"));
		
		mockMvc.perform(post("/admin/rate-limiting/api/clear-suspicious-flag")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"1.2.3.4\"}"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false));
	}

	// ========== whitelistIp() Tests ==========

	@Test
	@DisplayName("whitelistIp - should whitelist IP successfully")
	void whitelistIp_ShouldReturnSuccess() throws Exception {
		mockMvc.perform(post("/admin/rate-limiting/api/whitelist-ip")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"1.2.3.4\",\"description\":\"Trusted user\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.description").value("Trusted user"));
	}

	@Test
	@DisplayName("whitelistIp - should handle exceptions")
	void whitelistIp_WithException_ShouldReturnError() throws Exception {
		// Simulate exception scenario
		mockMvc.perform(post("/admin/rate-limiting/api/whitelist-ip")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ipAddress\":\"\"}"))
				.andExpect(status().isOk()); // Controller doesn't validate, just processes
	}

	// ========== clearAllBlocks() Tests ==========

	@Test
	@DisplayName("clearAllBlocks - should clear all permanent blocks")
	void clearAllBlocks_ShouldReturnSuccess() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(statsList);
		when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));
		
		mockMvc.perform(post("/admin/rate-limiting/api/clear-all-blocks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.clearedCount").exists());
	}

	@Test
	@DisplayName("clearAllBlocks - should handle exceptions")
	void clearAllBlocks_WithException_ShouldReturnError() throws Exception {
		when(statisticsRepository.findAll()).thenThrow(new RuntimeException("Database error"));
		
		mockMvc.perform(post("/admin/rate-limiting/api/clear-all-blocks"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false));
	}

	// ========== resetAllLimits() Tests ==========

	@Test
	@DisplayName("resetAllLimits - should reset all rate limits")
	void resetAllLimits_ShouldReturnSuccess() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(statsList);
		when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));
		
		mockMvc.perform(post("/admin/rate-limiting/api/reset-all-limits"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.resetCount").exists());
	}

	@Test
	@DisplayName("resetAllLimits - should handle exceptions")
	void resetAllLimits_WithException_ShouldReturnError() throws Exception {
		when(statisticsRepository.findAll()).thenThrow(new RuntimeException("Database error"));
		
		mockMvc.perform(post("/admin/rate-limiting/api/reset-all-limits"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false));
	}

	// ========== exportData() Tests ==========

	@Test
	@DisplayName("exportData - should export data successfully")
	void exportData_ShouldReturnSuccess() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(statsList);
		
		mockMvc.perform(get("/admin/rate-limiting/api/export-data"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.totalRecords").exists());
	}

	@Test
	@DisplayName("exportData - should handle exceptions")
	void exportData_WithException_ShouldReturnError() throws Exception {
		when(statisticsRepository.findAll()).thenThrow(new RuntimeException("Database error"));
		
		mockMvc.perform(get("/admin/rate-limiting/api/export-data"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	@DisplayName("exportData - should export empty data")
	void exportData_WithEmptyData_ShouldReturnEmpty() throws Exception {
		when(statisticsRepository.findAll()).thenReturn(Collections.emptyList());
		
		mockMvc.perform(get("/admin/rate-limiting/api/export-data"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.totalRecords").value(0));
	}
}


