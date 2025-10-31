package com.example.booking.web.controller.customer;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.ConcurrentModel;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class FavoriteControllerUnitTest {

	@Test
	void favoritesPage_shouldPopulateModel() {
		FavoriteService svc = Mockito.mock(FavoriteService.class);
		CustomerRepository repo = Mockito.mock(CustomerRepository.class);

		User user = new User("u", "u@example.com", "pwd", "User");
		user.setId(UUID.randomUUID());
		user.setRole(UserRole.CUSTOMER);

		Customer cust = new Customer();
		cust.setCustomerId(UUID.randomUUID());
		cust.setUser(user);

		when(repo.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cust));
		when(svc.getFavoriteRestaurantsWithFilters(any(UUID.class), any(), isNull(), isNull(), isNull(), isNull()))
				.thenReturn(new PageImpl<>(java.util.List.of()));

		FavoriteController ctrl = new FavoriteController();
		inject(ctrl, "favoriteService", svc);
		inject(ctrl, "customerRepository", repo);

		var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		var model = new ConcurrentModel();
		String view = ctrl.favoritesPage(0, 12, "createdAt", "desc", null, null, null, null, null, null, null, null, auth, model);

		assertEquals("customer/favorites-advanced", view);
		assertTrue(model.containsAttribute("favorites"));
	}

	@Test
	void toggleFavorite_shouldReturnOk() {
		FavoriteService svc = Mockito.mock(FavoriteService.class);
		CustomerRepository repo = Mockito.mock(CustomerRepository.class);

		User user = new User("u2", "u2@example.com", "pwd", "User2");
		user.setId(UUID.randomUUID());
		user.setRole(UserRole.CUSTOMER);

		Customer cust = new Customer();
		cust.setCustomerId(UUID.randomUUID());
		cust.setUser(user);

		when(repo.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cust));
		when(svc.toggleFavorite(any(UUID.class), any(ToggleFavoriteRequest.class)))
				.thenReturn(ToggleFavoriteResponse.success(true, 5, 10));

		FavoriteController ctrl = new FavoriteController();
		inject(ctrl, "favoriteService", svc);
		inject(ctrl, "customerRepository", repo);

		var req = new ToggleFavoriteRequest();
		req.setRestaurantId(10);

		var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		var resp = ctrl.toggleFavorite(req, auth);
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

