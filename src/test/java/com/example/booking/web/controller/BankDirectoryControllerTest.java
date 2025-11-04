package com.example.booking.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankDirectoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class BankDirectoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VietQRService vietQRService;

	private BankInfoDto testBank;

	@BeforeEach
	void setUp() {
		testBank = new BankInfoDto();
		testBank.setBin("970422");
		testBank.setName("Vietcombank");
		testBank.setShortName("VCB");
	}

	// ========== listBanks() Tests ==========

	@Test
	@DisplayName("listBanks - should return banks list successfully")
	void listBanks_ShouldReturnOk() throws Exception {
		when(vietQRService.listBanks()).thenReturn(Arrays.asList(testBank));
		mockMvc.perform(get("/api/banks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@DisplayName("listBanks - should return empty list")
	void listBanks_WithEmptyList_ShouldReturnOk() throws Exception {
		when(vietQRService.listBanks()).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/api/banks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@DisplayName("listBanks - should handle exceptions gracefully")
	void listBanks_WithException_ShouldReturnError() throws Exception {
		when(vietQRService.listBanks()).thenThrow(new RuntimeException("Service unavailable"));
		mockMvc.perform(get("/api/banks"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").exists());
	}

	// ========== getBankByBin() Tests ==========

	@Test
	@DisplayName("getBankByBin - should return bank when found")
	void getBankByBin_WhenFound_ShouldReturnOk() throws Exception {
		when(vietQRService.getBankByBin("970422")).thenReturn(Optional.of(testBank));
		mockMvc.perform(get("/api/banks/970422"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.bin").value("970422"))
				.andExpect(jsonPath("$.data.name").value("Vietcombank"));
	}

	@Test
	@DisplayName("getBankByBin - should return 404 when not found")
	void getBankByBin_WhenNotFound_ShouldReturn404() throws Exception {
		when(vietQRService.getBankByBin(anyString())).thenReturn(Optional.empty());
		mockMvc.perform(get("/api/banks/999999"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("getBankByBin - should handle exceptions gracefully")
	void getBankByBin_WithException_ShouldReturnError() throws Exception {
		when(vietQRService.getBankByBin(anyString())).thenThrow(new RuntimeException("Database error"));
		mockMvc.perform(get("/api/banks/970422"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	// ========== lookupAccount() Tests ==========

	@Test
	@DisplayName("lookupAccount - should return account name when found")
	void lookupAccount_WhenFound_ShouldReturnOk() throws Exception {
		when(vietQRService.lookupAccountName("970422", "1234567890"))
				.thenReturn(Optional.of("John Doe"));
		mockMvc.perform(post("/api/banks/lookup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"bin\":\"970422\", \"accountNumber\":\"1234567890\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").value("John Doe"));
	}

	@Test
	@DisplayName("lookupAccount - should return error when account not found")
	void lookupAccount_WhenNotFound_ShouldReturnError() throws Exception {
		when(vietQRService.lookupAccountName(anyString(), anyString()))
				.thenReturn(Optional.empty());
		mockMvc.perform(post("/api/banks/lookup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"bin\":\"970422\", \"accountNumber\":\"9999999999\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("lookupAccount - should handle invalid request body")
	void lookupAccount_WithInvalidBody_ShouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/banks/lookup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"bin\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("lookupAccount - should handle exceptions gracefully")
	void lookupAccount_WithException_ShouldReturnError() throws Exception {
		when(vietQRService.lookupAccountName(anyString(), anyString()))
				.thenThrow(new RuntimeException("Service error"));
		mockMvc.perform(post("/api/banks/lookup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"bin\":\"970422\", \"accountNumber\":\"1234567890\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").exists());
	}

	// ========== syncBanks() Tests ==========

	@Test
	@DisplayName("syncBanks - should sync banks successfully")
	void syncBanks_ShouldSyncSuccessfully() throws Exception {
		doNothing().when(vietQRService).syncBanksFromVietQR();
		mockMvc.perform(post("/api/banks/sync"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Đồng bộ danh sách ngân hàng thành công"));
	}

	@Test
	@DisplayName("syncBanks - should handle exceptions gracefully")
	void syncBanks_WithException_ShouldReturnError() throws Exception {
		doThrow(new RuntimeException("Sync failed")).when(vietQRService).syncBanksFromVietQR();
		mockMvc.perform(post("/api/banks/sync"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").exists());
	}
}


