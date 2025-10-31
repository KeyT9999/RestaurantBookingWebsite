package com.example.booking.service;

import com.example.booking.domain.RestaurantBankAccount;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.RestaurantBankAccountRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantBankAccountServiceTest {

    @Mock
    private RestaurantBankAccountRepository bankAccountRepository;

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @InjectMocks
    private RestaurantBankAccountService bankAccountService;

    private RestaurantProfile restaurant;
    private RestaurantBankAccount bankAccount;
    private RestaurantBankAccountDto bankAccountDto;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        bankAccount = new RestaurantBankAccount();
        bankAccount.setAccountId(1);
        bankAccount.setRestaurant(restaurant);
        bankAccount.setBankCode("970422");
        bankAccount.setBankName("MB Bank");
        bankAccount.setAccountNumber("1234567890");
        bankAccount.setAccountHolderName("Test Owner");
        bankAccount.setIsVerified(false);
        bankAccount.setIsDefault(true);

        bankAccountDto = new RestaurantBankAccountDto();
        bankAccountDto.setAccountId(1);
        bankAccountDto.setRestaurantId(1);
        bankAccountDto.setBankCode("970422");
        bankAccountDto.setBankName("MB Bank");
        bankAccountDto.setAccountNumber("1234567890");
        bankAccountDto.setAccountHolderName("Test Owner");
        bankAccountDto.setIsDefault(true);
    }

    @Test
    void shouldGetBankAccounts() {
        when(bankAccountRepository.findByRestaurantId(1)).thenReturn(Arrays.asList(bankAccount));

        List<RestaurantBankAccountDto> result = bankAccountService.getBankAccounts(1);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountId()).isEqualTo(1);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("1234567890");
    }

    @Test
    void shouldAddBankAccount() {
        RestaurantBankAccountDto newDto = new RestaurantBankAccountDto();
        newDto.setBankCode("970436");
        newDto.setAccountNumber("9876543210");
        newDto.setAccountHolderName("New Owner");

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(1, "9876543210")).thenReturn(false);
        when(bankAccountRepository.countByRestaurantId(1)).thenReturn(0L);
        when(bankAccountRepository.save(any(RestaurantBankAccount.class))).thenReturn(bankAccount);

        RestaurantBankAccountDto result = bankAccountService.addBankAccount(1, newDto);

        assertThat(result).isNotNull();
        verify(bankAccountRepository).save(any(RestaurantBankAccount.class));
    }

    @Test
    void shouldAddBankAccount_SetAsDefaultIfFirst() {
        RestaurantBankAccountDto newDto = new RestaurantBankAccountDto();
        newDto.setBankCode("970436");
        newDto.setAccountNumber("9876543210");
        newDto.setAccountHolderName("New Owner");

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(1, "9876543210")).thenReturn(false);
        when(bankAccountRepository.countByRestaurantId(1)).thenReturn(0L);
        when(bankAccountRepository.save(any(RestaurantBankAccount.class))).thenAnswer(invocation -> {
            RestaurantBankAccount account = invocation.getArgument(0);
            account.setAccountId(1);
            return account;
        });

        RestaurantBankAccountDto result = bankAccountService.addBankAccount(1, newDto);

        assertThat(result).isNotNull();
        verify(bankAccountRepository).save(any(RestaurantBankAccount.class));
    }

    @Test
    void shouldAddBankAccount_UnsetOtherDefaults() {
        RestaurantBankAccountDto newDto = new RestaurantBankAccountDto();
        newDto.setBankCode("970436");
        newDto.setAccountNumber("9876543210");
        newDto.setAccountHolderName("New Owner");
        newDto.setIsDefault(true);

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(1, "9876543210")).thenReturn(false);
        when(bankAccountRepository.countByRestaurantId(1)).thenReturn(1L);
        when(bankAccountRepository.save(any(RestaurantBankAccount.class))).thenReturn(bankAccount);

        bankAccountService.addBankAccount(1, newDto);

        verify(bankAccountRepository).unsetDefaultForRestaurant(1);
    }

    @Test
    void shouldAddBankAccount_RestaurantNotFound() {
        RestaurantBankAccountDto newDto = new RestaurantBankAccountDto();
        newDto.setBankCode("970436");
        newDto.setAccountNumber("9876543210");
        newDto.setAccountHolderName("New Owner");

        when(restaurantRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.addBankAccount(1, newDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy nhà hàng");
    }

    @Test
    void shouldAddBankAccount_DuplicateAccountNumber() {
        RestaurantBankAccountDto newDto = new RestaurantBankAccountDto();
        newDto.setBankCode("970436");
        newDto.setAccountNumber("1234567890");
        newDto.setAccountHolderName("New Owner");

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(1, "1234567890")).thenReturn(true);

        assertThatThrownBy(() -> bankAccountService.addBankAccount(1, newDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số tài khoản này đã tồn tại");
    }

    @Test
    void shouldAddBankAccount_InvalidAccountNumber() {
        RestaurantBankAccountDto newDto = new RestaurantBankAccountDto();
        newDto.setBankCode("970436");
        newDto.setAccountNumber("123"); // Too short
        newDto.setAccountHolderName("New Owner");

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(1, "123")).thenReturn(false);

        assertThatThrownBy(() -> bankAccountService.addBankAccount(1, newDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số tài khoản phải chứa từ 9-20 chữ số");
    }

    @Test
    void shouldUpdateBankAccount() {
        RestaurantBankAccountDto updateDto = new RestaurantBankAccountDto();
        updateDto.setBankCode("970436");
        updateDto.setAccountNumber("9876543210");
        updateDto.setAccountHolderName("Updated Owner");
        updateDto.setIsDefault(false);

        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumberAndAccountIdNot(1, "9876543210", 1)).thenReturn(false);
        when(bankAccountRepository.save(any(RestaurantBankAccount.class))).thenReturn(bankAccount);

        RestaurantBankAccountDto result = bankAccountService.updateBankAccount(1, 1, updateDto);

        assertThat(result).isNotNull();
        verify(bankAccountRepository).save(any(RestaurantBankAccount.class));
    }

    @Test
    void shouldUpdateBankAccount_NotFound() {
        RestaurantBankAccountDto updateDto = new RestaurantBankAccountDto();
        when(bankAccountRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.updateBankAccount(1, 1, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy tài khoản ngân hàng");
    }

    @Test
    void shouldUpdateBankAccount_WrongRestaurant() {
        RestaurantProfile otherRestaurant = new RestaurantProfile();
        otherRestaurant.setRestaurantId(2);

        RestaurantBankAccount otherAccount = new RestaurantBankAccount();
        otherAccount.setAccountId(1);
        otherAccount.setRestaurant(otherRestaurant);

        RestaurantBankAccountDto updateDto = new RestaurantBankAccountDto();
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(otherAccount));

        assertThatThrownBy(() -> bankAccountService.updateBankAccount(1, 1, updateDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thuộc nhà hàng này");
    }

    @Test
    void shouldDeleteBankAccount() {
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.countByRestaurantId(1)).thenReturn(2L); // More than 1 account

        bankAccountService.deleteBankAccount(1, 1);

        verify(bankAccountRepository).delete(bankAccount);
    }

    @Test
    void shouldDeleteBankAccount_NotFound() {
        when(bankAccountRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.deleteBankAccount(1, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy tài khoản ngân hàng");
    }

    @Test
    void shouldDeleteBankAccount_CannotDeleteDefaultWhenMultipleAccounts() {
        bankAccount.setIsDefault(true);
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.countByRestaurantId(1)).thenReturn(2L); // More than 1 account

        assertThatThrownBy(() -> bankAccountService.deleteBankAccount(1, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Không thể xóa tài khoản mặc định");
    }

    @Test
    void shouldGetDefaultBankAccount() {
        when(bankAccountRepository.findByRestaurantIdAndIsDefaultTrue(1)).thenReturn(Optional.of(bankAccount));

        RestaurantBankAccountDto result = bankAccountService.getDefaultBankAccount(1);

        assertThat(result).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(1);
        assertThat(result.getIsDefault()).isTrue();
    }

    @Test
    void shouldGetDefaultBankAccount_NotFound() {
        when(bankAccountRepository.findByRestaurantIdAndIsDefaultTrue(1)).thenReturn(Optional.empty());

        RestaurantBankAccountDto result = bankAccountService.getDefaultBankAccount(1);

        assertThat(result).isNull();
    }

    @Test
    void shouldValidateBankAccount_MissingBankCode() {
        RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
        dto.setBankCode("");
        dto.setAccountNumber("1234567890");
        dto.setAccountHolderName("Owner");

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> bankAccountService.addBankAccount(1, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mã ngân hàng không được để trống");
    }

    @Test
    void shouldValidateBankAccount_MissingAccountNumber() {
        RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
        dto.setBankCode("970422");
        dto.setAccountNumber("");
        dto.setAccountHolderName("Owner");

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> bankAccountService.addBankAccount(1, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số tài khoản không được để trống");
    }

    @Test
    void shouldValidateBankAccount_MissingAccountHolderName() {
        RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
        dto.setBankCode("970422");
        dto.setAccountNumber("1234567890");
        dto.setAccountHolderName("");

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(1, "1234567890")).thenReturn(false);

        assertThatThrownBy(() -> bankAccountService.addBankAccount(1, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Tên chủ tài khoản không được để trống");
    }

    @Test
    void shouldValidateBankAccount_InvalidBankCode() {
        RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
        dto.setBankCode("999999"); // Invalid bank code
        dto.setAccountNumber("1234567890");
        dto.setAccountHolderName("Owner");

        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(bankAccountRepository.existsByRestaurantIdAndAccountNumber(1, "1234567890")).thenReturn(false);

        assertThatThrownBy(() -> bankAccountService.addBankAccount(1, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mã ngân hàng không hợp lệ");
    }
}

