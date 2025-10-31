package com.example.booking.web.controller.api;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.AIActionRequest;
import com.example.booking.dto.AIActionResponse;
import com.example.booking.service.AIIntentDispatcherService;
import com.example.booking.service.SimpleUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AIActionsControllerUnitTest {

	@Test
	void executeAIAction_shouldReturnOk() {
		AIIntentDispatcherService svc = Mockito.mock(AIIntentDispatcherService.class);
		when(svc.dispatchIntent(anyString(), any(), any(User.class)))
				.thenReturn(AIActionResponse.success("done", Map.of("id", 1)));

		AIActionsController ctrl = new AIActionsController();
		inject(ctrl, "intentDispatcherService", svc);
		inject(ctrl, "userService", Mockito.mock(SimpleUserService.class));

		User user = new User("u1", "u1@example.com", "password123", "User");
		user.setId(UUID.randomUUID());
		user.setRole(UserRole.CUSTOMER);
		var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

		AIActionRequest req = new AIActionRequest();
		req.setIntent("BOOK_TABLE");
		req.setData(Map.of("restaurantId", 1));

		var resp = ctrl.executeAIAction(req, auth);
		assertEquals(200, resp.getStatusCode().value());
		var body = resp.getBody();
		assertNotNull(body);
		assertTrue(body.isSuccess());
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			var f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (Exception e) { throw new RuntimeException(e); }
	}
}

