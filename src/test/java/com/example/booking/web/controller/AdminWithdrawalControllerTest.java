package com.example.booking.web.controller;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.dto.admin.WithdrawalStatsDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.service.WithdrawalService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminWithdrawalController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminWithdrawalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private WithdrawalService withdrawalService;

	@Test
	void getWithdrawalManagement_shouldRenderView() throws Exception {
		when(withdrawalService.getWithdrawalStats()).thenReturn(new WithdrawalStatsDto());
		when(withdrawalService.getAllWithdrawals(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.<WithdrawalRequestDto>emptyList()));
		mockMvc.perform(get("/admin/withdrawal"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/withdrawal-management"));
	}

	@Test
	void approveWithdrawal_shouldRedirectWithFlash() throws Exception {
		doNothing().when(withdrawalService).approveWithdrawal(anyInt(), any(), any());
		mockMvc.perform(post("/admin/withdrawal/1/approve"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/withdrawal?status=PENDING"))
				.andExpect(flash().attributeExists("success"));
	}
}


