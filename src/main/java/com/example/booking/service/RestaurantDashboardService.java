package com.example.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.Dish;
import com.example.booking.domain.BookingDish;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.WaitlistRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để tính toán các thống kê cho Dashboard Restaurant Owner
 */
@Service
@Transactional(readOnly = true)
public class RestaurantDashboardService {
    
    private final BookingRepository bookingRepository;
    private final RestaurantTableRepository tableRepository;
    private final WaitlistRepository waitlistRepository;
    private final DishRepository dishRepository;
    private final BookingDishRepository bookingDishRepository;
    private final RestaurantBalanceRepository balanceRepository;
    private final PaymentRepository paymentRepository;
    
    @Autowired
    public RestaurantDashboardService(
            BookingRepository bookingRepository,
            RestaurantTableRepository tableRepository,
            WaitlistRepository waitlistRepository,
            DishRepository dishRepository,
            BookingDishRepository bookingDishRepository,
            RestaurantBalanceRepository balanceRepository,
            PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
        this.waitlistRepository = waitlistRepository;
        this.dishRepository = dishRepository;
        this.bookingDishRepository = bookingDishRepository;
        this.balanceRepository = balanceRepository;
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * Lấy thống kê tổng quan cho dashboard
     */
    public DashboardStats getDashboardStats(Integer restaurantId) {
        DashboardStats stats = new DashboardStats();
        
        // 1. Thống kê booking hôm nay
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        List<Booking> todayBookings = bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            restaurantId, startOfDay, endOfDay);
        
        stats.setTodayBookings(todayBookings.size());
        stats.setTodayCompletedBookings((int) todayBookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
            .count());
        stats.setTodayPendingBookings((int) todayBookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.PENDING)
            .count());
        
        // 2. Thống kê bàn
        List<RestaurantTable> allTables = tableRepository.findByRestaurantRestaurantId(restaurantId);
        stats.setTotalTables(allTables.size());
        stats.setAvailableTables((int) allTables.stream()
            .filter(t -> t.getStatus() == TableStatus.AVAILABLE)
            .count());
        stats.setOccupiedTables((int) allTables.stream()
            .filter(t -> t.getStatus() == TableStatus.OCCUPIED)
            .count());
        
