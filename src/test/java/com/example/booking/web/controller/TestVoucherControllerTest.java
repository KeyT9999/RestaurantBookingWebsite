package com.example.booking.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
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

	@MockBean
	private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

	@MockBean
	private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

	private Voucher testVoucher;

	@BeforeEach
	void setUp() {
		testVoucher = new Voucher();
		testVoucher.setVoucherId(1);
		testVoucher.setCode("TEST2024");
		testVoucher.setDescription("Test voucher");
		testVoucher.setStatus(VoucherStatus.ACTIVE);
	}

	// ========== listVouchers() Tests ==========

	@Test
	@DisplayName("listVouchers - should render test view with empty list")
	void listVouchers_WithEmptyList_ShouldRenderTestView() throws Exception {
		when(voucherService.getAllVouchers()).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/test-vouchers"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attributeExists("vouchers"))
				.andExpect(model().attribute("totalPages", 1))
				.andExpect(model().attribute("totalVouchers", 0))
				.andExpect(model().attribute("currentPage", 0));
	}

	@Test
	@DisplayName("listVouchers - should render test view with vouchers")
	void listVouchers_WithVouchers_ShouldRenderTestView() throws Exception {
		List<Voucher> vouchers = Arrays.asList(testVoucher);
		when(voucherService.getAllVouchers()).thenReturn(vouchers);
		mockMvc.perform(get("/test-vouchers"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attributeExists("vouchers"))
				.andExpect(model().attribute("totalVouchers", 1))
				.andExpect(model().attribute("search", ""))
				.andExpect(model().attribute("status", ""))
				.andExpect(model().attribute("sortBy", "createdAt"))
				.andExpect(model().attribute("sortDir", "desc"));
	}

	@Test
	@DisplayName("listVouchers - should handle exceptions gracefully")
	void listVouchers_WithException_ShouldRenderTestView() throws Exception {
		when(voucherService.getAllVouchers()).thenThrow(new RuntimeException("Database error"));
		mockMvc.perform(get("/test-vouchers"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attributeExists("message"));
	}

	// ========== simpleTest() Tests ==========

	@Test
	@DisplayName("simpleTest - should render test view with message")
	void simpleTest_ShouldRenderTestView() throws Exception {
		mockMvc.perform(get("/test-vouchers/simple"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attribute("message", "Test endpoint working!"));
	}

	// ========== debugVouchers() Tests ==========

	@Test
	@DisplayName("debugVouchers - should render debug view with vouchers")
	void debugVouchers_WithVouchers_ShouldRenderDebugView() throws Exception {
		List<Voucher> vouchers = Arrays.asList(testVoucher);
		when(voucherService.getAllVouchers()).thenReturn(vouchers);
		mockMvc.perform(get("/test-vouchers/debug"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attributeExists("vouchers", "voucherCount", "message"))
				.andExpect(model().attribute("voucherCount", 1))
				.andExpect(model().attribute("vouchers", vouchers));
	}

	@Test
	@DisplayName("debugVouchers - should handle empty list")
	void debugVouchers_WithEmptyList_ShouldRenderDebugView() throws Exception {
		when(voucherService.getAllVouchers()).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/test-vouchers/debug"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("voucherCount", 0))
				.andExpect(model().attributeExists("message"));
	}

	@Test
	@DisplayName("debugVouchers - should handle exceptions gracefully")
	void debugVouchers_WithException_ShouldRenderDebugView() throws Exception {
		when(voucherService.getAllVouchers()).thenThrow(new RuntimeException("Database error"));
		mockMvc.perform(get("/test-vouchers/debug"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attributeExists("message"));
	}

	// ========== adminView() Tests ==========

	@Test
	@DisplayName("adminView - should render admin view with vouchers")
	void adminView_WithVouchers_ShouldRenderAdminView() throws Exception {
		List<Voucher> vouchers = Arrays.asList(testVoucher);
		when(voucherService.getAllVouchers()).thenReturn(vouchers);
		mockMvc.perform(get("/test-vouchers/admin-view"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attributeExists("vouchers"))
				.andExpect(model().attribute("totalVouchers", 1))
				.andExpect(model().attribute("totalPages", 1))
				.andExpect(model().attribute("currentPage", 0));
	}

	@Test
	@DisplayName("adminView - should handle empty list")
	void adminView_WithEmptyList_ShouldRenderAdminView() throws Exception {
		when(voucherService.getAllVouchers()).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/test-vouchers/admin-view"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("totalVouchers", 0));
	}

	@Test
	@DisplayName("adminView - should handle exceptions gracefully")
	void adminView_WithException_ShouldRenderAdminView() throws Exception {
		when(voucherService.getAllVouchers()).thenThrow(new RuntimeException("Database error"));
		mockMvc.perform(get("/test-vouchers/admin-view"))
				.andExpect(status().isOk())
				.andExpect(view().name("test"))
				.andExpect(model().attributeExists("message"));
	}
}


