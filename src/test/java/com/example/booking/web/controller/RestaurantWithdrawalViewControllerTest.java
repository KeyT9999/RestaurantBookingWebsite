package com.example.booking.web.controller;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantWithdrawalViewController.class)
@DisplayName("RestaurantWithdrawalViewController Test")
class RestaurantWithdrawalViewControllerTest {

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

    private Principal principal;
    private RestaurantProfile restaurant;
    private Integer restaurantId;

    @BeforeEach
    void setUp() {
        principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        restaurantId = 1;
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");

        when(restaurantRepository.findByOwnerUsername("testuser"))
            .thenReturn(Collections.singletonList(restaurant));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal - Should display withdrawal management page")
    void testWithdrawalManagement_ShouldDisplayPage() throws Exception {
        RestaurantBalanceDto balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(BigDecimal.valueOf(1000000));

        List<RestaurantBankAccountDto> bankAccounts = Arrays.asList(
            createBankAccount(1, "VCB", "1234567890")
        );

        WithdrawalRequestDto withdrawal = new WithdrawalRequestDto();
        withdrawal.setRequestId(1);
        withdrawal.setRestaurantId(restaurantId);

        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(restaurantId))
            .thenReturn(balance);
        when(bankAccountService.getBankAccounts(restaurantId))
            .thenReturn(bankAccounts);
        when(withdrawalService.getAllWithdrawals(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(withdrawal)));

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/withdrawal-management"))
            .andExpect(model().attributeExists("restaurantName"))
            .andExpect(model().attributeExists("balance"))
            .andExpect(model().attributeExists("bankAccounts"))
            .andExpect(model().attributeExists("withdrawals"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal?status=PENDING - Should filter by status")
    void testWithdrawalManagement_WithStatusFilter() throws Exception {
        RestaurantBalanceDto balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(BigDecimal.valueOf(1000000));

        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(restaurantId))
            .thenReturn(balance);
        when(bankAccountService.getBankAccounts(restaurantId))
            .thenReturn(Collections.emptyList());
        when(withdrawalService.getWithdrawalsByStatus(WithdrawalStatus.PENDING))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .param("status", "PENDING")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(model().attribute("filter", "PENDING"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal - Should handle restaurant not found")
    void testWithdrawalManagement_RestaurantNotFound() throws Exception {
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/withdrawal/request - Should create withdrawal request")
    void testCreateWithdrawalRequest_ShouldCreateSuccessfully() throws Exception {
        WithdrawalRequestDto result = new WithdrawalRequestDto();
        result.setRequestId(1);
        result.setRestaurantId(restaurantId);

        when(withdrawalService.createWithdrawal(eq(restaurantId), any(CreateWithdrawalRequestDto.class)))
            .thenReturn(result);

        mockMvc.perform(post("/restaurant-owner/withdrawal/request")
                .param("amount", "500000")
                .param("bankAccountId", "1")
                .param("description", "Test withdrawal")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("success"));

        verify(withdrawalService).createWithdrawal(eq(restaurantId), any(CreateWithdrawalRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/withdrawal/request - Should handle error")
    void testCreateWithdrawalRequest_ShouldHandleError() throws Exception {
        when(withdrawalService.createWithdrawal(eq(restaurantId), any(CreateWithdrawalRequestDto.class)))
            .thenThrow(new RuntimeException("Insufficient balance"));

        mockMvc.perform(post("/restaurant-owner/withdrawal/request")
                .param("amount", "500000")
                .param("bankAccountId", "1")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal/fix-balance - Should fix balance")
    void testFixBalance_ShouldFixSuccessfully() throws Exception {
        doNothing().when(balanceService).fixBalanceFromWithdrawals(restaurantId);

        mockMvc.perform(get("/restaurant-owner/withdrawal/fix-balance")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("success"));

        verify(balanceService).fixBalanceFromWithdrawals(restaurantId);
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal/recalculate-balance - Should recalculate balance")
    void testRecalculateBalance_ShouldRecalculateSuccessfully() throws Exception {
        doNothing().when(balanceService).recalculateAll();

        mockMvc.perform(get("/restaurant-owner/withdrawal/recalculate-balance")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("success"));

        verify(balanceService).recalculateAll();
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/bank-accounts - Should add bank account")
    void testAddBankAccount_ShouldAddSuccessfully() throws Exception {
        RestaurantBankAccountDto result = createBankAccount(1, "VCB", "1234567890");

        when(bankAccountService.addBankAccount(eq(restaurantId), any(RestaurantBankAccountDto.class)))
            .thenReturn(result);

        mockMvc.perform(post("/restaurant-owner/withdrawal/bank-accounts")
                .param("bankCode", "VCB")
                .param("accountNumber", "1234567890")
                .param("accountHolderName", "Test Owner")
                .param("isDefault", "true")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal#bank-accounts"))
            .andExpect(flash().attributeExists("success"));

        verify(bankAccountService).addBankAccount(eq(restaurantId), any(RestaurantBankAccountDto.class));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/bank-accounts - Should handle error")
    void testAddBankAccount_ShouldHandleError() throws Exception {
        when(bankAccountService.addBankAccount(eq(restaurantId), any(RestaurantBankAccountDto.class)))
            .thenThrow(new RuntimeException("Invalid bank account"));

        mockMvc.perform(post("/restaurant-owner/withdrawal/bank-accounts")
                .param("bankCode", "VCB")
                .param("accountNumber", "1234567890")
                .param("accountHolderName", "Test Owner")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal#bank-accounts"))
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal?status=ALL - Should show all withdrawals")
    void testWithdrawalManagement_WithAllStatus() throws Exception {
        RestaurantBalanceDto balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(BigDecimal.valueOf(1000000));

        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(restaurantId))
            .thenReturn(balance);
        when(bankAccountService.getBankAccounts(restaurantId))
            .thenReturn(Collections.emptyList());
        when(withdrawalService.getAllWithdrawals(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .param("status", "ALL")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(model().attribute("filter", "ALL"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal - Should handle exception")
    void testWithdrawalManagement_ShouldHandleException() throws Exception {
        when(restaurantRepository.findById(restaurantId))
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("error"))
            .andExpect(model().attributeExists("balance"))
            .andExpect(model().attributeExists("bankAccounts"))
            .andExpect(model().attributeExists("withdrawals"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/withdrawal/request - Should handle null description")
    void testCreateWithdrawalRequest_WithNullDescription() throws Exception {
        WithdrawalRequestDto result = new WithdrawalRequestDto();
        result.setRequestId(1);
        result.setRestaurantId(restaurantId);

        when(withdrawalService.createWithdrawal(eq(restaurantId), any(CreateWithdrawalRequestDto.class)))
            .thenReturn(result);

        mockMvc.perform(post("/restaurant-owner/withdrawal/request")
                .param("amount", "500000")
                .param("bankAccountId", "1")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal/fix-balance - Should handle error")
    void testFixBalance_ShouldHandleError() throws Exception {
        doThrow(new RuntimeException("Balance fix failed"))
            .when(balanceService).fixBalanceFromWithdrawals(restaurantId);

        mockMvc.perform(get("/restaurant-owner/withdrawal/fix-balance")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal/recalculate-balance - Should handle error")
    void testRecalculateBalance_ShouldHandleError() throws Exception {
        doThrow(new RuntimeException("Recalculation failed"))
            .when(balanceService).recalculateAll();

        mockMvc.perform(get("/restaurant-owner/withdrawal/recalculate-balance")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal - Should filter withdrawals by restaurant")
    void testWithdrawalManagement_ShouldFilterByRestaurant() throws Exception {
        RestaurantBalanceDto balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(BigDecimal.valueOf(1000000));

        WithdrawalRequestDto withdrawal1 = new WithdrawalRequestDto();
        withdrawal1.setRequestId(1);
        withdrawal1.setRestaurantId(restaurantId);

        WithdrawalRequestDto withdrawal2 = new WithdrawalRequestDto();
        withdrawal2.setRequestId(2);
        withdrawal2.setRestaurantId(999); // Different restaurant

        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(restaurantId))
            .thenReturn(balance);
        when(bankAccountService.getBankAccounts(restaurantId))
            .thenReturn(Collections.emptyList());
        when(withdrawalService.getAllWithdrawals(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(withdrawal1, withdrawal2)));

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("withdrawals"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal - Should handle empty status string")
    void testWithdrawalManagement_WithEmptyStatus() throws Exception {
        RestaurantBalanceDto balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(BigDecimal.valueOf(1000000));

        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(restaurantId))
            .thenReturn(balance);
        when(bankAccountService.getBankAccounts(restaurantId))
            .thenReturn(Collections.emptyList());
        when(withdrawalService.getAllWithdrawals(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .param("status", "")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(model().attribute("filter", "ALL"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal - Should handle getRestaurantId exception")
    void testWithdrawalManagement_WithGetRestaurantIdException() throws Exception {
        when(restaurantRepository.findByOwnerUsername("testuser"))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/withdrawal/request - Should handle empty description")
    void testCreateWithdrawalRequest_WithEmptyDescription() throws Exception {
        WithdrawalRequestDto result = new WithdrawalRequestDto();
        result.setRequestId(1);
        result.setRestaurantId(restaurantId);

        when(withdrawalService.createWithdrawal(eq(restaurantId), any(CreateWithdrawalRequestDto.class)))
            .thenReturn(result);

        mockMvc.perform(post("/restaurant-owner/withdrawal/request")
                .param("amount", "500000")
                .param("bankAccountId", "1")
                .param("description", "")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/withdrawal/request - Should handle whitespace description")
    void testCreateWithdrawalRequest_WithWhitespaceDescription() throws Exception {
        WithdrawalRequestDto result = new WithdrawalRequestDto();
        result.setRequestId(1);
        result.setRestaurantId(restaurantId);

        when(withdrawalService.createWithdrawal(eq(restaurantId), any(CreateWithdrawalRequestDto.class)))
            .thenReturn(result);

        mockMvc.perform(post("/restaurant-owner/withdrawal/request")
                .param("amount", "500000")
                .param("bankAccountId", "1")
                .param("description", "   ")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal"))
            .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/withdrawal/bank-accounts - Should handle null isDefault")
    void testAddBankAccount_WithNullIsDefault() throws Exception {
        RestaurantBankAccountDto result = createBankAccount(1, "VCB", "1234567890");

        when(bankAccountService.addBankAccount(eq(restaurantId), any(RestaurantBankAccountDto.class)))
            .thenReturn(result);

        mockMvc.perform(post("/restaurant-owner/withdrawal/bank-accounts")
                .param("bankCode", "VCB")
                .param("accountNumber", "1234567890")
                .param("accountHolderName", "Test Owner")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal#bank-accounts"))
            .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("GET /restaurant-owner/withdrawal?status=SUCCEEDED - Should filter by SUCCEEDED status")
    void testWithdrawalManagement_WithSucceededStatus() throws Exception {
        RestaurantBalanceDto balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(BigDecimal.valueOf(1000000));

        WithdrawalRequestDto withdrawal = new WithdrawalRequestDto();
        withdrawal.setRequestId(1);
        withdrawal.setRestaurantId(restaurantId);
        withdrawal.setStatus(WithdrawalStatus.SUCCEEDED);

        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(restaurant));
        when(balanceService.getBalance(restaurantId))
            .thenReturn(balance);
        when(bankAccountService.getBankAccounts(restaurantId))
            .thenReturn(Collections.emptyList());
        when(withdrawalService.getWithdrawalsByStatus(WithdrawalStatus.SUCCEEDED))
            .thenReturn(Arrays.asList(withdrawal));

        mockMvc.perform(get("/restaurant-owner/withdrawal")
                .param("status", "SUCCEEDED")
                .principal(principal))
            .andExpect(status().isOk())
            .andExpect(model().attribute("filter", "SUCCEEDED"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("POST /restaurant-owner/withdrawal/bank-accounts - Should handle false isDefault")
    void testAddBankAccount_WithFalseIsDefault() throws Exception {
        RestaurantBankAccountDto result = createBankAccount(1, "VCB", "1234567890");

        when(bankAccountService.addBankAccount(eq(restaurantId), any(RestaurantBankAccountDto.class)))
            .thenReturn(result);

        mockMvc.perform(post("/restaurant-owner/withdrawal/bank-accounts")
                .param("bankCode", "VCB")
                .param("accountNumber", "1234567890")
                .param("accountHolderName", "Test Owner")
                .param("isDefault", "false")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/withdrawal#bank-accounts"))
            .andExpect(flash().attributeExists("success"));
    }

    private RestaurantBankAccountDto createBankAccount(Integer accountId, String bankCode, String accountNumber) {
        RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
        dto.setAccountId(accountId);
        dto.setBankCode(bankCode);
        dto.setAccountNumber(accountNumber);
        dto.setBankName("Vietcombank");
        return dto;
    }
}

