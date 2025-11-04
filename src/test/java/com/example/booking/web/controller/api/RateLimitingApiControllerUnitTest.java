package com.example.booking.web.controller.api;

import com.example.booking.service.RateLimitingMonitoringService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RateLimitingApiControllerUnitTest {

	@Test
	void checkIpStatus_shouldReturnOkMap() {
		RateLimitingMonitoringService svc = Mockito.mock(RateLimitingMonitoringService.class);
		when(svc.isIpBlocked(eq("1.2.3.4"), eq("general"))).thenReturn(true);
        when(svc.getBucketInfo(eq("1.2.3.4"))).thenReturn(Map.of("capacity", 10));
        when(svc.getIpStatistics(eq("1.2.3.4"))).thenReturn(new com.example.booking.service.RateLimitingMonitoringService.IpStatistics());

		RateLimitingApiController ctrl = new RateLimitingApiController();
		inject(ctrl, "monitoringService", svc);

		var resp = ctrl.checkIpStatus("1.2.3.4");
        assertEquals(200, resp.getStatusCode().value());
        var body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("ip"));
	}

	@Test
	void getBlockedIps_shouldReturnCounts() {
		RateLimitingMonitoringService svc = Mockito.mock(RateLimitingMonitoringService.class);
        when(svc.getBlockedIps()).thenReturn(List.of("a", "b"));
        when(svc.getTopBlockedIps(anyInt())).thenReturn(List.of(
                Map.entry("a", new com.example.booking.service.RateLimitingMonitoringService.IpStatistics())
        ));

		RateLimitingApiController ctrl = new RateLimitingApiController();
		inject(ctrl, "monitoringService", svc);

		var resp = ctrl.getBlockedIps();
        assertEquals(200, resp.getStatusCode().value());
        var body2 = resp.getBody();
        assertNotNull(body2);
        Object total = body2.get("totalCount");
        assertNotNull(total);
        assertEquals(2, ((Number) total).intValue());
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			var f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (Exception e) { throw new RuntimeException(e); }
	}
}


