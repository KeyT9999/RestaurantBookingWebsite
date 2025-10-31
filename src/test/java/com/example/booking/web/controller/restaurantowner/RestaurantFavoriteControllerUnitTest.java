package com.example.booking.web.controller.restaurantowner;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RestaurantFavoriteControllerUnitTest {

	@Test
	void favoriteStatistics_shouldPopulateModel_whenOwnerFound() {
		FavoriteService favoriteService = Mockito.mock(FavoriteService.class);
		RestaurantOwnerService ownerService = Mockito.mock(RestaurantOwnerService.class);
		SimpleUserService userService = Mockito.mock(SimpleUserService.class);

		User domainUser = new User("owner", "o@example.com", "password123", "Owner");
		domainUser.setId(UUID.randomUUID());
		when(userService.loadUserByUsername(eq("o@example.com"))).thenReturn((UserDetails) domainUser);

		RestaurantOwner owner = new RestaurantOwner();
		owner.setOwnerId(UUID.randomUUID());
		owner.setUser(domainUser);
		when(ownerService.getRestaurantOwnerByUserId(eq(domainUser.getId()))).thenReturn(Optional.of(owner));

		when(favoriteService.getFavoriteStatisticsForOwner(eq(owner.getOwnerId()), any(PageRequest.class)))
				.thenReturn(List.of(new FavoriteStatisticsDto()));

		RestaurantFavoriteController ctrl = new RestaurantFavoriteController();
		inject(ctrl, "favoriteService", favoriteService);
		inject(ctrl, "restaurantOwnerService", ownerService);
		inject(ctrl, "userService", userService);

		Model model = new ConcurrentModel();
		var auth = new UsernamePasswordAuthenticationToken("o@example.com", null);
		String view = ctrl.favoriteStatistics(0, 20, auth, model);
		assertEquals("restaurant-owner/favorite-statistics", view);
		assertTrue(model.containsAttribute("statistics"));
	}

	@Test
	void testRestaurantOwnerStats_shouldReturnBadRequest_whenOwnerMissing() {
		RestaurantFavoriteController ctrl = new RestaurantFavoriteController();
		inject(ctrl, "favoriteService", Mockito.mock(FavoriteService.class));
		inject(ctrl, "restaurantOwnerService", Mockito.mock(RestaurantOwnerService.class));
		SimpleUserService userService = Mockito.mock(SimpleUserService.class);
		when(userService.loadUserByUsername(eq("missing@example.com"))).thenReturn(null);
		inject(ctrl, "userService", userService);

		var auth = new UsernamePasswordAuthenticationToken("missing@example.com", null);
		var resp = ctrl.testRestaurantOwnerStats(auth);
		assertEquals(400, resp.getStatusCodeValue());
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			var f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}


