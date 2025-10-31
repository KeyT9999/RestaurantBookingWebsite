package com.example.booking.web.controller;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.RestaurantProfile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RestaurantWithdrawalViewController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RestaurantWithdrawalViewController WebMvc Tests")
class RestaurantWithdrawalViewControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantBalanceService balanceService;

    @MockBean
    private RestaurantBankAccountService bankAccountService;

    @MockBean
    private WithdrawalService withdrawalService;

    @MockBean
    private RestaurantProfileRepository restaurantRepository;

    @MockBean
    private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

    @MockBean
    private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

    private RestaurantProfile restaurant;
    private RestaurantBalanceDto balance;
    private List<RestaurantBankAccountDto> bankAccounts;
    private List<WithdrawalRequestDto> withdrawals;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(BigDecimal.valueOf(1000000));

        bankAccounts = new ArrayList<>();
        withdrawals = new ArrayList<>();
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/withdrawal - should show withdrawal management")
    void testWithdrawalManagement() throws Exception {
        // Given
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.singletonList(restaurant));
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(1)).thenReturn(balance);
        when(bankAccountService.getBankAccounts(1)).thenReturn(bankAccounts);
        when(withdrawalService.getAllWithdrawals(any(Pageable.class)))
            .thenReturn(new PageImpl<>(withdrawals));

        // When & Then
        mockMvc.perform(get("/restaurant-owner/withdrawal"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/withdrawal-management"))
            .andExpect(model().attributeExists("balance"))
            .andExpect(model().attributeExists("bankAccounts"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/withdrawal - should filter by status")
    void testWithdrawalManagement_WithStatusFilter() throws Exception {
        // Given
        WithdrawalRequestDto withdrawal = new WithdrawalRequestDto();
        withdrawal.setRequestId(1);
        withdrawal.setRestaurantId(1);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        List<WithdrawalRequestDto> filteredWithdrawals = Collections.singletonList(withdrawal);

        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.singletonList(restaurant));
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(1)).thenReturn(balance);
        when(bankAccountService.getBankAccounts(1)).thenReturn(bankAccounts);
        when(withdrawalService.getWithdrawalsByStatus(WithdrawalStatus.PENDING))
            .thenReturn(filteredWithdrawals);

        // When & Then - Controller filters by restaurantId too, so withdrawal must match restaurantId=1
        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/withdrawal-management"))
            .andExpect(model().attribute("filter", "PENDING"))
            .andExpect(model().attributeExists("withdrawals"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/withdrawal - should handle exception")
    void testWithdrawalManagement_Exception() throws Exception {
        // Given
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.singletonList(restaurant));
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(1)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/restaurant-owner/withdrawal"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/withdrawal-management"))
            .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/withdrawal/request - should create withdrawal request")
    void testCreateWithdrawalRequest() throws Exception {
        // Given
        WithdrawalRequestDto withdrawal = new WithdrawalRequestDto();
        withdrawal.setRequestId(1);
        withdrawal.setRestaurantId(1);

        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.singletonList(restaurant));
        when(withdrawalService.createWithdrawal(eq(1), any()))
            .thenReturn(withdrawal);

        // When & Then
        mockMvc.perform(post("/restaurant-owner/withdrawal/request")
                .param("amount", "500000")
                .param("bankAccountId", "1")
                .param("description", "Test withdrawal")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/withdrawal/request - should handle exception")
    void testCreateWithdrawalRequest_Exception() throws Exception {
        // Given
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.singletonList(restaurant));
        when(withdrawalService.createWithdrawal(eq(1), any()))
            .thenThrow(new RuntimeException("Invalid amount"));

        // When & Then
        mockMvc.perform(post("/restaurant-owner/withdrawal/request")
                .param("amount", "500000")
                .param("bankAccountId", "1")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/withdrawal/fix-balance - should fix balance")
    void testFixBalance() throws Exception {
        // Given
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.singletonList(restaurant));
        doNothing().when(balanceService).fixBalanceFromWithdrawals(1);

        // When & Then
        mockMvc.perform(get("/restaurant-owner/withdrawal/fix-balance"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/withdrawal/recalculate-balance - should recalculate balance")
    void testRecalculateBalance() throws Exception {
        // Given
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.singletonList(restaurant));
        doNothing().when(balanceService).recalculateAll();

        // When & Then
        mockMvc.perform(get("/restaurant-owner/withdrawal/recalculate-balance"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/withdrawal/bank-accounts - should add bank account")
    void testAddBankAccount() throws Exception {
        // Given
        RestaurantBankAccountDto bankAccount = new RestaurantBankAccountDto();
        bankAccount.setAccountId(1);
        bankAccount.setBankName("Vietcombank");
        bankAccount.setAccountNumber("1234567890");

        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.singletonList(restaurant));
        when(bankAccountService.addBankAccount(eq(1), any()))
            .thenReturn(bankAccount);

        // When & Then
        mockMvc.perform(post("/restaurant-owner/withdrawal/bank-accounts")
                .param("bankCode", "VCB")
                .param("accountNumber", "1234567890")
                .param("accountHolderName", "Test Owner")
                .param("isDefault", "true")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal#bank-accounts"));
    }
}