        // 3. Thống kê waitlist
        List<Waitlist> waitingCustomers = waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            restaurantId, com.example.booking.domain.WaitlistStatus.WAITING);
        stats.setWaitingCustomers(waitingCustomers.size());
        
        // 4. Tính doanh thu hôm nay từ Payment với status = COMPLETED
        // Sử dụng aggregation query để tránh N+1 problem
        try {
            List<Object[]> todayRevenueResults = paymentRepository.getRevenueByDateRange(
                    restaurantId, startOfDay, endOfDay);
            System.out.println("[Dashboard] Today revenue query returned " + todayRevenueResults.size() + " results");
            BigDecimal todayRevenue = todayRevenueResults.stream()
                    .map(result -> {
                        System.out.println("[Dashboard] Revenue result: date=" + result[0] + ", amount=" + result[1]);
                        return (BigDecimal) result[1];
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTodayRevenue(todayRevenue);
        } catch (Exception e) {
            System.err.println("[Dashboard] Error calculating today revenue: " + e.getMessage());
            e.printStackTrace();
            stats.setTodayRevenue(BigDecimal.ZERO);
        }
        
        // 5. Lấy số dư từ restaurant_balance
        Optional<RestaurantBalance> balance = balanceRepository.findByRestaurantRestaurantId(restaurantId);
        if (balance.isPresent()) {
            stats.setTotalRevenue(balance.get().getTotalRevenue());
            stats.setAvailableBalance(balance.get().getAvailableBalance());
            stats.setTotalCompletedBookings(balance.get().getTotalBookingsCompleted());
        } else {
            stats.setTotalRevenue(BigDecimal.ZERO);
            stats.setAvailableBalance(BigDecimal.ZERO);
            stats.setTotalCompletedBookings(0);
        }
        
        // 6. Tính toán phần trăm thay đổi so với hôm qua
        calculatePercentageChanges(stats, restaurantId);
        
        return stats;
    }
    
    /**
     * Tính toán phần trăm thay đổi so với hôm qua
     */
    private void calculatePercentageChanges(DashboardStats stats, Integer restaurantId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // Tính booking hôm qua
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(23, 59, 59);
        List<Booking> yesterdayBookings = bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            restaurantId, yesterdayStart, yesterdayEnd);
        int yesterdayBookingCount = yesterdayBookings.size();
        
        // Tính doanh thu hôm qua từ Payment với status = COMPLETED
        // Sử dụng aggregation query để tránh N+1 problem
        List<Object[]> yesterdayRevenueResults = paymentRepository.getRevenueByDateRange(
                restaurantId, yesterdayStart, yesterdayEnd);
        BigDecimal yesterdayRevenue = yesterdayRevenueResults.stream()
                .map(result -> (BigDecimal) result[1])
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Tính bàn hôm qua
        List<RestaurantTable> yesterdayTables = tableRepository.findByRestaurantRestaurantId(restaurantId);
        int yesterdayAvailableTables = (int) yesterdayTables.stream()
            .filter(t -> t.getStatus() == TableStatus.AVAILABLE)
            .count();
        int yesterdayOccupiedTables = (int) yesterdayTables.stream()
            .filter(t -> t.getStatus() == TableStatus.OCCUPIED)
            .count();
        
        // Tính phần trăm thay đổi
        stats.setBookingChangePercent(calculatePercentageChange(yesterdayBookingCount, stats.getTodayBookings()));
        stats.setRevenueChangePercent(calculatePercentageChange(yesterdayRevenue, stats.getTodayRevenue()));
        stats.setAvailableTableChangePercent(calculatePercentageChange(yesterdayAvailableTables, stats.getAvailableTables()));
        stats.setOccupiedTableChangePercent(calculatePercentageChange(yesterdayOccupiedTables, stats.getOccupiedTables()));
    }
    
    /**
     * Tính phần trăm thay đổi
     */
    private double calculatePercentageChange(Number oldValue, Number newValue) {
        if (oldValue == null || newValue == null) {
            return 0.0;
        }
        
        double old = oldValue.doubleValue();
        double newVal = newValue.doubleValue();
        
        if (old == 0) {
            return newVal > 0 ? 100.0 : 0.0;
        }
        
        return ((newVal - old) / old) * 100.0;
    }
    
    /**
     * Lấy dữ liệu doanh thu theo khoảng thời gian
     */
    public List<DailyRevenueData> getRevenueDataByPeriod(Integer restaurantId, String period) {
        List<DailyRevenueData> revenueData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        System.out.println("[Dashboard] Getting revenue data for restaurant " + restaurantId + ", period: " + period);

        switch (period.toLowerCase()) {
            case "week":
                // 7 ngày gần nhất
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    revenueData.add(getRevenueForDate(restaurantId, date));
                }
                break;
                
            case "month":
                // 30 ngày gần nhất
                for (int i = 29; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    revenueData.add(getRevenueForDate(restaurantId, date));
                }
                break;
                
            case "year":
                // 12 tháng gần nhất
                for (int i = 11; i >= 0; i--) {
                    LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
                    LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
                    revenueData.add(getRevenueForMonth(restaurantId, monthStart, monthEnd));
                }
                break;
                
            case "all":
                // Lấy toàn bộ dữ liệu từ khi bắt đầu (10 năm trước) đến hiện tại
                // Sử dụng aggregation query để tránh N+1 problem
                LocalDateTime startOfAll = LocalDateTime.now().minusYears(10);
                LocalDateTime endOfAll = LocalDateTime.now();
                System.out.println("[Dashboard] Querying all revenue from " + startOfAll + " to " + endOfAll);
                List<Object[]> revenueResults = paymentRepository.getRevenueByDateRange(
                        restaurantId, startOfAll, endOfAll);
                System.out.println("[Dashboard] All revenue query returned " + revenueResults.size() + " results");

                // Convert aggregation results to DailyRevenueData
                revenueData = revenueResults.stream()
                        .map(result -> {
                            // result[0] = Date (from CAST(p.paid_at AS DATE)) - could be java.sql.Date or
                            // LocalDate
                            // result[1] = BigDecimal (from SUM(p.amount))
                            LocalDate date;
                            if (result[0] instanceof java.sql.Date) {
                                date = ((java.sql.Date) result[0]).toLocalDate();
                            } else if (result[0] instanceof LocalDate) {
                                date = (LocalDate) result[0];
                            } else {
                                // Fallback: try to parse as string or use today
                                date = LocalDate.now();
                            }
                            BigDecimal revenue = result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;

                            // Get booking count for this date
                            LocalDateTime startOfDay = date.atStartOfDay();
                            LocalDateTime endOfDay = date.atTime(23, 59, 59);
                            List<Booking> dayBookings = bookingRepository
                                    .findByRestaurantRestaurantIdAndBookingTimeBetween(
                                            restaurantId, startOfDay, endOfDay);
                            return new DailyRevenueData(date, revenue, dayBookings.size());
                        })
                        .collect(Collectors.toList());
                break;

            default:
                // Mặc định là tuần
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    revenueData.add(getRevenueForDate(restaurantId, date));
                }
        }
        
        return revenueData;
    }
    
    /**
     * Lấy doanh thu cho một ngày cụ thể
     * Tính từ Payment với status = COMPLETED (không tính booking bị cancel)
     * Sử dụng aggregation query để tránh N+1 problem
     */
    private DailyRevenueData getRevenueForDate(Integer restaurantId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        try {
            // Sử dụng aggregation query để tính tổng doanh thu trực tiếp trong database
            List<Object[]> revenueResults = paymentRepository.getRevenueByDateRange(
                    restaurantId, startOfDay, endOfDay);

            System.out.println(
                    "[Dashboard] Revenue query for " + date + " returned " + revenueResults.size() + " results");

            // Lấy revenue từ kết quả aggregation (nếu có)
            BigDecimal dayRevenue = revenueResults.stream()
                    .map(result -> {
                        System.out.println("[Dashboard] Result for " + date + ": " + result[0] + " = " + result[1]);
                        return result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;
                    })
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            // Lấy số lượng bookings trong ngày (để hiển thị)
            List<Booking> dayBookings = bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    restaurantId, startOfDay, endOfDay);

            // Debug log for first few days
            if (date.isAfter(LocalDate.now().minusDays(7))) {
                System.out.println("[Dashboard] Revenue for " + date + ": " + dayRevenue + " (from "
                        + revenueResults.size() + " aggregated results)");
            }

            return new DailyRevenueData(date, dayRevenue, dayBookings.size());
        } catch (Exception e) {
            System.err.println("[Dashboard] Error getting revenue for date " + date + ": " + e.getMessage());
            e.printStackTrace();
            return new DailyRevenueData(date, BigDecimal.ZERO, 0);
        }
    }
    
    /**
     * Lấy doanh thu cho một tháng cụ thể
     * Tính từ Payment với status = COMPLETED (không tính booking bị cancel)
     * Sử dụng aggregation query để tránh N+1 problem
     */
    private DailyRevenueData getRevenueForMonth(Integer restaurantId, LocalDate monthStart, LocalDate monthEnd) {
        LocalDateTime startOfMonth = monthStart.atStartOfDay();
        LocalDateTime endOfMonth = monthEnd.atTime(23, 59, 59);
        
        // Sử dụng aggregation query để tính tổng doanh thu trực tiếp trong database
        List<Object[]> revenueResults = paymentRepository.getRevenueByDateRange(
            restaurantId, startOfMonth, endOfMonth);
        
        // Tính tổng revenue từ tất cả các ngày trong tháng
        BigDecimal monthRevenue = revenueResults.stream()
                .map(result -> (BigDecimal) result[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Lấy số lượng bookings trong tháng (để hiển thị)
        List<Booking> monthBookings = bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                restaurantId, startOfMonth, endOfMonth);

        return new DailyRevenueData(monthStart, monthRevenue, monthBookings.size());
    }
    
    /**
     * Lấy dữ liệu món ăn bán chạy
     * Lấy từ toàn bộ bookings của nhà hàng (không filter theo status)
     */
    public List<PopularDishData> getPopularDishesData(Integer restaurantId) {
        // Lấy tất cả dishes của restaurant
        List<Dish> dishes = dishRepository.findByRestaurantRestaurantId(restaurantId);

        List<PopularDishData> popularDishes = new ArrayList<>();
        
        for (Dish dish : dishes) {
            // Query booking dishes từ tất cả bookings của restaurant (không filter status)
            List<BookingDish> bookingDishes = bookingDishRepository
                    .findByRestaurantIdAndDishId(
                            restaurantId,
                            dish.getDishId());
            
            // Tính tổng số lượng đã bán từ tất cả bookings
            int totalQuantity = bookingDishes.stream()
                .mapToInt(BookingDish::getQuantity)
                .sum();
            
            if (totalQuantity > 0) {
                popularDishes.add(new PopularDishData(dish.getName(), totalQuantity, dish.getPrice()));
            }
        }
        
        // Sắp xếp theo số lượng bán giảm dần và lấy top 5
        List<PopularDishData> result = popularDishes.stream()
            .sorted((a, b) -> Integer.compare(b.getQuantitySold(), a.getQuantitySold()))
            .limit(5)
            .collect(Collectors.toList());

        // Debug log
        System.out.println(
                "[Dashboard] Popular dishes for restaurant " + restaurantId + ": " + result.size()
                        + " dishes (from all bookings)");
        for (PopularDishData dish : result) {
            System.out.println(
                    "  - " + dish.getDishName() + ": " + dish.getQuantitySold() + " phần, Giá: " + dish.getPrice());
        }

        return result;
    }
    
    /**
     * Lấy booking gần đây nhất với thông tin đầy đủ
     */
    public List<BookingInfo> getRecentBookingsWithDetails(Integer restaurantId, int limit) {
        List<Booking> bookings = bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId)
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
        
        return bookings.stream()
            .map(this::convertToBookingInfo)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Booking entity to BookingInfo DTO
     */
    private BookingInfo convertToBookingInfo(Booking booking) {
        BookingInfo info = new BookingInfo();
        info.setBookingId(booking.getBookingId());
        
        // Eagerly load customer data to avoid LazyInitializationException
        try {
            if (booking.getCustomer() != null) {
                String customerName = booking.getCustomer().getFullName();
                
                if (customerName != null && !customerName.trim().isEmpty()) {
                    info.setCustomerName(customerName);
                } else {
                    // Try to get name from User
                    if (booking.getCustomer().getUser() != null) {
                        String userName = booking.getCustomer().getUser().getFullName();
                        info.setCustomerName(userName != null ? userName : "Khách hàng không xác định");
                    } else {
                        info.setCustomerName("Khách hàng không xác định");
                    }
                }
                
                // Get phone
                if (booking.getCustomer().getUser() != null) {
                    String customerPhone = booking.getCustomer().getUser().getPhoneNumber();
                    info.setCustomerPhone(customerPhone);
                }
            } else {
                info.setCustomerName("Khách hàng không xác định");
                info.setCustomerPhone(null);
            }
        } catch (Exception e) {
            info.setCustomerName("Khách hàng không xác định");
            info.setCustomerPhone(null);
        }
        
        info.setBookingTime(booking.getBookingTime());
        info.setNumberOfGuests(booking.getNumberOfGuests());
        info.setStatus(booking.getStatus());
        info.setDepositAmount(booking.getDepositAmount());
        info.setNote(booking.getNote());
        
        // Get assigned tables
        try {
            List<String> tableNames = booking.getBookingTables().stream()
                .map(bt -> bt.getTable().getTableName())
                .collect(Collectors.toList());
            info.setAssignedTables(tableNames);
        } catch (Exception e) {
            info.setAssignedTables(new ArrayList<>());
        }
        
        return info;
    }
    
    public List<Waitlist> getWaitingCustomers(Integer restaurantId) {
        return waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            restaurantId, com.example.booking.domain.WaitlistStatus.WAITING);
    }
    
    // DTO Classes
    
    public static class DashboardStats {
        private int todayBookings;
        private int todayCompletedBookings;
        private int todayPendingBookings;
        private int totalTables;
        private int availableTables;
        private int occupiedTables;
        private int waitingCustomers;
        private BigDecimal todayRevenue;
        private BigDecimal totalRevenue;
        private BigDecimal availableBalance;
        private int totalCompletedBookings;
        
        // Percentage changes
        private double bookingChangePercent;
        private double availableTableChangePercent;
        private double occupiedTableChangePercent;
        private double revenueChangePercent;
        
        // Getters and Setters
        public int getTodayBookings() { return todayBookings; }
        public void setTodayBookings(int todayBookings) { this.todayBookings = todayBookings; }
        
        public int getTodayCompletedBookings() { return todayCompletedBookings; }
        public void setTodayCompletedBookings(int todayCompletedBookings) { this.todayCompletedBookings = todayCompletedBookings; }
        
        public int getTodayPendingBookings() { return todayPendingBookings; }
        public void setTodayPendingBookings(int todayPendingBookings) { this.todayPendingBookings = todayPendingBookings; }
        
        public int getTotalTables() { return totalTables; }
        public void setTotalTables(int totalTables) { this.totalTables = totalTables; }
        
        public int getAvailableTables() { return availableTables; }
        public void setAvailableTables(int availableTables) { this.availableTables = availableTables; }
        
        public int getOccupiedTables() { return occupiedTables; }
        public void setOccupiedTables(int occupiedTables) { this.occupiedTables = occupiedTables; }
        
        public int getWaitingCustomers() { return waitingCustomers; }
        public void setWaitingCustomers(int waitingCustomers) { this.waitingCustomers = waitingCustomers; }
        
        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }
        
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public BigDecimal getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
        
        public int getTotalCompletedBookings() { return totalCompletedBookings; }
        public void setTotalCompletedBookings(int totalCompletedBookings) { this.totalCompletedBookings = totalCompletedBookings; }
        
        // Percentage change getters and setters
        public double getBookingChangePercent() { return bookingChangePercent; }
        public void setBookingChangePercent(double bookingChangePercent) { this.bookingChangePercent = bookingChangePercent; }
        
        public double getAvailableTableChangePercent() { return availableTableChangePercent; }
        public void setAvailableTableChangePercent(double availableTableChangePercent) { this.availableTableChangePercent = availableTableChangePercent; }
        
        public double getOccupiedTableChangePercent() { return occupiedTableChangePercent; }
        public void setOccupiedTableChangePercent(double occupiedTableChangePercent) { this.occupiedTableChangePercent = occupiedTableChangePercent; }
        
        public double getRevenueChangePercent() { return revenueChangePercent; }
        public void setRevenueChangePercent(double revenueChangePercent) { this.revenueChangePercent = revenueChangePercent; }
    }
    
    public static class DailyRevenueData {
        private LocalDate date;
        private BigDecimal revenue;
        private int bookingCount;
        
        public DailyRevenueData(LocalDate date, BigDecimal revenue, int bookingCount) {
            this.date = date;
            this.revenue = revenue;
            this.bookingCount = bookingCount;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public BigDecimal getRevenue() { return revenue; }
        public int getBookingCount() { return bookingCount; }
    }
    
    public static class PopularDishData {
        private String dishName;
        private int quantitySold;
        private BigDecimal price;
        
        public PopularDishData(String dishName, int quantitySold, BigDecimal price) {
            this.dishName = dishName;
            this.quantitySold = quantitySold;
            this.price = price;
        }
        
        // Getters
        public String getDishName() { return dishName; }
        public int getQuantitySold() { return quantitySold; }
        public BigDecimal getPrice() { return price; }
    }
    
    public static class BookingInfo {
        private Integer bookingId;
        private String customerName;
        private String customerPhone;
        private LocalDateTime bookingTime;
        private Integer numberOfGuests;
        private BookingStatus status;
        private BigDecimal depositAmount;
        private String note;
        private List<String> assignedTables;
        
        // Constructors
        public BookingInfo() {}
        
        // Getters and Setters
        public Integer getBookingId() { return bookingId; }
        public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        
        public LocalDateTime getBookingTime() { return bookingTime; }
        public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
        
        public Integer getNumberOfGuests() { return numberOfGuests; }
        public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }
        
        public BookingStatus getStatus() { return status; }
        public void setStatus(BookingStatus status) { this.status = status; }
        
        public BigDecimal getDepositAmount() { return depositAmount; }
        public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
        
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        
        public List<String> getAssignedTables() { return assignedTables; }
        public void setAssignedTables(List<String> assignedTables) { this.assignedTables = assignedTables; }
    }
}
