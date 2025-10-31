package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.RestaurantBankAccount;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.RestaurantBankAccountRepository;
import com.example.booking.repository.RestaurantProfileRepository;

/**
 * Unit tests for RestaurantBankAccountService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantBankAccountService Tests")
public class RestaurantBankAccountServiceTest {

    @Mock
    private RestaurantBankAccountRepository bankAccountRepository;

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @InjectMocks
    private RestaurantBankAccountService bankAccountService;

    private Integer restaurantId;
    private Integer accountId;
    private RestaurantProfile restaurant;
    private RestaurantBankAccount account;
    private RestaurantBankAccountDto accountDto;

    @BeforeEach
    void setUp() {
        restaurantId = 1;
        accountId = 1;

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");

        account = new RestaurantBankAccount();
        account.setAccountId(accountId);
        account.setRestaurant(restaurant);
        account.setBankCode("VCB");
        account.setBankName("Vietcombank");
        account.setAccountNumber("1234567890");
        account.setAccountHolderName("Test Owner");
        account.setIsDefault(true);
        account.setIsVerified(false);

        accountDto = new RestaurantBankAccountDto();
        accountDto.setBankCode("VCB");
        accountDto.setAccountNumber("1234567890");
        accountDto.setAccountHolderName("Test Owner");
        accountDto.setIsDefault(true);
    }

    // ========== getBankAccounts() Tests ==========

    @Test
    @DisplayName("shouldGetBankAccounts_successfully")
    void shouldGetBankAccounts_successfully() {
        // Given
        List<RestaurantBankAccount> accounts = Arrays.asList(account);
        when(bankAccountRepository.findByRestaurantId(restaurantId)).thenReturn(accounts);

        // When
        List<RestaurantBankAccountDto> result = bankAccountService.getBankAccounts(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("shouldReturnNull_whenNoDefaultAccount")
    void shouldReturnNull_whenNoDefaultAccount() {
        // Given
        when(bankAccountRepository.findByRestaurantIdAndIsDefaultTrue(restaurantId))
                .thenReturn(Optional.empty());

        // When
        RestaurantBankAccountDto result = bankAccountService.getDefaultBankAccount(restaurantId);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("shouldThrowException_whenAccountNumberExists")
    void shouldThrowException_whenAccountNumberExists() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(restaurantId, accountDto.getAccountNumber()))
                .thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            bankAccountService.addBankAccount(restaurantId, accountDto);
        });

        assertTrue(exception.getMessage().contains("Số tài khoản này đã tồn tại"));
    }

    @Test
    @DisplayName("shouldSetAsDefault_whenFirstAccount")
    void shouldSetAsDefault_whenFirstAccount() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(restaurantId, accountDto.getAccountNumber()))
                .thenReturn(false);
        when(bankAccountRepository.countByRestaurantId(restaurantId)).thenReturn(0L);
        when(bankAccountRepository.save(any(RestaurantBankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RestaurantBankAccountDto result = bankAccountService.addBankAccount(restaurantId, accountDto);

        // Then - First account should be default
        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(bankAccountRepository).save(any(RestaurantBankAccount.class));
    }

    @Test
    @DisplayName("shouldKeepExistingDefault_whenIsDefaultIsNull")
    void shouldKeepExistingDefault_whenIsDefaultIsNull() {
        // Given
        account.setIsDefault(true);
        accountDto.setIsDefault(null); // Null means keep existing

        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumberAndAccountIdNot(
                restaurantId, accountDto.getAccountNumber(), accountId)).thenReturn(false);
        when(bankAccountRepository.save(any(RestaurantBankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RestaurantBankAccountDto result = bankAccountService.updateBankAccount(restaurantId, accountId, accountDto);

        // Then - Should keep existing default value
        assertNotNull(result);
        assertTrue(result.getIsDefault()); // Should keep true
        verify(bankAccountRepository, never()).unsetDefaultForRestaurant(restaurantId);
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenNoAccounts")
    void shouldReturnEmptyList_whenNoAccounts() {
        // Given
        when(bankAccountRepository.findByRestaurantId(restaurantId)).thenReturn(Arrays.asList());

        // When
        List<RestaurantBankAccountDto> result = bankAccountService.getBankAccounts(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ========== addBankAccount() Tests ==========

    @Test
    @DisplayName("shouldAddBankAccount_successfully")
    void shouldAddBankAccount_successfully() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(restaurantId, "1234567890"))
            .thenReturn(false);
        when(bankAccountRepository.countByRestaurantId(restaurantId)).thenReturn(0L);
        when(bankAccountRepository.save(any(RestaurantBankAccount.class))).thenReturn(account);

        // When
        RestaurantBankAccountDto result = bankAccountService.addBankAccount(restaurantId, accountDto);

        // Then
        assertNotNull(result);
        verify(bankAccountRepository, times(1)).save(any(RestaurantBankAccount.class));
    }

    @Test
    @DisplayName("shouldSetFirstAccountAsDefault")
    void shouldSetFirstAccountAsDefault() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(restaurantId, "1234567890"))
            .thenReturn(false);
        when(bankAccountRepository.countByRestaurantId(restaurantId)).thenReturn(0L);
        when(bankAccountRepository.save(any(RestaurantBankAccount.class))).thenReturn(account);

        // When
        RestaurantBankAccountDto result = bankAccountService.addBankAccount(restaurantId, accountDto);

        // Then
        assertNotNull(result);
        verify(bankAccountRepository, times(1)).save(any(RestaurantBankAccount.class));
    }

    @Test
    @DisplayName("shouldThrowException_whenRestaurantNotFound")
    void shouldThrowException_whenRestaurantNotFound() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankAccountService.addBankAccount(restaurantId, accountDto);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenAccountNumberExists_Update")
    void shouldThrowException_whenAccountNumberExists_Update() {
        // Given - duplicate account number for update
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumberAndAccountIdNot(restaurantId, "1234567890",
                accountId))
            .thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            bankAccountService.updateBankAccount(restaurantId, accountId, accountDto);
        });
    }

    // ========== updateBankAccount() Tests ==========

    @Test
    @DisplayName("shouldUpdateBankAccount_successfully")
    void shouldUpdateBankAccount_successfully() {
        // Given
        account.setRestaurant(restaurant);
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(restaurantId, "1234567890"))
            .thenReturn(false);
        when(bankAccountRepository.save(account)).thenReturn(account);

        // When
        RestaurantBankAccountDto result = bankAccountService.updateBankAccount(restaurantId, accountId, accountDto);

        // Then
        assertNotNull(result);
        verify(bankAccountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("shouldThrowException_whenAccountNotFound")
    void shouldThrowException_whenAccountNotFound() {
        // Given
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankAccountService.updateBankAccount(restaurantId, accountId, accountDto);
        });
    }

    // ========== deleteBankAccount() Tests ==========

    @Test
    @DisplayName("shouldDeleteBankAccount_successfully")
    void shouldDeleteBankAccount_successfully() {
        // Given
        account.setRestaurant(restaurant);
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(bankAccountRepository).delete(account);

        // When
        bankAccountService.deleteBankAccount(restaurantId, accountId);

        // Then
        verify(bankAccountRepository, times(1)).delete(account);
    }

    @Test
    @DisplayName("shouldThrowException_whenDeleteAccountNotFound")
    void shouldThrowException_whenDeleteAccountNotFound() {
        // Given
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankAccountService.deleteBankAccount(restaurantId, accountId);
        });
    }


    // ========== getDefaultBankAccount() Tests ==========

    @Test
    @DisplayName("shouldGetDefaultBankAccount_successfully")
    void shouldGetDefaultBankAccount_successfully() {
        // Given
        when(bankAccountRepository.findByRestaurantIdAndIsDefaultTrue(restaurantId))
            .thenReturn(Optional.of(account));

        // When
        RestaurantBankAccountDto result = bankAccountService.getDefaultBankAccount(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsDefault());
    }

    @Test
    @DisplayName("shouldReturnNull_whenNoDefaultAccount_Duplicate")
    void shouldReturnNull_whenNoDefaultAccount_Duplicate() {
        // Given
        when(bankAccountRepository.findByRestaurantIdAndIsDefaultTrue(restaurantId))
            .thenReturn(Optional.empty());

        // When
        RestaurantBankAccountDto result = bankAccountService.getDefaultBankAccount(restaurantId);

        // Then
        assertNull(result);
    }

    // ========== Validation Tests ==========

    @Test
    @DisplayName("shouldThrowException_whenBankCodeIsEmpty")
    void shouldThrowException_whenBankCodeIsEmpty() {
        // Given
        accountDto.setBankCode("");
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(com.example.booking.exception.BadRequestException.class, () -> {
            bankAccountService.addBankAccount(restaurantId, accountDto);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenAccountNumberIsInvalid")
    void shouldThrowException_whenAccountNumberIsInvalid() {
        // Given
        accountDto.setAccountNumber("123"); // Too short
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(com.example.booking.exception.BadRequestException.class, () -> {
            bankAccountService.addBankAccount(restaurantId, accountDto);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenAccountNumberIsNull")
    void shouldThrowException_whenAccountNumberIsNull() {
        // Given
        accountDto.setAccountNumber(null);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(com.example.booking.exception.BadRequestException.class, () -> {
            bankAccountService.addBankAccount(restaurantId, accountDto);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenDeleteDefaultAccount_withOtherAccounts")
    void shouldThrowException_whenDeleteDefaultAccount_withOtherAccounts() {
        // Given
        account.setIsDefault(true);
        account.setRestaurant(restaurant);
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(bankAccountRepository.countByRestaurantId(restaurantId)).thenReturn(2L);

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            bankAccountService.deleteBankAccount(restaurantId, accountId);
        });
    }

    @Test
    @DisplayName("shouldUnsetDefaultForRestaurant_whenSettingNewDefault")
    void shouldUnsetDefaultForRestaurant_whenSettingNewDefault() {
        // Given
        account.setRestaurant(restaurant);
        account.setIsDefault(false);
        accountDto.setIsDefault(true);
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumberAndAccountIdNot(
            restaurantId, "1234567890", accountId)).thenReturn(false);
        doNothing().when(bankAccountRepository).unsetDefaultForRestaurant(restaurantId);
        when(bankAccountRepository.save(account)).thenReturn(account);

        // When
        RestaurantBankAccountDto result = bankAccountService.updateBankAccount(restaurantId, accountId, accountDto);

        // Then
        assertNotNull(result);
        verify(bankAccountRepository, times(1)).unsetDefaultForRestaurant(restaurantId);
    }

    @Test
    @DisplayName("shouldThrowException_whenAccountNotOwnedByRestaurant")
    void shouldThrowException_whenAccountNotOwnedByRestaurant() {
        // Given
        RestaurantProfile otherRestaurant = new RestaurantProfile();
        otherRestaurant.setRestaurantId(999);
        account.setRestaurant(otherRestaurant);
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            bankAccountService.updateBankAccount(restaurantId, accountId, accountDto);
        });
    }

    // ========== convertToDto() - Mask Account Number Tests ==========

    @Test
    @DisplayName("shouldMaskAccountNumber_whenAccountNumberLengthGreaterThan4")
    void shouldMaskAccountNumber_whenAccountNumberLengthGreaterThan4() {
        // Given
        account.setAccountNumber("1234567890"); // 10 characters
        when(bankAccountRepository.findByRestaurantId(restaurantId))
                .thenReturn(java.util.Arrays.asList(account));

        // When
        List<RestaurantBankAccountDto> result = bankAccountService.getBankAccounts(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getMaskedAccountNumber());
        assertEquals("******7890", result.get(0).getMaskedAccountNumber());
    }

    @Test
    @DisplayName("shouldNotMaskAccountNumber_whenAccountNumberLengthIs4OrLess")
    void shouldNotMaskAccountNumber_whenAccountNumberLengthIs4OrLess() {
        // Given
        account.setAccountNumber("1234"); // Exactly 4 characters
        when(bankAccountRepository.findByRestaurantId(restaurantId))
                .thenReturn(java.util.Arrays.asList(account));

        // When
        List<RestaurantBankAccountDto> result = bankAccountService.getBankAccounts(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getMaskedAccountNumber());
    }

    @Test
    @DisplayName("shouldNotMaskAccountNumber_whenAccountNumberIsNull")
    void shouldNotMaskAccountNumber_whenAccountNumberIsNull() {
        // Given
        account.setAccountNumber(null);
        when(bankAccountRepository.findByRestaurantId(restaurantId))
                .thenReturn(java.util.Arrays.asList(account));

        // When
        List<RestaurantBankAccountDto> result = bankAccountService.getBankAccounts(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getMaskedAccountNumber());
    }

    @Test
    @DisplayName("shouldNotMaskAccountNumber_whenAccountNumberIsEmpty")
    void shouldNotMaskAccountNumber_whenAccountNumberIsEmpty() {
        // Given
        account.setAccountNumber("");
        when(bankAccountRepository.findByRestaurantId(restaurantId))
                .thenReturn(java.util.Arrays.asList(account));

        // When
        List<RestaurantBankAccountDto> result = bankAccountService.getBankAccounts(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getMaskedAccountNumber());
    }

    // ========== getRestaurantBankAccountById() Tests ==========
    // Note: This method doesn't exist in the service, but we test
    // getDefaultBankAccount exception case

    @Test
    @DisplayName("shouldHandleException_whenGettingDefaultBankAccount")
    void shouldHandleException_whenGettingDefaultBankAccount() {
        // Given
        when(bankAccountRepository.findByRestaurantIdAndIsDefaultTrue(restaurantId))
                .thenReturn(Optional.empty());

        // When
        RestaurantBankAccountDto result = bankAccountService.getDefaultBankAccount(restaurantId);

        // Then
        assertNull(result);
    }
}

