package com.example.booking.web.controller.api;

import com.example.booking.service.RateLimitingMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RateLimitingApiController.class)
class RateLimitingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateLimitingMonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        // Setup default mocks
        RateLimitingMonitoringService.IpStatistics defaultStats = new RateLimitingMonitoringService.IpStatistics();
        when(monitoringService.isIpBlocked(anyString(), anyString())).thenReturn(false);
        when(monitoringService.getBucketInfo(anyString())).thenReturn(new HashMap<>());
        when(monitoringService.getIpStatistics(anyString())).thenReturn(defaultStats);
        when(monitoringService.getBlockedIps()).thenReturn(Arrays.asList("192.168.1.100"));
        when(monitoringService.getTopBlockedIps(anyInt())).thenReturn(Arrays.asList(
                Map.entry("192.168.1.100", defaultStats)
        ));
        Map<String, RateLimitingMonitoringService.IpStatistics> allStats = new HashMap<>();
        allStats.put("192.168.1.100", defaultStats);
        when(monitoringService.getAllIpStatistics()).thenReturn(allStats);
    }

    @Test
    void shouldCheckIpStatus() throws Exception {
        Map<String, Object> bucketInfo = new HashMap<>();
        bucketInfo.put("capacity", 100);
        bucketInfo.put("availableTokens", 50);

        RateLimitingMonitoringService.IpStatistics statistics = new RateLimitingMonitoringService.IpStatistics();

        when(monitoringService.isIpBlocked("192.168.1.1", "general")).thenReturn(false);
        when(monitoringService.getBucketInfo("192.168.1.1")).thenReturn(bucketInfo);
        when(monitoringService.getIpStatistics("192.168.1.1")).thenReturn(statistics);

        mockMvc.perform(get("/api/rate-limiting/check/192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ip").value("192.168.1.1"))
                .andExpect(jsonPath("$.isBlocked").value(false))
                .andExpect(jsonPath("$.bucketInfo").exists())
                .andExpect(jsonPath("$.statistics").exists());
    }

    @Test
    void shouldCheckBlockedIpStatus() throws Exception {
        when(monitoringService.isIpBlocked("192.168.1.100", "general")).thenReturn(true);

        mockMvc.perform(get("/api/rate-limiting/check/192.168.1.100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ip").value("192.168.1.100"))
                .andExpect(jsonPath("$.isBlocked").value(true));
    }

    @Test
    void shouldGetBlockedIps() throws Exception {
        List<String> blockedIps = Arrays.asList("192.168.1.100", "192.168.1.101");
        RateLimitingMonitoringService.IpStatistics stats = new RateLimitingMonitoringService.IpStatistics();
        when(monitoringService.getBlockedIps()).thenReturn(blockedIps);
        when(monitoringService.getTopBlockedIps(10)).thenReturn(Arrays.asList(
                Map.entry("192.168.1.100", stats),
                Map.entry("192.168.1.101", stats)
        ));

        mockMvc.perform(get("/api/rate-limiting/blocked-ips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockedIps").isArray())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.topBlockedIps").isArray());
    }

    @Test
    void shouldResetRateLimit() throws Exception {
        doNothing().when(monitoringService).resetRateLimitForIp("192.168.1.1");

        mockMvc.perform(post("/api/rate-limiting/reset/192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rate limit đã được reset cho IP: 192.168.1.1"))
                .andExpect(jsonPath("$.status").value("success"));

        verify(monitoringService).resetRateLimitForIp("192.168.1.1");
    }

    @Test
    void shouldGetStatistics() throws Exception {
        RateLimitingMonitoringService.IpStatistics stats = new RateLimitingMonitoringService.IpStatistics();
        Map<String, RateLimitingMonitoringService.IpStatistics> allStatistics = new HashMap<>();
        allStatistics.put("192.168.1.1", stats);
        when(monitoringService.getAllIpStatistics()).thenReturn(allStatistics);
        when(monitoringService.getTopBlockedIps(5)).thenReturn(Arrays.asList(
                Map.entry("192.168.1.100", stats)
        ));

        mockMvc.perform(get("/api/rate-limiting/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBlockedIps").exists())
                .andExpect(jsonPath("$.topBlockedIps").isArray())
                .andExpect(jsonPath("$.allStatistics").exists());
    }

    @Test
    void shouldHandleEmptyBlockedIps() throws Exception {
        when(monitoringService.getBlockedIps()).thenReturn(Collections.emptyList());
        when(monitoringService.getTopBlockedIps(10)).thenReturn(Collections.emptyList());
        when(monitoringService.getAllIpStatistics()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/rate-limiting/blocked-ips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0));
    }
}

