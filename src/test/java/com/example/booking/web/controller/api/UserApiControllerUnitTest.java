package com.example.booking.web.controller.api;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.SimpleUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class UserApiControllerUnitTest {

	@Test
	void getCurrentUser_regular_shouldReturnInfo() {
		UserApiController ctrl = new UserApiController();
		inject(ctrl, "userService", Mockito.mock(SimpleUserService.class));

		User user = new User("u1", "u1@example.com", "password123", "Name");
		user.setId(UUID.randomUUID());
		user.setRole(UserRole.CUSTOMER);
		var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

		var resp = ctrl.getCurrentUser(auth);
        assertEquals(200, resp.getStatusCode().value());
	}

	@Test
	void getCurrentUser_oauth_shouldLoadFromService() {
		SimpleUserService userService = Mockito.mock(SimpleUserService.class);
		UserApiController ctrl = new UserApiController();
		inject(ctrl, "userService", userService);

		OAuth2User oauth = new OAuth2User() {
			@Override public Map<String, Object> getAttributes() { return Map.of("email", "x@example.com"); }
			@Override public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() { return java.util.List.of(); }
			@Override public String getName() { return "x@example.com"; }
		};
		var auth = new UsernamePasswordAuthenticationToken(oauth, null, java.util.List.of());

		User user = new User("u2", "x@example.com", "password123", "X");
		user.setId(UUID.randomUUID());
		user.setRole(UserRole.ADMIN);
		when(userService.loadUserByUsername(eq("x@example.com"))).thenReturn(user);

		var resp = ctrl.getCurrentUser(auth);
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


