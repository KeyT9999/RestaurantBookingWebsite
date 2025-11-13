package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantBankAccount;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.WithdrawalRequest;
import com.example.booking.dto.admin.RestaurantBalanceInfoDto;
import com.example.booking.dto.admin.WithdrawalStatsDto;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.ManualPayDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantBankAccountRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.WithdrawalRequestRepository;

/**
 * Service để quản lý withdrawal requests
 */
@Service
public class WithdrawalService {
    
    private static final Logger logger = LoggerFactory.getLogger(WithdrawalService.class);
    private static final BigDecimal MIN_WITHDRAWAL_AMOUNT = new BigDecimal("100000"); // 100k VNĐ
    private static final int MAX_WITHDRAWALS_PER_DAY = 3;
    
    private final WithdrawalRequestRepository withdrawalRepository;
    private final RestaurantBankAccountRepository bankAccountRepository;
    private final RestaurantProfileRepository restaurantRepository;
    private final RestaurantBalanceRepository balanceRepository;
    
    private final RestaurantBalanceService balanceService;
    private final WithdrawalNotificationService notificationService;
    
    
    public WithdrawalService(
        WithdrawalRequestRepository withdrawalRepository,
        RestaurantBankAccountRepository bankAccountRepository,
        RestaurantProfileRepository restaurantRepository,
        RestaurantBalanceRepository balanceRepository,
        RestaurantBalanceService balanceService,
        WithdrawalNotificationService notificationService
    ) {
        this.withdrawalRepository = withdrawalRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.restaurantRepository = restaurantRepository;
        this.balanceRepository = balanceRepository;
        this.balanceService = balanceService;
        this.notificationService = notificationService;
    }
    
