package com.example.booking.web.controller.admin;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.web.controller.SimpleAdminController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SimpleAdminControllerUnitTest {

	@Test
	void simpleRestaurantList_shouldReturnView() {
		RestaurantProfileRepository repo = Mockito.mock(RestaurantProfileRepository.class);
		RestaurantProfile p = new RestaurantProfile();
		p.setRestaurantId(1);
		p.setRestaurantName("R1");
		p.setApprovalStatus(RestaurantApprovalStatus.PENDING);
		when(repo.findAll()).thenReturn(List.of(p));

		SimpleAdminController ctrl = new SimpleAdminController();
		try {
			var f = SimpleAdminController.class.getDeclaredField("restaurantProfileRepository");
			f.setAccessible(true);
			f.set(ctrl, repo);
		} catch (Exception e) { throw new RuntimeException(e); }

		Model model = new ConcurrentModel();
		String view = ctrl.simpleRestaurantList(model);
		assertEquals("admin/simple-restaurant-list", view);
	}
}


