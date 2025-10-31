package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("RestaurantWithdrawalViewController Tests")
class RestaurantWithdrawalViewControllerTest {

	private MockMvc mockMvc;

	@Mock
	private RestaurantBalanceService balanceService;

	@Mock
	private RestaurantBankAccountService bankAccountService;

	@Mock
	private WithdrawalService withdrawalService;

	@Mock
	private RestaurantProfileRepository restaurantRepository;

	@Mock
	private Principal principal;

	@Mock
	private Model model;

	@InjectMocks
	private RestaurantWithdrawalViewController controller;

	private RestaurantProfile testRestaurant;
	private RestaurantBalanceDto testBalance;
	private RestaurantBankAccountDto testBankAccount;
	private WithdrawalRequestDto testWithdrawal;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		testRestaurant = new RestaurantProfile();
		testRestaurant.setRestaurantId(1);
		testRestaurant.setRestaurantName("Test Restaurant");

		testBalance = new RestaurantBalanceDto();
		testBalance.setAvailableBalance(new BigDecimal("1000000"));
		testBalance.setTotalRevenue(new BigDecimal("5000000"));
		testBalance.setTotalWithdrawn(new BigDecimal("2000000"));

		testBankAccount = new RestaurantBankAccountDto();
		testBankAccount.setAccountId(1);
		testBankAccount.setBankCode("VCB");
		testBankAccount.setAccountNumber("1234567890");

		testWithdrawal = new WithdrawalRequestDto();
		testWithdrawal.setRequestId(1);
		testWithdrawal.setRestaurantId(1);
		testWithdrawal.setAmount(new BigDecimal("500000"));
		testWithdrawal.setStatus(WithdrawalStatus.PENDING);

		when(principal.getName()).thenReturn("owner@example.com");
		when(restaurantRepository.findByOwnerUsername("owner@example.com"))
				.thenReturn(Arrays.asList(testRestaurant));
	}

	// ========== withdrawalManagement() Tests ==========

	@Test
	@DisplayName("withdrawalManagement - should render withdrawal page")
	void withdrawalManagement_ShouldRenderPage() throws Exception {
		when(restaurantRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
		when(balanceService.getBalance(1)).thenReturn(testBalance);
		when(bankAccountService.getBankAccounts(1)).thenReturn(Arrays.asList(testBankAccount));
		when(withdrawalService.getAllWithdrawals(any())).thenReturn(
				new org.springframework.data.domain.PageImpl<>(Arrays.asList(testWithdrawal)));

		mockMvc.perform(get("/restaurant-owner/withdrawal")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurant-owner/withdrawal-management"))
				.andExpect(model().attributeExists("balance", "bankAccounts", "withdrawals"));
	}

	@Test
	@DisplayName("withdrawalManagement - should filter by status")
	void withdrawalManagement_WithStatusFilter_ShouldFilter() throws Exception {
		when(restaurantRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
		when(balanceService.getBalance(1)).thenReturn(testBalance);
		when(bankAccountService.getBankAccounts(1)).thenReturn(Collections.emptyList());
		when(withdrawalService.getWithdrawalsByStatus(WithdrawalStatus.PENDING))
				.thenReturn(Arrays.asList(testWithdrawal));

		mockMvc.perform(get("/restaurant-owner/withdrawal")
				.param("status", "PENDING")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(model().attribute("filter", "PENDING"));
	}

	@Test
	@DisplayName("withdrawalManagement - should handle exceptions gracefully")
	void withdrawalManagement_WithException_ShouldRenderWithEmptyData() throws Exception {
		when(restaurantRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

		mockMvc.perform(get("/restaurant-owner/withdrawal")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurant-owner/withdrawal-management"))
				.andExpect(model().attributeExists("error"));
	}

	// ========== createWithdrawalRequest() Tests ==========

	@Test
	@DisplayName("createWithdrawalRequest - should create withdrawal successfully")
	void createWithdrawalRequest_ShouldCreateSuccessfully() throws Exception {
		when(withdrawalService.createWithdrawal(eq(1), any(CreateWithdrawalRequestDto.class)))
				.thenReturn(testWithdrawal);

		mockMvc.perform(post("/restaurant-owner/withdrawal/request")
				.param("amount", "500000")
				.param("bankAccountId", "1")
				.param("description", "Withdrawal request")
				.with(csrf())
				.principal(principal))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
				.andExpect(flash().attributeExists("success"));
	}

	@Test
	@DisplayName("createWithdrawalRequest - should handle exceptions")
	void createWithdrawalRequest_WithException_ShouldHandleError() throws Exception {
		when(withdrawalService.createWithdrawal(anyInt(), any())).thenThrow(new RuntimeException("Insufficient balance"));

		mockMvc.perform(post("/restaurant-owner/withdrawal/request")
				.param("amount", "500000")
				.param("bankAccountId", "1")
				.with(csrf())
				.principal(principal))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("error"));
	}

	// ========== fixBalance() Tests ==========

	@Test
	@DisplayName("fixBalance - should fix balance successfully")
	void fixBalance_ShouldFixSuccessfully() throws Exception {
		doNothing().when(balanceService).fixBalanceFromWithdrawals(1);

		mockMvc.perform(get("/restaurant-owner/withdrawal/fix-balance")
				.principal(principal))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
				.andExpect(flash().attributeExists("success"));
	}

	// ========== recalculateBalance() Tests ==========

	@Test
	@DisplayName("recalculateBalance - should recalculate successfully")
	void recalculateBalance_ShouldRecalculateSuccessfully() throws Exception {
		doNothing().when(balanceService).recalculateAll();

		mockMvc.perform(get("/restaurant-owner/withdrawal/recalculate-balance")
				.principal(principal))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
				.andExpect(flash().attributeExists("success"));
	}

	// ========== addBankAccount() Tests ==========

	@Test
	@DisplayName("addBankAccount - should add bank account successfully")
	void addBankAccount_ShouldAddSuccessfully() throws Exception {
		when(bankAccountService.addBankAccount(eq(1), any(RestaurantBankAccountDto.class)))
				.thenReturn(testBankAccount);

		mockMvc.perform(post("/restaurant-owner/withdrawal/bank-accounts")
				.param("bankCode", "VCB")
				.param("accountNumber", "1234567890")
				.param("accountHolderName", "John Doe")
				.param("isDefault", "true")
				.with(csrf())
				.principal(principal))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurant-owner/withdrawal#bank-accounts"))
				.andExpect(flash().attributeExists("success"));
	}

	@Test
	@DisplayName("addBankAccount - should handle exceptions")
	void addBankAccount_WithException_ShouldHandleError() throws Exception {
		when(bankAccountService.addBankAccount(anyInt(), any())).thenThrow(new RuntimeException("Invalid bank account"));

		mockMvc.perform(post("/restaurant-owner/withdrawal/bank-accounts")
				.param("bankCode", "VCB")
				.param("accountNumber", "1234567890")
				.param("accountHolderName", "John Doe")
				.with(csrf())
				.principal(principal))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("error"));
	}
}

