package com.example.booking.service;

import com.example.booking.common.enums.CommissionType;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.WithdrawalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantBalanceServiceTest {

    @Mock
    private RestaurantBalanceRepository balanceRepository;

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @Mock
    private WithdrawalRequestRepository withdrawalRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RestaurantBalanceService balanceService;

    private RestaurantProfile restaurant;
    private RestaurantBalance balance;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        balance = new RestaurantBalance();
        balance.setBalanceId(1);
        balance.setRestaurant(restaurant);
        balance.setTotalRevenue(new BigDecimal("10000000"));
        balance.setTotalBookingsCompleted(50);
        balance.setCommissionRate(new BigDecimal("5.0"));
        balance.setCommissionType(CommissionType.PERCENTAGE);
        balance.setCommissionFixedAmount(BigDecimal.ZERO);
        balance.setTotalCommission(new BigDecimal("500000"));
        balance.setTotalWithdrawn(new BigDecimal("200000"));
        balance.setPendingWithdrawal(new BigDecimal("100000"));
    }

    @Test
    void shouldGetBalance_ExistingBalance() {
        when(balanceRepository.findByRestaurantRestaurantId(1)).thenReturn(Optional.of(balance));

        RestaurantBalanceDto result = balanceService.getBalance(1);

        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo(1);
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("10000000"));
        assertThat(result.getTotalBookingsCompleted()).isEqualTo(50);
    }

    @Test
    void shouldGetBalance_CreateNewBalance() {
        when(balanceRepository.findByRestaurantRestaurantId(1)).thenReturn(Optional.empty());
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(balanceRepository.save(any(RestaurantBalance.class))).thenReturn(balance);

        RestaurantBalanceDto result = balanceService.getBalance(1);

        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo(1);
        verify(balanceRepository).save(any(RestaurantBalance.class));
    }

    @Test
    void shouldGetBalance_RestaurantNotFound() {
        when(balanceRepository.findByRestaurantRestaurantId(1)).thenReturn(Optional.empty());
        when(restaurantRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> balanceService.getBalance(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy nhà hàng");
    }

    @Test
    void shouldAddRevenue() {
        when(balanceRepository.findByRestaurantRestaurantId(1)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any(RestaurantBalance.class))).thenReturn(balance);

        BigDecimal amount = new BigDecimal("500000");
        balanceService.addRevenue(1, amount);

        verify(balanceRepository).save(any(RestaurantBalance.class));
    }

    @Test
    void shouldLockBalance() {
        when(balanceRepository.findByRestaurantRestaurantId(1)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any(RestaurantBalance.class))).thenReturn(balance);

        BigDecimal amount = new BigDecimal("100000");
        balanceService.lockBalance(1, amount);

        verify(balanceRepository).save(any(RestaurantBalance.class));
    }

    @Test
    void shouldUnlockBalance() {
        when(balanceRepository.findByRestaurantRestaurantId(1)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any(RestaurantBalance.class))).thenReturn(balance);

        BigDecimal amount = new BigDecimal("100000");
        balanceService.unlockBalance(1, amount);

        verify(balanceRepository).save(any(RestaurantBalance.class));
    }

    @Test
    void shouldConfirmWithdrawal() {
        when(balanceRepository.findByRestaurantRestaurantId(1)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any(RestaurantBalance.class))).thenReturn(balance);

        BigDecimal amount = new BigDecimal("500000");
        balanceService.confirmWithdrawal(1, amount);

        verify(balanceRepository).save(any(RestaurantBalance.class));
    }

    @Test
    void shouldFixBalanceFromWithdrawals() {
        when(balanceRepository.findByRestaurantRestaurantId(1)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any(RestaurantBalance.class))).thenReturn(balance);
        when(withdrawalRepository.sumAmountByRestaurantIdAndStatus(eq(1), any())).thenReturn(BigDecimal.ZERO);
        when(withdrawalRepository.countByRestaurantRestaurantIdAndStatus(eq(1), any())).thenReturn(0L);

        balanceService.fixBalanceFromWithdrawals(1);

        verify(balanceRepository).save(any(RestaurantBalance.class));
    }

    @Test
    void shouldRecalculateAll() {
        doNothing().when(balanceRepository).recalculateAllBalances();

        balanceService.recalculateAll();

        verify(balanceRepository).recalculateAllBalances();
    }
}

