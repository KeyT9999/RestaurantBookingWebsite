package com.example.booking.web.controller.api;

import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class RestaurantWithdrawalApiControllerUnitTest {

	private Principal principal(String username) {
		return () -> username;
	}

	@Test
	void getBalance_shouldReturnOk() {
		RestaurantBalanceService bal = Mockito.mock(RestaurantBalanceService.class);
		RestaurantBankAccountService bank = Mockito.mock(RestaurantBankAccountService.class);
		WithdrawalService w = Mockito.mock(WithdrawalService.class);
		RestaurantProfileRepository repo = Mockito.mock(RestaurantProfileRepository.class);
        com.example.booking.domain.RestaurantProfile rp = new com.example.booking.domain.RestaurantProfile();
        rp.setRestaurantId(1);
        when(repo.findByOwnerUsername(eq("owner"))).thenReturn(java.util.List.of(rp));
		when(bal.getBalance(anyInt())).thenReturn(new RestaurantBalanceDto());

		RestaurantWithdrawalApiController ctrl = new RestaurantWithdrawalApiController(bal, bank, w, repo);
		var resp = ctrl.getBalance(principal("owner"));
        assertEquals(200, resp.getStatusCode().value());
        var body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
	}
}


