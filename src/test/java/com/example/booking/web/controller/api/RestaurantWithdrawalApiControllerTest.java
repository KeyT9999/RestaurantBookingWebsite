package com.example.booking.web.controller.api;

import com.example.booking.common.api.ApiResponse;
import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantWithdrawalApiController.class)
class RestaurantWithdrawalApiControllerTest {

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
    private WithdrawalRequestDto withdrawalRequest;
    private RestaurantBalanceDto balance;
    private RestaurantBankAccountDto bankAccount;
    private CreateWithdrawalRequestDto createRequest;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);

        balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(new BigDecimal("1000000"));
        balance.setPendingWithdrawal(new BigDecimal("500000"));

        bankAccount = new RestaurantBankAccountDto();
        bankAccount.setAccountId(1);
        bankAccount.setAccountNumber("1234567890");
        bankAccount.setAccountHolderName("Owner Name");

        withdrawalRequest = new WithdrawalRequestDto();
        withdrawalRequest.setRequestId(1);
        withdrawalRequest.setRestaurantId(1);
        withdrawalRequest.setAmount(new BigDecimal("500000"));
        withdrawalRequest.setStatus(WithdrawalStatus.PENDING);

        createRequest = new CreateWithdrawalRequestDto();
        createRequest.setBankAccountId(1);
        createRequest.setAmount(new BigDecimal("500000"));
        createRequest.setDescription("Monthly withdrawal");
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldCreateWithdrawalRequest() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        when(withdrawalService.createWithdrawal(eq(1), any(CreateWithdrawalRequestDto.class)))
                .thenReturn(withdrawalRequest);

        mockMvc.perform(post("/api/restaurant/withdrawal/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bankAccountId\":1,\"amount\":500000,\"description\":\"Monthly withdrawal\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestId").value(1));
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldGetBalance() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        when(balanceService.getBalance(1)).thenReturn(balance);

        mockMvc.perform(get("/api/restaurant/withdrawal/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableBalance").value(1000000));
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldGetBankAccounts() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        when(bankAccountService.getBankAccounts(1)).thenReturn(Arrays.asList(bankAccount));

        mockMvc.perform(get("/api/restaurant/withdrawal/bank-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].accountId").value(1));
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldAddBankAccount() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        when(bankAccountService.addBankAccount(eq(1), any(RestaurantBankAccountDto.class)))
                .thenReturn(bankAccount);

        mockMvc.perform(post("/api/restaurant/withdrawal/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountNumber\":\"1234567890\",\"accountHolderName\":\"Owner Name\",\"bankCode\":\"970422\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldGetWithdrawalRequests() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        Page<WithdrawalRequestDto> page = new PageImpl<>(Arrays.asList(withdrawalRequest));
        when(withdrawalService.getAllWithdrawals(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/restaurant/withdrawal/requests")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].requestId").value(1));
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldGetWithdrawalRequests_WithStatusFilter() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        Page<WithdrawalRequestDto> page = new PageImpl<>(Arrays.asList(withdrawalRequest));
        when(withdrawalService.getAllWithdrawals(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/restaurant/withdrawal/requests")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldGetWithdrawalRequest() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        when(withdrawalService.getWithdrawal(1)).thenReturn(withdrawalRequest);

        mockMvc.perform(get("/api/restaurant/withdrawal/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestId").value(1));
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldRejectUnauthorizedWithdrawalRequest() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        WithdrawalRequestDto otherRequest = new WithdrawalRequestDto();
        otherRequest.setRequestId(1);
        otherRequest.setRestaurantId(999); // Different restaurant
        when(withdrawalService.getWithdrawal(1)).thenReturn(otherRequest);

        mockMvc.perform(get("/api/restaurant/withdrawal/requests/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "owner", roles = "RESTAURANT_OWNER")
    void shouldHandleException() throws Exception {
        when(restaurantRepository.findByOwnerUsername("owner"))
                .thenReturn(Arrays.asList(restaurant));
        when(withdrawalService.createWithdrawal(eq(1), any(CreateWithdrawalRequestDto.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/restaurant/withdrawal/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bankAccountId\":1,\"amount\":500000}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

