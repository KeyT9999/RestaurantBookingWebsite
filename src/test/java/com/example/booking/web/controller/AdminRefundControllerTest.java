package com.example.booking.web.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.RefundRequest;
import com.example.booking.domain.Payment;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.service.RefundService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRefundController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminRefundControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RefundService refundService;

	@Test
	void getPendingRefunds_returnsSuccessJson() throws Exception {
		RefundRequest req = new RefundRequest();
		req.setRefundRequestId(1);
		Payment payment = new Payment();
		payment.setPaymentId(100);
		req.setPayment(payment);
		Customer customer = new Customer();
		customer.setCustomerId(UUID.randomUUID());
		req.setCustomer(customer);
		RestaurantProfile rest = new RestaurantProfile();
		rest.setRestaurantId(5);
		req.setRestaurant(rest);
		when(refundService.getPendingRefunds()).thenReturn(List.of(req));

		mockMvc.perform(get("/admin/refunds/pending"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.count").value(1));
	}

	@Test
	void rejectRefund_returnsSuccessJson() throws Exception {
		doNothing().when(refundService).rejectRefund(anyInt(), any(UUID.class), any());
		mockMvc.perform(post("/admin/refunds/10/reject").param("rejectReason", "bad"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void completeRefund_returnsSuccessJson() throws Exception {
		doNothing().when(refundService).completeRefund(anyInt(), any(UUID.class), any(), any());
		mockMvc.perform(post("/admin/refunds/10/complete")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"adminNote\":\"ok\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void generateVietQR_returnsSuccessJson() throws Exception {
		when(refundService.generateVietQRForRefund(anyInt())).thenReturn("http://vietqr");
		mockMvc.perform(post("/admin/refunds/11/generate-vietqr"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.vietqrUrl").value("http://vietqr"));
	}
}


