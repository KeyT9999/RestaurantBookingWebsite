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
}

