package com.example.booking.web.controller;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.dto.payout.BankInfoDto;
import com.example.booking.service.VietQRService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankDirectoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class BankDirectoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VietQRService vietQRService;

	@Test
	void listBanks_returnsOk() throws Exception {
		when(vietQRService.listBanks()).thenReturn(Collections.<BankInfoDto>emptyList());
		mockMvc.perform(get("/api/banks"))
				.andExpect(status().isOk());
	}

	@Test
	void getBankByBin_notFound_returns404() throws Exception {
		when(vietQRService.getBankByBin(anyString())).thenReturn(Optional.empty());
		mockMvc.perform(get("/api/banks/970422"))
				.andExpect(status().isNotFound());
	}

	@Test
	void lookupAccount_success_returnsOk() throws Exception {
		when(vietQRService.lookupAccountName(anyString(), anyString())).thenReturn(Optional.of("John Doe"));
		mockMvc.perform(post("/api/banks/lookup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"bin\":\"970422\", \"accountNumber\":\"123456\"}"))
				.andExpect(status().isOk());
	}
}


