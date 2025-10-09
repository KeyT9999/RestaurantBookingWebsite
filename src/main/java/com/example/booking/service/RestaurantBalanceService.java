package com.example.booking.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.WithdrawalRequestRepository;

/**
 * Service để quản lý số dư nhà hàng
 */
@Service
public class RestaurantBalanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantBalanceService.class);
    
    private final RestaurantBalanceRepository balanceRepository;
    private final RestaurantProfileRepository restaurantRepository;
    private final WithdrawalRequestRepository withdrawalRepository;
    
    public RestaurantBalanceService(
        RestaurantBalanceRepository balanceRepository,
        RestaurantProfileRepository restaurantRepository,
        WithdrawalRequestRepository withdrawalRepository
    ) {
        this.balanceRepository = balanceRepository;
        this.restaurantRepository = restaurantRepository;
        this.withdrawalRepository = withdrawalRepository;
    }
    
    /**
     * Lấy số dư của nhà hàng
     */
    @Transactional
    public RestaurantBalanceDto getBalance(Integer restaurantId) {
        RestaurantBalance balance = getOrCreateBalance(restaurantId);
        
        // Không recalculate - để database trigger tự động cập nhật
        // balance.recalculateAvailableBalance();
        // balanceRepository.save(balance);
        
        return convertToDto(balance);
    }
    
    /**
     * Lấy hoặc tạo mới balance record
     */
    @Transactional
    public RestaurantBalance getOrCreateBalance(Integer restaurantId) {
        Optional<RestaurantBalance> existingBalance = balanceRepository.findByRestaurantRestaurantId(restaurantId);
        
        if (existingBalance.isPresent()) {
            return existingBalance.get();
        }
        
        // Create new balance with default test data
        RestaurantProfile restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà hàng"));
        
        RestaurantBalance newBalance = new RestaurantBalance();
        newBalance.setRestaurant(restaurant);
        
        // Set default test data for development
        newBalance.setTotalRevenue(new BigDecimal("10000000")); // 10 triệu VNĐ
        newBalance.setTotalBookingsCompleted(50);
        newBalance.setCommissionRate(new BigDecimal("5.0")); // 5%
        newBalance.setCommissionType(com.example.booking.common.enums.CommissionType.PERCENTAGE);
        newBalance.setCommissionFixedAmount(BigDecimal.ZERO);
        
        newBalance.recalculateAvailableBalance();
        
        logger.info("📊 Created new balance for restaurant {} with test data: {} VNĐ", 
            restaurantId, newBalance.getAvailableBalance());
        
        return balanceRepository.save(newBalance);
    }
    
    /**
     * Cập nhật doanh thu khi có booking completed
     */
    @Transactional
    public void addRevenue(Integer restaurantId, BigDecimal amount) {
        RestaurantBalance balance = getOrCreateBalance(restaurantId);
        balance.addRevenue(amount);
        balanceRepository.save(balance);
        
        logger.info("💰 Added revenue {} for restaurant {}", amount, restaurantId);
    }
    
    /**
     * Lock số dư khi có yêu cầu rút tiền pending
     */
    @Transactional
    public void lockBalance(Integer restaurantId, BigDecimal amount) {
        RestaurantBalance balance = getOrCreateBalance(restaurantId);
        
        if (!balance.hasEnoughBalance(amount)) {
            throw new InsufficientBalanceException(
                String.format("Số dư không đủ. Khả dụng: %s, Yêu cầu: %s", 
                    balance.getAvailableBalance(), amount)
            );
        }
        
        balance.lockBalance(amount);
        balanceRepository.save(balance);
        
        logger.info("🔒 Locked balance {} for restaurant {}", amount, restaurantId);
    }
    
    /**
     * Unlock số dư khi withdrawal bị reject/cancel
     */
    @Transactional
    public void unlockBalance(Integer restaurantId, BigDecimal amount) {
        RestaurantBalance balance = getOrCreateBalance(restaurantId);
        balance.unlockBalance(amount);
        balanceRepository.save(balance);
        
        logger.info("🔓 Unlocked balance {} for restaurant {}", amount, restaurantId);
    }
    
    /**
     * Xác nhận withdrawal thành công
     */
    @Transactional
    public void confirmWithdrawal(Integer restaurantId, BigDecimal amount) {
        RestaurantBalance balance = getOrCreateBalance(restaurantId);
        balance.confirmWithdrawal(amount);
        balanceRepository.save(balance);
        
        logger.info("✅ Confirmed withdrawal {} for restaurant {}", amount, restaurantId);
    }
    
    /**
     * Fix balance dựa trên withdrawal requests thực tế
     */
    @Transactional
    public void fixBalanceFromWithdrawals(Integer restaurantId) {
        RestaurantBalance balance = getOrCreateBalance(restaurantId);
        
        // Tính toán từ withdrawal requests thực tế
        BigDecimal totalWithdrawn = withdrawalRepository.sumAmountByRestaurantIdAndStatus(
            restaurantId, com.example.booking.common.enums.WithdrawalStatus.SUCCEEDED);
        BigDecimal pendingWithdrawal = withdrawalRepository.sumAmountByRestaurantIdAndStatus(
            restaurantId, com.example.booking.common.enums.WithdrawalStatus.PENDING);
        Integer totalRequests = (int) withdrawalRepository.countByRestaurantRestaurantIdAndStatus(
            restaurantId, com.example.booking.common.enums.WithdrawalStatus.PENDING);
        
        // Cập nhật balance
        balance.setTotalWithdrawn(totalWithdrawn != null ? totalWithdrawn : BigDecimal.ZERO);
        balance.setPendingWithdrawal(pendingWithdrawal != null ? pendingWithdrawal : BigDecimal.ZERO);
        balance.setTotalWithdrawalRequests(totalRequests != null ? totalRequests : 0);
        
        // Recalculate available balance
        balance.recalculateAvailableBalance();
        balanceRepository.save(balance);
        
        logger.info("🔧 Fixed balance for restaurant {}: withdrawn={}, pending={}, available={}", 
            restaurantId, balance.getTotalWithdrawn(), balance.getPendingWithdrawal(), balance.getAvailableBalance());
    }
    
    /**
     * Recalculate tất cả balances
     */
    @Transactional
    public void recalculateAll() {
        balanceRepository.recalculateAllBalances();
        logger.info("🔄 Recalculated all restaurant balances");
    }
    
    /**
     * Convert entity to DTO
     */
    private RestaurantBalanceDto convertToDto(RestaurantBalance balance) {
        RestaurantBalanceDto dto = new RestaurantBalanceDto();
        
        dto.setRestaurantId(balance.getRestaurant().getRestaurantId());
        dto.setRestaurantName(balance.getRestaurant().getRestaurantName());
        
        dto.setTotalRevenue(balance.getTotalRevenue());
        dto.setTotalBookingsCompleted(balance.getTotalBookingsCompleted());
        
        dto.setCommissionType(balance.getCommissionType());
        dto.setCommissionRate(balance.getCommissionRate());
        dto.setCommissionFixedAmount(balance.getCommissionFixedAmount());
        dto.setTotalCommission(balance.getTotalCommission());
        
        dto.setTotalWithdrawn(balance.getTotalWithdrawn());
        dto.setPendingWithdrawal(balance.getPendingWithdrawal());
        dto.setTotalWithdrawalRequests(balance.getTotalWithdrawalRequests());
        
        dto.setAvailableBalance(balance.getAvailableBalance());
        dto.setCanWithdraw(balance.hasEnoughBalance(dto.getMinimumWithdrawal()));
        
        dto.setLastCalculatedAt(balance.getLastCalculatedAt());
        dto.setLastWithdrawalAt(balance.getLastWithdrawalAt());
        
        return dto;
    }
    
    /**
     * Custom exception
     */
    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }
}

