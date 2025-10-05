package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;

@Service
public interface VoucherService {

    record ValidationRequest(String code, Integer restaurantId, LocalDateTime bookingTime, Integer guestCount, Customer customer, BigDecimal orderAmount) {}
    record ValidationResult(boolean valid, String reason, BigDecimal calculatedDiscount, Voucher voucher) {}
    
    record ApplyRequest(String code, Integer restaurantId, UUID customerId, BigDecimal orderAmount, Integer bookingId) {}
    record ApplyResult(boolean success, String reason, BigDecimal discountApplied, Integer redemptionId) {}

    Optional<Voucher> findByCode(String code);

    ValidationResult validate(ValidationRequest request);

    ApplyResult applyToBooking(ApplyRequest request);

    BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount);

    boolean isVoucherApplicableToRestaurant(Voucher voucher, RestaurantProfile restaurant);
    
    // Admin/Restaurant Owner methods
    Voucher createAdminVoucher(VoucherCreateDto dto);
    Voucher createRestaurantVoucher(Integer restaurantId, VoucherCreateDto dto);
    Voucher updateVoucher(Integer voucherId, VoucherCreateDto dto);
    List<Voucher> getVouchersByRestaurant(Integer restaurantId);
    List<Voucher> getAllVouchers();
    Voucher getVoucherById(Integer voucherId);
    void deleteVoucher(Integer voucherId);
    
    void pauseVoucher(Integer voucherId);
    void resumeVoucher(Integer voucherId);
    void expireVoucher(Integer voucherId);
    
    // Customer assignment methods
    void assignVoucherToCustomers(Integer voucherId, List<UUID> customerIds);
    
    Long countRedemptionsByVoucherId(Integer voucherId);
    
    Long countRedemptionsByVoucherIdAndCustomerId(Integer voucherId, UUID customerId);
    void revokeVoucherFromCustomer(Integer voucherId, UUID customerId);
    List<Voucher> getVouchersByCustomer(UUID customerId);
    
    // Status management
    void activateScheduledVouchers();
    void expireVouchers();
    
    // Reporting
    List<VoucherUsageStats> getVoucherUsageStats(Integer voucherId);
    List<CustomerVoucherView> getCustomerVouchers(UUID customerId);
    
    // DTOs
    record VoucherCreateDto(String code, String description, String discountType, 
                           BigDecimal discountValue, LocalDate startDate, LocalDate endDate,
                           Integer globalUsageLimit, Integer perCustomerLimit, 
                           BigDecimal minOrderAmount, BigDecimal maxDiscountAmount,
                           Integer restaurantId, VoucherStatus status) {}
    
    record VoucherUsageStats(Integer voucherId, String code, String description,
                            Long totalRedemptions, Long uniqueCustomers, 
                            BigDecimal totalDiscountGiven, LocalDateTime lastUsed) {}
    
    record CustomerVoucherView(Integer customerVoucherId, String voucherCode, 
                              String description, Integer timesUsed, Integer remainingUses,
                              LocalDateTime assignedAt, LocalDateTime lastUsedAt) {}
}


