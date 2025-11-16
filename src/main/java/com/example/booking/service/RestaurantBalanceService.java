package com.example.booking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.exception.InsufficientBalanceException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.dto.analytics.CommissionSeriesPoint;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.WithdrawalRequestRepository;
import com.example.booking.repository.PaymentRepository;

/**
 * Service để quản lý số dư nhà hàng
 */
@Service
public class RestaurantBalanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantBalanceService.class);
    
    private final RestaurantBalanceRepository balanceRepository;
    private final RestaurantProfileRepository restaurantRepository;
    private final WithdrawalRequestRepository withdrawalRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.07"); // 7% của subtotal (tổng đơn hàng ban đầu, không tính voucher)
    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");
    
    public RestaurantBalanceService(
        RestaurantBalanceRepository balanceRepository,
        RestaurantProfileRepository restaurantRepository,
        WithdrawalRequestRepository withdrawalRepository,
        BookingRepository bookingRepository,
        PaymentRepository paymentRepository
    ) {
        this.balanceRepository = balanceRepository;
        this.restaurantRepository = restaurantRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
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
        newBalance.setCommissionRate(new BigDecimal("7.0")); // 7% của subtotal
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
     * Admin commission metrics
     * Tính hoa hồng hôm nay dựa trên payment date (paidAt), không phải booking created date
     */
    @Transactional(readOnly = true)
    public BigDecimal getCommissionToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        // Tính hoa hồng từ subtotal (tổng tiền ban đầu) của bookings có payment được thanh toán hôm nay (paidAt)
        // Hoa hồng = 7% của subtotal
        BigDecimal subtotal = paymentRepository.sumSubtotalFromPaymentsByPaidAtRange(startOfDay, endOfDay);
        return calculateCommissionFromSubtotal(subtotal);
    }

    @Transactional(readOnly = true)
    public long getCompletedBookingsToday() {
        LocalDate today = LocalDate.now();
        return bookingRepository.countByStatusAndBookingTimeBetween(
            BookingStatus.COMPLETED,
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal getWeeklyCommission() {
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime start = end.minusDays(7);
        return getCommissionBetween(start, end);
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyCommission() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return getCommissionBetween(start, end);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCommission() {
        // Tính hoa hồng từ subtotal (tổng tiền ban đầu) của bookings có payment COMPLETED
        // Hoa hồng = 7% của subtotal (table fees + dishes + services, trước voucher discount)
        // Không phải từ bookings COMPLETED vì không phải tất cả bookings COMPLETED đều có payment COMPLETED
        BigDecimal subtotal = paymentRepository.sumSubtotalFromAllCompletedPayments();
        return calculateCommissionFromSubtotal(subtotal);
    }

    /**
     * Get commission for a specific date range
     * @param startDate Start date
     * @param endDate End date
     * @return Total commission for the date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getCommissionByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return getCommissionBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageCommissionPerBooking() {
        // Đếm số lượng bookings có payment COMPLETED, không phải số lượng bookings COMPLETED
        // Vì không phải tất cả bookings COMPLETED đều có payment COMPLETED
        long totalBookings = paymentRepository.countDistinctBookingsWithCompletedPayments();
        if (totalBookings == 0) {
            return BigDecimal.ZERO.setScale(0);
        }
        BigDecimal totalCommission = getTotalCommission();
        return totalCommission.divide(BigDecimal.valueOf(totalBookings), 0, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCommissionRate() {
        // Luôn trả về 7% (tỷ lệ hoa hồng mới: 7% của subtotal - tổng đơn hàng ban đầu, không tính voucher)
        // Không lấy trung bình từ database vì có thể có dữ liệu cũ (5% hoặc 30%)
        return DEFAULT_COMMISSION_RATE.multiply(new BigDecimal("100")); // 7.0%
    }

    public enum CommissionSeriesGranularity {
        DAILY,
        MONTHLY,
        YEARLY
    }

    @Transactional(readOnly = true)
    public List<CommissionSeriesPoint> getCommissionSeries(CommissionSeriesGranularity granularity, int points) {
        switch (granularity) {
            case DAILY:
                return buildDailySeries(points);
            case MONTHLY:
                return buildMonthlySeries(points);
            case YEARLY:
                return buildYearlySeries(points);
            default:
                return List.of();
        }
    }

    private List<CommissionSeriesPoint> buildDailySeries(int days) {
        if (days <= 0) {
            return List.of();
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();

        List<Object[]> rawData = bookingRepository.sumDepositByStatusGroupedByDay(
            BookingStatus.COMPLETED, startDateTime, endDateTime);

        Map<LocalDate, BigDecimal> grossByDate = new HashMap<>();
        for (Object[] row : rawData) {
            Date sqlDate = (Date) row[0];
            BigDecimal gross = (BigDecimal) row[1];
            grossByDate.put(sqlDate.toLocalDate(), calculateCommission(gross));
        }

        List<CommissionSeriesPoint> series = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            BigDecimal amount = grossByDate.getOrDefault(date, BigDecimal.ZERO);
            series.add(new CommissionSeriesPoint(date.format(DAY_LABEL_FORMATTER), amount));
        }
        return series;
    }

    private List<CommissionSeriesPoint> buildMonthlySeries(int months) {
        if (months <= 0) {
            return List.of();
        }

        YearMonth end = YearMonth.from(LocalDate.now());
        YearMonth start = end.minusMonths(months - 1L);

        LocalDateTime startDateTime = start.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = end.plusMonths(1).atDay(1).atStartOfDay();

        List<Object[]> rawData = bookingRepository.sumDepositByStatusGroupedByMonth(
            BookingStatus.COMPLETED.name(), startDateTime, endDateTime);

        Map<YearMonth, BigDecimal> grossByMonth = new HashMap<>();
        for (Object[] row : rawData) {
            Object period = row[0];
            BigDecimal gross = (BigDecimal) row[1];
            YearMonth key;
            if (period instanceof Timestamp ts) {
                key = YearMonth.from(ts.toLocalDateTime());
            } else if (period instanceof Instant inst) {
                key = YearMonth.from(LocalDateTime.ofInstant(inst, ZoneId.systemDefault()));
            } else {
                // Fallback: parse via string (e.g. "2025-01-01 00:00:00")
                key = YearMonth.from(LocalDateTime.parse(period.toString().replace(' ', 'T')));
            }
            grossByMonth.put(key, calculateCommission(gross));
        }

        List<CommissionSeriesPoint> series = new ArrayList<>();
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            BigDecimal amount = grossByMonth.getOrDefault(month, BigDecimal.ZERO);
            series.add(new CommissionSeriesPoint(month.format(MONTH_LABEL_FORMATTER), amount));
        }
        return series;
    }

    private List<CommissionSeriesPoint> buildYearlySeries(int years) {
        if (years <= 0) {
            return List.of();
        }

        int endYear = LocalDate.now().getYear();
        int startYear = endYear - (years - 1);

        LocalDateTime startDateTime = LocalDate.of(startYear, 1, 1).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.of(endYear + 1, 1, 1).atStartOfDay();

        List<Object[]> rawData = bookingRepository.sumDepositByStatusGroupedByYear(
            BookingStatus.COMPLETED.name(), startDateTime, endDateTime);

        Map<Integer, BigDecimal> grossByYear = new HashMap<>();
        for (Object[] row : rawData) {
            Object period = row[0];
            BigDecimal gross = (BigDecimal) row[1];
            int year;
            if (period instanceof Timestamp ts) {
                year = ts.toLocalDateTime().getYear();
            } else if (period instanceof Instant inst) {
                year = LocalDateTime.ofInstant(inst, ZoneId.systemDefault()).getYear();
            } else {
                year = LocalDateTime.parse(period.toString().replace(' ', 'T')).getYear();
            }
            grossByYear.put(year, calculateCommission(gross));
        }

        List<CommissionSeriesPoint> series = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            BigDecimal amount = grossByYear.getOrDefault(year, BigDecimal.ZERO);
            series.add(new CommissionSeriesPoint(String.valueOf(year), amount));
        }
        return series;
    }

    private BigDecimal getCommissionBetween(LocalDateTime start, LocalDateTime end) {
        // Tính hoa hồng từ subtotal (tổng tiền ban đầu) của bookings có payment trong khoảng thời gian
        // Hoa hồng = 7% của subtotal
        BigDecimal subtotal = paymentRepository.sumSubtotalFromPaymentsByPaidAtRange(start, end);
        return calculateCommissionFromSubtotal(subtotal);
    }

    /**
     * Calculate commission from subtotal (tổng tiền ban đầu)
     * Commission = 7% của subtotal (table fees + dishes + services, trước voucher discount)
     * @param subtotal Tổng tiền ban đầu (subtotal)
     * @return Commission amount (7% of subtotal)
     */
    private BigDecimal calculateCommissionFromSubtotal(BigDecimal subtotal) {
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        // Commission = 7% của subtotal (tổng đơn hàng ban đầu, không tính voucher)
        return subtotal.multiply(DEFAULT_COMMISSION_RATE).setScale(0, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate commission from deposit (legacy method, kept for backward compatibility)
     * @deprecated Use calculateCommissionFromSubtotal() instead for clarity
     * @param grossAmount Deposit amount (10% of subtotal)
     * @return Commission amount (7% of subtotal = 70% of deposit)
     */
    @Deprecated
    private BigDecimal calculateCommission(BigDecimal grossAmount) {
        if (grossAmount == null) {
            grossAmount = BigDecimal.ZERO;
        }
        // grossAmount là deposit (10% của subtotal)
        // Cần tính 7% trên subtotal (tổng đơn hàng ban đầu, không tính voucher)
        // Vì deposit = 10% subtotal, nên subtotal = deposit * 10
        // Commission = 7% subtotal = 7% * (deposit * 10) = 70% deposit
        BigDecimal subtotal = grossAmount.multiply(new BigDecimal("10")); // deposit * 10 = subtotal
        return calculateCommissionFromSubtotal(subtotal); // 7% của subtotal
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
     * Lấy balance theo restaurant ID (alias method)
     */
    public RestaurantBalance getBalanceByRestaurantId(Integer restaurantId) {
        return getOrCreateBalance(restaurantId);
    }

    /**
     * Save balance (alias method)
     */
    public RestaurantBalance saveBalance(RestaurantBalance balance) {
        return balanceRepository.save(balance);
    }
}
