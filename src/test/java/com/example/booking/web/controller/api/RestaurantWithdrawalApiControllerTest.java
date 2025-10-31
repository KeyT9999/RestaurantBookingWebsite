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
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;

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
}

