package com.example.booking.web.controller.api;

import com.example.booking.service.TableStatusManagementService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableStatusApiControllerUnitTest {

	@Test
	void checkInCustomer_shouldReturnOk() {
		TableStatusManagementService svc = Mockito.mock(TableStatusManagementService.class);
		TableStatusApiController ctrl = new TableStatusApiController();
		inject(ctrl, "tableStatusService", svc);
		var resp = ctrl.checkInCustomer(10);
        assertEquals(200, resp.getStatusCode().value());
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			var f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (Exception e) { throw new RuntimeException(e); }
	}
}


