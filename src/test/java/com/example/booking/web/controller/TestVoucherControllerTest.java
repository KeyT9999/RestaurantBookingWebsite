package com.example.booking.web.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.Voucher;
import com.example.booking.service.VoucherService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TestVoucherController.class)
@AutoConfigureMockMvc(addFilters = false)
class TestVoucherControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VoucherService voucherService;

	@Test
	void listVouchers_shouldRenderTestView() throws Exception {
		when(voucherService.getAllVouchers()).thenReturn(List.<Voucher>of());
		mockMvc.perform(get("/test-vouchers"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attributeExists("vouchers"))
				.andExpect(model().attribute("totalPages", 1));
	}

	@Test
	void simpleTest_shouldRenderTestView() throws Exception {
		mockMvc.perform(get("/test-vouchers/simple"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attribute("message", "Test endpoint working!"));
	}
}


