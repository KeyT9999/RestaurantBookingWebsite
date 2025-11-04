package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.booking.common.api.ApiResponse;
import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for RestaurantWithdrawalApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantWithdrawalApiController Tests")
public class RestaurantWithdrawalApiControllerTest {

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

    @InjectMocks
    private RestaurantWithdrawalApiController controller;

    private RestaurantProfile restaurant;
    private Integer restaurantId;

    @BeforeEach
    void setUp() {
        restaurantId = 1;
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);

        RestaurantOwner owner = new RestaurantOwner();
        User user = new User();
        user.setId(UUID.randomUUID());
        owner.setUser(user);
        restaurant.setOwner(owner);
    }

    // ========== createWithdrawalRequest() Tests ==========

    @Test
    @DisplayName("shouldCreateWithdrawalRequest_successfully")
    void shouldCreateWithdrawalRequest_successfully() {
        // Given
        CreateWithdrawalRequestDto dto = new CreateWithdrawalRequestDto();
        dto.setAmount(new BigDecimal("1000000"));
        dto.setBankAccountId(1);

        WithdrawalRequestDto result = new WithdrawalRequestDto();
        result.setRequestId(1);
        result.setAmount(new BigDecimal("1000000"));

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(withdrawalService.createWithdrawal(restaurantId, dto)).thenReturn(result);

        // When
        ResponseEntity<ApiResponse<WithdrawalRequestDto>> response = 
            controller.createWithdrawalRequest(dto, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    // ========== getBalance() Tests ==========

    @Test
    @DisplayName("shouldGetBalance_successfully")
    void shouldGetBalance_successfully() {
        // Given
        RestaurantBalanceDto balance = new RestaurantBalanceDto();
        balance.setAvailableBalance(new BigDecimal("5000000"));

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(balanceService.getBalance(restaurantId)).thenReturn(balance);

        // When
        ResponseEntity<ApiResponse<RestaurantBalanceDto>> response = 
            controller.getBalance(principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldHandleError_whenCreateWithdrawalRequestFails")
    void shouldHandleError_whenCreateWithdrawalRequestFails() {
        // Given
        CreateWithdrawalRequestDto dto = new CreateWithdrawalRequestDto();
        dto.setAmount(new BigDecimal("1000000"));
        dto.setBankAccountId(1);

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(withdrawalService.createWithdrawal(restaurantId, dto))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<ApiResponse<WithdrawalRequestDto>> response = 
            controller.createWithdrawalRequest(dto, principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldHandleError_whenRestaurantNotFoundForCreateWithdrawal")
    void shouldHandleError_whenRestaurantNotFoundForCreateWithdrawal() {
        // Given
        CreateWithdrawalRequestDto dto = new CreateWithdrawalRequestDto();
        dto.setAmount(new BigDecimal("1000000"));

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<ApiResponse<WithdrawalRequestDto>> response = 
            controller.createWithdrawalRequest(dto, principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetBalanceFails")
    void shouldHandleError_whenGetBalanceFails() {
        // Given
        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(balanceService.getBalance(restaurantId))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<ApiResponse<RestaurantBalanceDto>> response = 
            controller.getBalance(principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    // ========== getBankAccounts() Tests ==========

    @Test
    @DisplayName("shouldGetBankAccounts_successfully")
    void shouldGetBankAccounts_successfully() {
        // Given
        RestaurantBankAccountDto bankAccount = new RestaurantBankAccountDto();
        bankAccount.setAccountId(1);
        bankAccount.setAccountNumber("1234567890");

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(bankAccountService.getBankAccounts(restaurantId))
            .thenReturn(Arrays.asList(bankAccount));

        // When
        ResponseEntity<ApiResponse<List<RestaurantBankAccountDto>>> response = 
            controller.getBankAccounts(principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetBankAccountsFails")
    void shouldHandleError_whenGetBankAccountsFails() {
        // Given
        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(bankAccountService.getBankAccounts(restaurantId))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<ApiResponse<List<RestaurantBankAccountDto>>> response = 
            controller.getBankAccounts(principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    // ========== addBankAccount() Tests ==========

    @Test
    @DisplayName("shouldAddBankAccount_successfully")
    void shouldAddBankAccount_successfully() {
        // Given
        RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
        dto.setAccountNumber("1234567890");
        dto.setBankName("Test Bank");

        RestaurantBankAccountDto result = new RestaurantBankAccountDto();
        result.setAccountId(1);
        result.setAccountNumber("1234567890");

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(bankAccountService.addBankAccount(restaurantId, dto)).thenReturn(result);

        // When
        ResponseEntity<ApiResponse<RestaurantBankAccountDto>> response = 
            controller.addBankAccount(dto, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldHandleError_whenAddBankAccountFails")
    void shouldHandleError_whenAddBankAccountFails() {
        // Given
        RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
        dto.setAccountNumber("1234567890");

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(bankAccountService.addBankAccount(restaurantId, dto))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<ApiResponse<RestaurantBankAccountDto>> response = 
            controller.addBankAccount(dto, principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    // ========== getWithdrawalRequests() Tests ==========

    @Test
    @DisplayName("shouldGetWithdrawalRequests_successfully")
    void shouldGetWithdrawalRequests_successfully() {
        // Given
        WithdrawalRequestDto request = new WithdrawalRequestDto();
        request.setRequestId(1);
        request.setRestaurantId(restaurantId);
        request.setStatus(WithdrawalStatus.PENDING);

        Page<WithdrawalRequestDto> requestPage = new PageImpl<>(Arrays.asList(request));

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(withdrawalService.getAllWithdrawals(any(Pageable.class))).thenReturn(requestPage);

        // When
        ResponseEntity<ApiResponse<Page<WithdrawalRequestDto>>> response = 
            controller.getWithdrawalRequests(0, 10, null, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldGetWithdrawalRequests_withStatusFilter")
    void shouldGetWithdrawalRequests_withStatusFilter() {
        // Given
        WithdrawalRequestDto request = new WithdrawalRequestDto();
        request.setRequestId(1);
        request.setRestaurantId(restaurantId);
        request.setStatus(WithdrawalStatus.PENDING);

        Page<WithdrawalRequestDto> requestPage = new PageImpl<>(Arrays.asList(request));

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(withdrawalService.getAllWithdrawals(any(Pageable.class))).thenReturn(requestPage);

        // When
        ResponseEntity<ApiResponse<Page<WithdrawalRequestDto>>> response = 
            controller.getWithdrawalRequests(0, 10, "PENDING", principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetWithdrawalRequestsFails")
    void shouldHandleError_whenGetWithdrawalRequestsFails() {
        // Given
        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(withdrawalService.getAllWithdrawals(any(Pageable.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<ApiResponse<Page<WithdrawalRequestDto>>> response = 
            controller.getWithdrawalRequests(0, 10, null, principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    // ========== getWithdrawalRequest() Tests ==========

    @Test
    @DisplayName("shouldGetWithdrawalRequest_successfully")
    void shouldGetWithdrawalRequest_successfully() {
        // Given
        Integer requestId = 1;
        WithdrawalRequestDto request = new WithdrawalRequestDto();
        request.setRequestId(requestId);
        request.setRestaurantId(restaurantId);
        request.setStatus(WithdrawalStatus.PENDING);

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(withdrawalService.getWithdrawal(requestId)).thenReturn(request);

        // When
        ResponseEntity<ApiResponse<WithdrawalRequestDto>> response = 
            controller.getWithdrawalRequest(requestId, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldReturnError_whenRequestDoesNotBelongToRestaurant")
    void shouldReturnError_whenRequestDoesNotBelongToRestaurant() {
        // Given
        Integer requestId = 1;
        WithdrawalRequestDto request = new WithdrawalRequestDto();
        request.setRequestId(requestId);
        request.setRestaurantId(999); // Different restaurant ID
        request.setStatus(WithdrawalStatus.PENDING);

        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(withdrawalService.getWithdrawal(requestId)).thenReturn(request);

        // When
        ResponseEntity<ApiResponse<WithdrawalRequestDto>> response = 
            controller.getWithdrawalRequest(requestId, principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetWithdrawalRequestFails")
    void shouldHandleError_whenGetWithdrawalRequestFails() {
        // Given
        Integer requestId = 1;
        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(java.util.List.of(restaurant));
        when(withdrawalService.getWithdrawal(requestId))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<ApiResponse<WithdrawalRequestDto>> response = 
            controller.getWithdrawalRequest(requestId, principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    // ========== getBalance() Additional Tests ==========

    @Test
    @DisplayName("shouldHandleError_whenRestaurantNotFoundForGetBalance")
    void shouldHandleError_whenRestaurantNotFoundForGetBalance() {
        // Given
        when(principal.getName()).thenReturn("owner");
        when(restaurantRepository.findByOwnerUsername("owner"))
            .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<ApiResponse<RestaurantBalanceDto>> response = 
            controller.getBalance(principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