    /**
     * Tạo yêu cầu rút tiền
     */
    @Transactional
    public WithdrawalRequestDto createWithdrawal(Integer restaurantId, CreateWithdrawalRequestDto dto) {
        // Validate
        validateWithdrawalRequest(restaurantId, dto);
        
        // Get entities
        RestaurantProfile restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà hàng"));
        
        RestaurantBankAccount bankAccount = bankAccountRepository.findById(dto.getBankAccountId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ngân hàng"));
        
        if (!bankAccount.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new BadRequestException("Tài khoản ngân hàng không thuộc nhà hàng này");
        }
        
        // Check balance
        RestaurantBalance balance = balanceService.getOrCreateBalance(restaurantId);
        if (!balance.hasEnoughBalance(dto.getAmount())) {
            throw new BadRequestException(
                String.format("Số dư không đủ. Khả dụng: %s VNĐ", balance.getAvailableBalance())
            );
        }
        
        // Create withdrawal request
        WithdrawalRequest request = new WithdrawalRequest();
        request.setRestaurant(restaurant);
        request.setBankAccount(bankAccount);
        request.setAmount(dto.getAmount());
        request.setDescription(dto.getDescription() != null ? dto.getDescription() : "Rút tiền về tài khoản ngân hàng");
        request.setStatus(WithdrawalStatus.PENDING);
        
        // Calculate commission (if any)
        BigDecimal commission = BigDecimal.ZERO; // For now, no commission on withdrawal
        request.setCommissionAmount(commission);
        request.setNetAmount(dto.getAmount().subtract(commission));
        
        request = withdrawalRepository.save(request);
        withdrawalRepository.flush(); // Đảm bảo withdrawal request được insert trước khi audit
        logger.info("📝 Created withdrawal request {} for restaurant {} - Amount: {}", 
            request.getRequestId(), restaurantId, dto.getAmount());
        
        // Send notification
        try {
            notificationService.notifyWithdrawalStatusChanged(request, null);
        } catch (Exception e) {
            logger.error("Failed to send notification for withdrawal created", e);
        }
        
        return convertToDto(request);
    }
    
    /**
     * Approve withdrawal (Admin only)
     * Uses pessimistic locking to prevent race conditions
     */
    @Transactional
    public WithdrawalRequestDto approveWithdrawal(Integer requestId, UUID adminUserId, String notes) {
        // Step 1: Lock withdrawal request (FOR UPDATE)
        WithdrawalRequest request = withdrawalRepository.findByIdForUpdate(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu rút tiền"));
        
        if (!request.canBeApproved()) {
            throw new BadRequestException("Yêu cầu không thể duyệt ở trạng thái hiện tại: " + request.getStatus());
        }
        
        // Step 2: Lock restaurant balance (FOR UPDATE)
        RestaurantBalance balance = balanceRepository.findByRestaurantIdForUpdate(
            request.getRestaurant().getRestaurantId()
        ).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy số dư nhà hàng"));
        
        // Step 3: Recalculate and validate available balance
        balance.recalculateAvailableBalance();
        if (!balance.hasEnoughBalance(request.getAmount())) {
            throw new BadRequestException(
                String.format("Số dư không đủ. Khả dụng: %s VNĐ, Yêu cầu: %s VNĐ", 
                    balance.getAvailableBalance(), request.getAmount())
            );
        }
        
        // Step 4: Lock pending withdrawal
        balance.setPendingWithdrawal(
            balance.getPendingWithdrawal().add(request.getAmount())
        );
        balance.recalculateAvailableBalance();
        balanceRepository.save(balance);
        
        logger.info("🔒 Locked {} VNĐ for withdrawal request {}", request.getAmount(), requestId);
        
        // Step 5: Approve request
        WithdrawalStatus oldStatus = request.getStatus();
        request.approve(adminUserId, notes);
        request.setStatus(WithdrawalStatus.APPROVED);
        withdrawalRepository.save(request);
        
        logger.info("✅ Approved withdrawal request {} by admin {}", requestId, adminUserId);
        
        // Send notification
        try {
            notificationService.notifyWithdrawalStatusChanged(request, oldStatus);
        } catch (Exception e) {
            logger.error("Failed to send notification for withdrawal approved", e);
        }
        
        // Note: No PayOS payout transaction needed for manual process
        // Admin will manually transfer money and use markWithdrawalPaid() method
        
        return convertToDto(request);
    }
    
    /**
     * Reject withdrawal (Admin only)
     * Uses pessimistic locking to prevent race conditions
     */
    @Transactional
    public WithdrawalRequestDto rejectWithdrawal(Integer requestId, UUID adminUserId, String reason) {
        // Lock withdrawal request (FOR UPDATE)
        WithdrawalRequest request = withdrawalRepository.findByIdForUpdate(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu rút tiền"));
        
        if (!request.canBeRejected()) {
            throw new BadRequestException("Yêu cầu không thể từ chối ở trạng thái hiện tại: " + request.getStatus());
        }
        
        // Reject request
        WithdrawalStatus oldStatus = request.getStatus();
        request.reject(adminUserId, reason);
        withdrawalRepository.save(request);
        
        logger.info("❌ Rejected withdrawal request {} by admin {}: {}", requestId, adminUserId, reason);
        
        // Send notification
        try {
            notificationService.notifyWithdrawalStatusChanged(request, oldStatus);
        } catch (Exception e) {
            logger.error("Failed to send notification for withdrawal rejected", e);
        }
        
        return convertToDto(request);
    }
    
    /**
     * Get withdrawal by ID
     */
    @Transactional(readOnly = true)
    public WithdrawalRequestDto getWithdrawal(Integer requestId) {
        WithdrawalRequest request = withdrawalRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu rút tiền"));
        return convertToDto(request);
    }
    
    /**
     * Get withdrawals by restaurant
     */
    @Transactional(readOnly = true)
    public Page<WithdrawalRequestDto> getWithdrawalsByRestaurant(Integer restaurantId, Pageable pageable) {
        return withdrawalRepository.findByRestaurantRestaurantId(restaurantId, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Get all pending requests (Admin)
     */
    @Transactional(readOnly = true)
    public Page<WithdrawalRequestDto> getPendingRequests(Pageable pageable) {
        return withdrawalRepository.findPendingRequests(pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Get all withdrawals (Admin)
     */
    @Transactional(readOnly = true)
    public Page<WithdrawalRequestDto> getAllWithdrawals(Pageable pageable) {
        return withdrawalRepository.findAll(pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Get withdrawals by status (Admin)
     */
    @Transactional(readOnly = true)
    public List<WithdrawalRequestDto> getWithdrawalsByStatus(WithdrawalStatus status) {
        return withdrawalRepository.findByStatus(status, Pageable.unpaged())
            .getContent()
            .stream()
            .map(this::convertToDto)
            .collect(java.util.stream.Collectors.toList());
    }
    
    
    /**
     * Validate withdrawal request
     */
    private void validateWithdrawalRequest(Integer restaurantId, CreateWithdrawalRequestDto dto) {
        // Check minimum amount
        if (dto.getAmount().compareTo(MIN_WITHDRAWAL_AMOUNT) < 0) {
            throw new BadRequestException(
                String.format("Số tiền rút tối thiểu là %s VNĐ", MIN_WITHDRAWAL_AMOUNT)
            );
        }
        
        // Check daily limit (count PENDING, APPROVED, PROCESSING, SUCCEEDED)
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrow = today.plusDays(1);
        
        // Check daily limit (count PENDING, SUCCEEDED)
        
        long todayCount = withdrawalRepository.countByRestaurantIdAndDateRange(
            restaurantId, today, tomorrow
        );
        
        if (todayCount >= MAX_WITHDRAWALS_PER_DAY) {
            throw new BadRequestException(
                String.format("Đã đạt giới hạn %d lần rút tiền/ngày", MAX_WITHDRAWALS_PER_DAY)
            );
        }
        
        // Check if restaurant has enough available balance
        RestaurantBalance balance = balanceService.getOrCreateBalance(restaurantId);
        balance.recalculateAvailableBalance();
        
        if (!balance.hasEnoughBalance(dto.getAmount())) {
            throw new BadRequestException(
                String.format("Số dư không đủ. Khả dụng: %s VNĐ, Yêu cầu: %s VNĐ", 
                    balance.getAvailableBalance(), dto.getAmount())
            );
        }
    }
    
    /**
     * Get withdrawal statistics (Admin)
     */
    @Transactional(readOnly = true)
    public WithdrawalStatsDto getWithdrawalStats() {
        Long pendingCount = withdrawalRepository.countByStatus(WithdrawalStatus.PENDING);
        Long succeededCount = withdrawalRepository.countByStatus(WithdrawalStatus.SUCCEEDED);
        Long rejectedCount = withdrawalRepository.countByStatus(WithdrawalStatus.REJECTED);
        
        BigDecimal pendingAmount = withdrawalRepository.sumAmountByStatus(WithdrawalStatus.PENDING);
        BigDecimal succeededAmount = withdrawalRepository.sumAmountByStatus(WithdrawalStatus.SUCCEEDED);
        BigDecimal totalCommission = withdrawalRepository.sumCommissionByStatus(WithdrawalStatus.SUCCEEDED);
        
        // Calculate average processing time
        Double avgHours = withdrawalRepository.calculateAverageProcessingTimeHours();
        
        // Calculate success rate
        Long totalCompleted = succeededCount + rejectedCount;
        Double successRate = totalCompleted > 0 ? (succeededCount.doubleValue() / totalCompleted * 100) : 0.0;
        
        return new WithdrawalStatsDto(
            pendingCount, 0L, succeededCount, 0L, rejectedCount,
            pendingAmount != null ? pendingAmount : BigDecimal.ZERO,
            BigDecimal.ZERO,
            succeededAmount != null ? succeededAmount : BigDecimal.ZERO,
            totalCommission != null ? totalCommission : BigDecimal.ZERO,
            avgHours, successRate
        );
    }
    
    
    /**
     * Get all restaurant balances (Admin)
     */
    @Transactional(readOnly = true)
    public Page<RestaurantBalanceInfoDto> getAllRestaurantBalances(String search, Pageable pageable) {
        Page<RestaurantBalance> balances;
        
        if (search != null && !search.isEmpty()) {
            balances = balanceRepository.findByRestaurantNameContaining(search, pageable);
        } else {
            balances = balanceRepository.findAll(pageable);
        }
        
        return balances.map(this::convertBalanceToDto);
    }
    
    /**
     * Convert entity to DTO
     */
    private WithdrawalRequestDto convertToDto(WithdrawalRequest request) {
        WithdrawalRequestDto dto = new WithdrawalRequestDto();
        
        dto.setRequestId(request.getRequestId());
        dto.setRestaurantId(request.getRestaurant().getRestaurantId());
        dto.setRestaurantName(request.getRestaurant().getRestaurantName());
        dto.setBankAccountId(request.getBankAccount().getAccountId());
        dto.setAmount(request.getAmount());
        dto.setDescription(request.getDescription());
        dto.setStatus(request.getStatus());
        dto.setReviewedAt(request.getReviewedAt());
        dto.setRejectionReason(request.getRejectionReason());
        dto.setAdminNotes(request.getAdminNotes());
        dto.setCommissionAmount(request.getCommissionAmount());
        dto.setNetAmount(request.getNetAmount());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        
        // Owner information
        if (request.getRestaurant().getOwner() != null) {
            User ownerUser = request.getRestaurant().getOwner().getUser();
            if (ownerUser != null) {
                dto.setOwnerName(ownerUser.getFullName());
                dto.setOwnerEmail(ownerUser.getEmail());
            }
        }
        
        // Bank account information
        if (request.getBankAccount() != null) {
            dto.setBankAccountNumber(request.getBankAccount().getAccountNumber());
            dto.setAccountHolderName(request.getBankAccount().getAccountHolderName());
            dto.setBankCode(request.getBankAccount().getBankCode());
            dto.setBankName(request.getBankAccount().getBankName());
        }
        
        return dto;
    }
    
    
    /**
     * Convert RestaurantBalance to DTO with owner info
     */
    private RestaurantBalanceInfoDto convertBalanceToDto(RestaurantBalance balance) {
        RestaurantBalanceInfoDto dto = new RestaurantBalanceInfoDto();
        
        RestaurantProfile restaurant = balance.getRestaurant();
        dto.setRestaurantId(restaurant.getRestaurantId());
        dto.setRestaurantName(restaurant.getRestaurantName());
        
        // Get owner info
        if (restaurant.getOwner() != null) {
            User ownerUser = restaurant.getOwner().getUser();
            if (ownerUser != null) {
                dto.setOwnerEmail(ownerUser.getEmail());
                dto.setOwnerPhone(ownerUser.getPhoneNumber());
            }
        }
        
        dto.setTotalRevenue(balance.getTotalRevenue());
        dto.setAvailableBalance(balance.getAvailableBalance());
        dto.setPendingWithdrawal(balance.getPendingWithdrawal());
        dto.setTotalWithdrawn(balance.getTotalWithdrawn());
        dto.setTotalCommission(balance.getTotalCommission());
        
        dto.setTotalBookingsCompleted(balance.getTotalBookingsCompleted() != null ? balance.getTotalBookingsCompleted().longValue() : 0L);
        dto.setTotalWithdrawalRequests(balance.getTotalWithdrawalRequests() != null ? balance.getTotalWithdrawalRequests().longValue() : 0L);
        
        dto.setLastWithdrawalAt(balance.getLastWithdrawalAt());
        dto.setLastCalculatedAt(balance.getLastCalculatedAt());
        
        return dto;
    }
    
    /**
     * Mark withdrawal as paid (manual transfer process)
     * Replaces the PayOS automated payout process
     */
    @Transactional
    public void markWithdrawalPaid(Integer requestId, UUID adminId, ManualPayDto dto) {
        logger.info("Marking withdrawal {} as paid by admin {}", requestId, adminId);
        
        // 1) Lock request + balance
        WithdrawalRequest request = withdrawalRepository.findByIdForUpdate(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu rút tiền"));
        
        if (request.getStatus() != WithdrawalStatus.PENDING && request.getStatus() != WithdrawalStatus.APPROVED) {
            throw new IllegalStateException("Yêu cầu không ở trạng thái có thể đánh dấu đã chi: " + request.getStatus());
        }
        
        RestaurantBalance balance = balanceRepository.findByRestaurantIdForUpdate(request.getRestaurant().getRestaurantId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy số dư nhà hàng"));
        
        // 2) Validate business rules
        validateWithdrawalAmount(request.getAmount());
        
        if (balance.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Số dư không đủ để rút: " + balance.getAvailableBalance() + " < " + request.getAmount());
        }
        
        // 3) Update manual transfer information
        request.setManualTransferRef(dto.getTransferRef());
        request.setManualTransferredAt(LocalDateTime.now());
        request.setManualTransferredBy(adminId);
        request.setManualNote(dto.getNote());
        request.setManualProofUrl(dto.getProofUrl());
        
        // 4) Set status to SUCCEEDED and update timestamps
        WithdrawalStatus oldStatus = request.getStatus();
        request.setStatus(WithdrawalStatus.SUCCEEDED);
        request.setReviewedByUserId(adminId);
        request.setReviewedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        withdrawalRepository.save(request);
        
        // 5) Update balance (trigger will handle the balance updates)
        logger.info("Withdrawal {} marked as paid successfully", requestId);
        
        // 6) Send notification
        try {
            notificationService.notifyWithdrawalStatusChanged(request, oldStatus);
        } catch (Exception e) {
            logger.error("Failed to send notification for withdrawal succeeded", e);
        }
        
        // Log manual transfer
        logger.info("Manual transfer confirmed for withdrawal {}: {}", requestId, dto.getTransferRef());
    }
    
    /**
     * Validate withdrawal amount
     */
    private void validateWithdrawalAmount(BigDecimal amount) {
        if (amount.compareTo(MIN_WITHDRAWAL_AMOUNT) < 0) {
            throw new BadRequestException(
                String.format("Số tiền rút tối thiểu là %s VNĐ", MIN_WITHDRAWAL_AMOUNT)
            );
        }
    }
    
    // ============== ADMIN DASHBOARD STATISTICS METHODS ==============
    
    /**
     * Get total pending withdrawal amount
     */
    public BigDecimal getTotalPendingAmount() {
        return withdrawalRepository.sumAmountByStatus(WithdrawalStatus.PENDING);
    }
    
    /**
     * Get total withdrawn amount
     */
    public BigDecimal getTotalWithdrawnAmount() {
        return withdrawalRepository.sumAmountByStatus(WithdrawalStatus.SUCCEEDED);
    }
    
    /**
     * Get top restaurants by withdrawal amount
     */
    public List<RestaurantBalanceInfoDto> getTopRestaurantsByWithdrawal(int limit) {
        return balanceRepository.findTopRestaurantsByWithdrawal(limit)
            .stream()
            .map(this::convertToRestaurantBalanceInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get monthly withdrawal statistics for chart
     */
    public Map<String, Object> getMonthlyWithdrawalStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get last 6 months data
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        
        // Monthly withdrawal amounts
        List<Object[]> monthlyData = withdrawalRepository.getMonthlyWithdrawalStats(sixMonthsAgo);
        
        List<String> months = monthlyData.stream()
            .map(row -> row[0].toString())
            .collect(Collectors.toList());
            
        List<BigDecimal> amounts = monthlyData.stream()
            .map(row -> (BigDecimal) row[1])
            .collect(Collectors.toList());
        
        stats.put("months", months);
        stats.put("amounts", amounts);
        
        return stats;
    }
    
    /**
     * Get total commission earned
     */
    public BigDecimal getTotalCommissionEarned() {
        return balanceRepository.getTotalCommissionEarned();
    }
    
    /**
     * Convert RestaurantBalance to RestaurantBalanceInfoDto
     */
    private RestaurantBalanceInfoDto convertToRestaurantBalanceInfoDto(RestaurantBalance balance) {
        RestaurantBalanceInfoDto dto = new RestaurantBalanceInfoDto();
        dto.setRestaurantId(balance.getRestaurant().getRestaurantId());
        dto.setRestaurantName(balance.getRestaurant().getRestaurantName());
        dto.setTotalRevenue(balance.getTotalRevenue());
        dto.setTotalWithdrawn(balance.getTotalWithdrawn());
        dto.setPendingWithdrawal(balance.getPendingWithdrawal());
        dto.setAvailableBalance(balance.getAvailableBalance());
        dto.setTotalCommission(balance.getTotalCommission());
        return dto;
    }
}

