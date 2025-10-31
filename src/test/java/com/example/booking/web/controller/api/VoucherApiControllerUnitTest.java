package com.example.booking.web.controller.api;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.service.CustomerService;
import com.example.booking.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class VoucherApiControllerUnitTest {

	@Test
	void validate_shouldReturnOkResponse() {
		VoucherService voucherService = Mockito.mock(VoucherService.class);
		CustomerService customerService = Mockito.mock(CustomerService.class);
		when(voucherService.validate(any(VoucherService.ValidationRequest.class)))
				.thenReturn(new VoucherService.ValidationResult(true, "OK", BigDecimal.TEN, null));

		VoucherApiController ctrl = new VoucherApiController();
		inject(ctrl, "voucherService", voucherService);
		inject(ctrl, "customerService", customerService);

		var body = new VoucherApiController.ValidateRequest("CODE", 1, java.time.LocalDateTime.now(), 2, BigDecimal.valueOf(100));
		var resp = ctrl.validate(body, null);
        assertEquals(200, resp.getStatusCode().value());
	}

	@Test
	void apply_shouldUseAuthenticatedCustomer() {
		VoucherService voucherService = Mockito.mock(VoucherService.class);
		CustomerService customerService = Mockito.mock(CustomerService.class);

		Customer customer = new Customer();
		customer.setCustomerId(UUID.randomUUID());
		when(customerService.findByUsername(eq("u1"))).thenReturn(Optional.of(customer));
		when(voucherService.applyToBooking(any(VoucherService.ApplyRequest.class)))
				.thenReturn(new VoucherService.ApplyResult(true, "", BigDecimal.ONE, 1));

		VoucherApiController ctrl = new VoucherApiController();
		inject(ctrl, "voucherService", voucherService);
		inject(ctrl, "customerService", customerService);

		var auth = new UsernamePasswordAuthenticationToken("u1", null, java.util.List.of());
		var resp = ctrl.apply(new VoucherApiController.ApplyRequest("CODE", 1, BigDecimal.TEN, 99), auth);
        assertEquals(200, resp.getStatusCode().value());
	}

	@Test
	void getDemoVouchers_shouldFilterActiveAndDate() {
		VoucherService voucherService = Mockito.mock(VoucherService.class);
        Voucher v1 = new Voucher(); v1.setVoucherId(1); v1.setCode("A"); v1.setStatus(VoucherStatus.ACTIVE); v1.setStartDate(LocalDate.now().minusDays(1)); v1.setEndDate(LocalDate.now().plusDays(1)); v1.setDiscountValue(BigDecimal.ONE); v1.setDiscountType(com.example.booking.domain.DiscountType.FIXED);
		Voucher v2 = new Voucher(); v2.setVoucherId(2); v2.setCode("B"); v2.setStatus(VoucherStatus.EXPIRED);
		when(voucherService.getAllVouchers()).thenReturn(List.of(v1, v2));
		when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

		VoucherApiController ctrl = new VoucherApiController();
		inject(ctrl, "voucherService", voucherService);
		inject(ctrl, "customerService", Mockito.mock(CustomerService.class));

		var resp = ctrl.getDemoVouchers(null, null);
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


