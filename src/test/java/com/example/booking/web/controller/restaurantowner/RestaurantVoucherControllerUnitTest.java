package com.example.booking.web.controller.restaurantowner;

import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class RestaurantVoucherControllerUnitTest {

	@Test
	void debugVouchers_shouldReturnListView() {
		VoucherService voucherService = Mockito.mock(VoucherService.class);
		when(voucherService.getVouchersByRestaurant(anyInt())).thenReturn(List.of());

		RestaurantVoucherController ctrl = new RestaurantVoucherController();
		inject(ctrl, "voucherService", voucherService);
		inject(ctrl, "restaurantOwnerService", Mockito.mock(RestaurantOwnerService.class));
		inject(ctrl, "userService", Mockito.mock(SimpleUserService.class));

		Model model = new ConcurrentModel();
		String view = ctrl.debugVouchers(model);
		assertEquals("restaurant-owner/vouchers/list", view);
		assertTrue(model.containsAttribute("vouchers"));
	}

	@Test
	void listVouchers_noRestaurants_shouldReturnErrorView() {
		RestaurantOwnerService ownerService = Mockito.mock(RestaurantOwnerService.class);
		when(ownerService.getRestaurantsByCurrentUser(Mockito.any())).thenReturn(List.of());

		RestaurantVoucherController ctrl = new RestaurantVoucherController();
		inject(ctrl, "restaurantOwnerService", ownerService);
		inject(ctrl, "voucherService", Mockito.mock(VoucherService.class));
		inject(ctrl, "userService", Mockito.mock(SimpleUserService.class));

		Model model = new ConcurrentModel();
		String view = ctrl.listVouchers(0, 10, "createdAt", "desc", null, null, null, null, model);
		assertEquals("restaurant-owner/vouchers/list", view);
		assertTrue(model.containsAttribute("errorMessage"));
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


