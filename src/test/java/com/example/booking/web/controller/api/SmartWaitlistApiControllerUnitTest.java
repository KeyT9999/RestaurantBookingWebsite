package com.example.booking.web.controller.api;

import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.service.CustomerService;
import com.example.booking.service.SmartWaitlistService;
import com.example.booking.service.WaitlistService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class SmartWaitlistApiControllerUnitTest {

	@Test
	void checkAvailability_general_shouldReturnOk() {
		SmartWaitlistService svc = Mockito.mock(SmartWaitlistService.class);
		when(svc.checkGeneralAvailability(anyInt(), any(), anyInt()))
				.thenReturn(new AvailabilityCheckResponse(false, null));

		SmartWaitlistApiController ctrl = new SmartWaitlistApiController();
		inject(ctrl, "smartWaitlistService", svc);
		inject(ctrl, "waitlistService", Mockito.mock(WaitlistService.class));
		inject(ctrl, "customerService", Mockito.mock(CustomerService.class));

		var resp = ctrl.checkAvailability(10, "2024-12-25T19:00", 2, null);
		assertEquals(200, resp.getStatusCode().value());
		var body = resp.getBody();
		assertNotNull(body);
		assertFalse(body.isHasConflict());
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			var f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (Exception e) { throw new RuntimeException(e); }
	}
}

