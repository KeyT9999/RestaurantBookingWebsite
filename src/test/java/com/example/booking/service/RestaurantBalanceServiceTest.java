package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.CommissionType;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.WithdrawalRequestRepository;
import com.example.booking.exception.InsufficientBalanceException;

/**
 * Unit tests for RestaurantBalanceService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantBalanceService Tests")
public class RestaurantBalanceServiceTest {

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

    private Integer restaurantId;
    private RestaurantProfile restaurant;
    private RestaurantBalance balance;

    @BeforeEach
    void setUp() {
        restaurantId = 1;

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");

        balance = new RestaurantBalance();
        balance.setRestaurant(restaurant);
        balance.setTotalRevenue(new BigDecimal("10000000"));
        balance.setTotalBookingsCompleted(50);
        balance.setCommissionRate(new BigDecimal("5.0"));
        balance.setCommissionType(CommissionType.PERCENTAGE);
        balance.setCommissionFixedAmount(BigDecimal.ZERO);
        balance.setTotalWithdrawn(BigDecimal.ZERO);
        balance.setPendingWithdrawal(BigDecimal.ZERO);
        balance.recalculateAvailableBalance();
    }

    // ========== getBalance() Tests ==========

    @Test
    @DisplayName("shouldGetBalance_successfully")
    void shouldGetBalance_successfully() {
        // Given
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));

        // When
        com.example.booking.dto.payout.RestaurantBalanceDto result = balanceService.getBalance(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(restaurantId, result.getRestaurantId());
    }

    @Test
    @DisplayName("shouldCreateBalance_whenNotExists")
    void shouldCreateBalance_whenNotExists() {
        // Given
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.empty());
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(balanceRepository.save(any(RestaurantBalance.class))).thenReturn(balance);

        // When
        com.example.booking.dto.payout.RestaurantBalanceDto result = balanceService.getBalance(restaurantId);

        // Then
        assertNotNull(result);
        verify(balanceRepository, times(1)).save(any(RestaurantBalance.class));
    }

    // ========== getOrCreateBalance() Tests ==========

    @Test
    @DisplayName("shouldGetExistingBalance_whenExists")
    void shouldGetExistingBalance_whenExists() {
        // Given
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));

        // When
        RestaurantBalance result = balanceService.getOrCreateBalance(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(balance, result);
        verify(balanceRepository, never()).save(any(RestaurantBalance.class));
    }

    @Test
    @DisplayName("shouldCreateNewBalance_whenNotExists")
    void shouldCreateNewBalance_whenNotExists() {
        // Given
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.empty());
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(balanceRepository.save(any(RestaurantBalance.class))).thenReturn(balance);

        // When
        RestaurantBalance result = balanceService.getOrCreateBalance(restaurantId);

        // Then
        assertNotNull(result);
        verify(balanceRepository, times(1)).save(any(RestaurantBalance.class));
    }

    // ========== addRevenue() Tests ==========

    @Test
    @DisplayName("shouldAddRevenue_successfully")
    void shouldAddRevenue_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));
        when(balanceRepository.save(balance)).thenReturn(balance);

        // When
        balanceService.addRevenue(restaurantId, amount);

        // Then
        verify(balanceRepository, times(1)).save(balance);
    }

    // ========== lockBalance() Tests ==========

    @Test
    @DisplayName("shouldLockBalance_successfully")
    void shouldLockBalance_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));
        when(balanceRepository.save(balance)).thenReturn(balance);

        // When
        balanceService.lockBalance(restaurantId, amount);

        // Then
        verify(balanceRepository, times(1)).save(balance);
    }

    @Test
    @DisplayName("shouldThrowException_whenInsufficientBalance")
    void shouldThrowException_whenInsufficientBalance() {
        // Given
        BigDecimal amount = new BigDecimal("999999999"); // Too large
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));

        // When & Then
        assertThrows(InsufficientBalanceException.class, () -> {
            balanceService.lockBalance(restaurantId, amount);
        });
    }

    // ========== unlockBalance() Tests ==========

    @Test
    @DisplayName("shouldUnlockBalance_successfully")
    void shouldUnlockBalance_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));
        when(balanceRepository.save(balance)).thenReturn(balance);

        // When
        balanceService.unlockBalance(restaurantId, amount);

        // Then
        verify(balanceRepository, times(1)).save(balance);
    }

    // ========== confirmWithdrawal() Tests ==========

    @Test
    @DisplayName("shouldConfirmWithdrawal_successfully")
    void shouldConfirmWithdrawal_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));
        when(balanceRepository.save(balance)).thenReturn(balance);

        // When
        balanceService.confirmWithdrawal(restaurantId, amount);

        // Then
        verify(balanceRepository, times(1)).save(balance);
    }

    // ========== fixBalanceFromWithdrawals() Tests ==========

    @Test
    @DisplayName("shouldFixBalanceFromWithdrawals_successfully")
    void shouldFixBalanceFromWithdrawals_successfully() {
        // Given
        BigDecimal totalWithdrawn = new BigDecimal("5000000");
        BigDecimal pendingWithdrawal = new BigDecimal("1000000");
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));
        when(withdrawalRepository.sumAmountByRestaurantIdAndStatus(restaurantId, 
            com.example.booking.common.enums.WithdrawalStatus.SUCCEEDED)).thenReturn(totalWithdrawn);
        when(withdrawalRepository.sumAmountByRestaurantIdAndStatus(restaurantId, 
            com.example.booking.common.enums.WithdrawalStatus.PENDING)).thenReturn(pendingWithdrawal);
        when(withdrawalRepository.countByRestaurantRestaurantIdAndStatus(restaurantId, 
            com.example.booking.common.enums.WithdrawalStatus.PENDING)).thenReturn(1L);
        when(balanceRepository.save(balance)).thenReturn(balance);

        // When
        balanceService.fixBalanceFromWithdrawals(restaurantId);

        // Then
        verify(balanceRepository, times(1)).save(balance);
    }

    // ========== Commission Methods Tests ==========

    @Test
    @DisplayName("shouldGetCommissionToday_successfully")
    void shouldGetCommissionToday_successfully() {
        // Given
        BigDecimal gross = new BigDecimal("10000000");
        when(bookingRepository.sumDepositByStatusAndCreatedBetween(
            eq(BookingStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(gross);

        // When
        BigDecimal result = balanceService.getCommissionToday();

        // Then
        assertNotNull(result);
        // Commission = 10000000 * 0.30 = 3000000
        assertEquals(new BigDecimal("3000000"), result);
    }

    @Test
    @DisplayName("shouldGetCompletedBookingsToday_successfully")
    void shouldGetCompletedBookingsToday_successfully() {
        // Given
        when(bookingRepository.countByStatusAndBookingTimeBetween(
            eq(BookingStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(10L);

        // When
        long result = balanceService.getCompletedBookingsToday();

        // Then
        assertEquals(10L, result);
    }

    @Test
    @DisplayName("shouldGetWeeklyCommission_successfully")
    void shouldGetWeeklyCommission_successfully() {
        // Given
        BigDecimal gross = new BigDecimal("70000000");
        when(bookingRepository.sumDepositByStatusAndCreatedBetween(
            eq(BookingStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(gross);

        // When
        BigDecimal result = balanceService.getWeeklyCommission();

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("shouldGetMonthlyCommission_successfully")
    void shouldGetMonthlyCommission_successfully() {
        // Given
        BigDecimal gross = new BigDecimal("300000000");
        when(bookingRepository.sumDepositByStatusAndCreatedBetween(
            eq(BookingStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(gross);

        // When
        BigDecimal result = balanceService.getMonthlyCommission();

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("shouldGetTotalCommission_successfully")
    void shouldGetTotalCommission_successfully() {
        // Given
        BigDecimal gross = new BigDecimal("100000000");
        when(bookingRepository.sumDepositByStatus(BookingStatus.COMPLETED)).thenReturn(gross);

        // When
        BigDecimal result = balanceService.getTotalCommission();

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("30000000"), result); // 30% of 100M
    }

    @Test
    @DisplayName("shouldGetAverageCommissionPerBooking_successfully")
    void shouldGetAverageCommissionPerBooking_successfully() {
        // Given
        BigDecimal gross = new BigDecimal("100000000");
        when(bookingRepository.sumDepositByStatus(BookingStatus.COMPLETED)).thenReturn(gross);
        when(bookingRepository.countByStatus(BookingStatus.COMPLETED)).thenReturn(100L);

        // When
        BigDecimal result = balanceService.getAverageCommissionPerBooking();

        // Then
        assertNotNull(result);
        // Total commission = 30M, divided by 100 = 300K per booking
    }

    @Test
    @DisplayName("shouldGetCommissionRate_successfully")
    void shouldGetCommissionRate_successfully() {
        // When
        BigDecimal result = balanceService.getCommissionRate();

        // Then
        assertEquals(new BigDecimal("30.0"), result); // 0.30 * 100
    }

    // ========== Helper Methods Tests ==========

    @Test
    @DisplayName("shouldGetBalanceByRestaurantId_successfully")
    void shouldGetBalanceByRestaurantId_successfully() {
        // Given
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.of(balance));

        // When
        RestaurantBalance result = balanceService.getBalanceByRestaurantId(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(balance, result);
    }

    @Test
    @DisplayName("shouldSaveBalance_successfully")
    void shouldSaveBalance_successfully() {
        // Given
        when(balanceRepository.save(balance)).thenReturn(balance);

        // When
        RestaurantBalance result = balanceService.saveBalance(balance);

        // Then
        assertNotNull(result);
        verify(balanceRepository, times(1)).save(balance);
    }

    @Test
    @DisplayName("shouldRecalculateAll_successfully")
    void shouldRecalculateAll_successfully() {
        // Given
        doNothing().when(balanceRepository).recalculateAllBalances();

        // When
        balanceService.recalculateAll();

        // Then
        verify(balanceRepository, times(1)).recalculateAllBalances();
    }
}

