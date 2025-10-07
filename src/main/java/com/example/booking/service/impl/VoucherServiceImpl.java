package com.example.booking.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Customer;
import com.example.booking.domain.CustomerVoucher;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherRedemption;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.CustomerVoucherRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.repository.VoucherRedemptionRepository;
import com.example.booking.repository.VoucherRepository;
import com.example.booking.service.VoucherService;

@Service
@Transactional(readOnly = true)
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private CustomerVoucherRepository customerVoucherRepository;
    
    @Autowired
    private VoucherRedemptionRepository redemptionRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;

    @Override
    public Optional<Voucher> findByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return voucherRepository.findByCodeIgnoreCase(code.trim());
    }

    @Override
    public ValidationResult validate(ValidationRequest req) {
        if (req == null || req.code() == null || req.code().isBlank()) {
            return new ValidationResult(false, "EMPTY_CODE", null, null);
        }

        Optional<Voucher> opt = findByCode(req.code());
        if (opt.isEmpty()) {
            return new ValidationResult(false, "NOT_FOUND", null, null);
        }

        Voucher v = opt.get();

        // Status check
        if (v.getStatus() != VoucherStatus.ACTIVE) {
            return new ValidationResult(false, "INACTIVE", null, v);
        }

        // Date window check
        LocalDate today = LocalDate.now();
        if (v.getStartDate() != null && today.isBefore(v.getStartDate())) {
            return new ValidationResult(false, "NOT_STARTED", null, v);
        }
        if (v.getEndDate() != null && today.isAfter(v.getEndDate())) {
            return new ValidationResult(false, "EXPIRED", null, v);
        }

        // Restaurant scope check
        if (v.getRestaurant() != null) {
            if (req.restaurantId() == null || !v.getRestaurant().getRestaurantId().equals(req.restaurantId())) {
                return new ValidationResult(false, "RESTAURANT_SCOPE_MISMATCH", null, v);
            }
        }

        // Min order amount check
        if (v.getMinOrderAmount() != null && req.orderAmount() != null) {
            if (req.orderAmount().compareTo(v.getMinOrderAmount()) < 0) {
                return new ValidationResult(false, "MIN_ORDER_NOT_MET", null, v);
            }
        }

        // Calculate discount
        BigDecimal calculatedDiscount = calculateDiscount(v, req.orderAmount());
        
        return new ValidationResult(true, null, calculatedDiscount, v);
    }

    @Override
    @Transactional
    public ApplyResult applyToBooking(ApplyRequest req) {
        try {
            // Find voucher with lock
            Optional<Voucher> optVoucher = voucherRepository.findByCodeForUpdate(req.code());
            if (optVoucher.isEmpty()) {
                return new ApplyResult(false, "NOT_FOUND", null, null);
            }

            Voucher voucher = optVoucher.get();

            // Validate voucher
            ValidationRequest validationReq = new ValidationRequest(
                req.code(), req.restaurantId(), LocalDateTime.now(), null, null, req.orderAmount()
            );
            ValidationResult validation = validate(validationReq);
            
            if (!validation.valid()) {
                return new ApplyResult(false, validation.reason(), null, null);
            }

            // Check global usage limit
            if (voucher.getGlobalUsageLimit() != null) {
                Long globalUsage = redemptionRepository.countByVoucherIdForUpdate(voucher.getVoucherId());
                if (globalUsage >= voucher.getGlobalUsageLimit()) {
                    return new ApplyResult(false, "GLOBAL_LIMIT_REACHED", null, null);
                }
            }

            // Check per-customer usage limit
            if (voucher.getPerCustomerLimit() != null) {
                Long customerUsage = redemptionRepository.countByVoucherIdAndCustomerIdForUpdate(
                    voucher.getVoucherId(), req.customerId()
                );
                if (customerUsage >= voucher.getPerCustomerLimit()) {
                    return new ApplyResult(false, "PER_CUSTOMER_LIMIT_REACHED", null, null);
                }
            }

            // Check if assigned voucher
            if (voucher.isAssignedVoucher()) {
                Optional<CustomerVoucher> customerVoucher = customerVoucherRepository
                    .findByCustomerIdAndVoucherIdForUpdate(req.customerId(), voucher.getVoucherId());
                if (customerVoucher.isEmpty()) {
                    return new ApplyResult(false, "NOT_ASSIGNED", null, null);
                }
                if (!customerVoucher.get().canUseMore()) {
                    return new ApplyResult(false, "LIMIT_REACHED", null, null);
                }
            }

            // Calculate final discount
            BigDecimal discountApplied = calculateDiscount(voucher, req.orderAmount());
            if (discountApplied.compareTo(BigDecimal.ZERO) <= 0) {
                return new ApplyResult(false, "NO_DISCOUNT_APPLICABLE", null, null);
            }

            // Create redemption record
            VoucherRedemption redemption = new VoucherRedemption(
                voucher, req.customerId(), discountApplied, 
                req.orderAmount(), req.orderAmount().subtract(discountApplied)
            );
            redemption = redemptionRepository.save(redemption);

            // Update customer voucher usage if assigned
            if (voucher.isAssignedVoucher()) {
                customerVoucherRepository.incrementUsage(req.customerId(), voucher.getVoucherId());
            }

            return new ApplyResult(true, null, discountApplied, redemption.getRedemptionId());

        } catch (Exception e) {
            return new ApplyResult(false, "APPLICATION_ERROR", null, null);
        }
    }

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount) {
        if (voucher == null || orderAmount == null) return BigDecimal.ZERO;
        if (voucher.getDiscountType() == null || voucher.getDiscountValue() == null) return BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;

        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            discount = orderAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else if (voucher.getDiscountType() == DiscountType.FIXED) {
            discount = voucher.getDiscountValue().min(orderAmount);
        }

        // Apply max discount cap
        if (voucher.getMaxDiscountAmount() != null) {
            discount = discount.min(voucher.getMaxDiscountAmount());
        }

        // Ensure discount doesn't exceed order amount
        discount = discount.min(orderAmount);

        return discount.max(BigDecimal.ZERO);
    }

    @Override
    public boolean isVoucherApplicableToRestaurant(Voucher voucher, RestaurantProfile restaurant) {
        if (voucher == null) return false;
        if (voucher.getRestaurant() == null) return true; // Global voucher
        return restaurant != null && voucher.getRestaurant().getRestaurantId().equals(restaurant.getRestaurantId());
    }

    @Override
    @Transactional
    public Voucher createAdminVoucher(VoucherCreateDto dto) {
        User createdBy = userRepository.findAll().stream()
            .filter(user -> user.getRole().name().equals("ADMIN"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No admin user found"));

        Voucher voucher = new Voucher();
        voucher.setCode(dto.code());
        voucher.setDescription(dto.description());
        voucher.setDiscountType(DiscountType.valueOf(dto.discountType()));
        voucher.setDiscountValue(dto.discountValue());
        voucher.setStartDate(dto.startDate());
        voucher.setEndDate(dto.endDate());
        voucher.setGlobalUsageLimit(dto.globalUsageLimit());
        voucher.setPerCustomerLimit(dto.perCustomerLimit());
        voucher.setMinOrderAmount(dto.minOrderAmount());
        voucher.setMaxDiscountAmount(dto.maxDiscountAmount());
        voucher.setCreatedByUser(createdBy);
        voucher.setRestaurant(null); // Global voucher
        voucher.setStatus(dto.status());

        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher createRestaurantVoucher(Integer restaurantId, VoucherCreateDto dto) {
        // For now, use a default user - TODO: Get from authentication context
        User createdBy = userRepository.findAll().stream()
            .filter(user -> user.getRole().name().equals("RESTAURANT_OWNER"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No restaurant owner user found"));

        // Load restaurant from database instead of creating new object
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + restaurantId));

        Voucher voucher = new Voucher();
        voucher.setCode(dto.code());
        voucher.setDescription(dto.description());
        voucher.setDiscountType(DiscountType.valueOf(dto.discountType()));
        voucher.setDiscountValue(dto.discountValue());
        voucher.setStartDate(dto.startDate());
        voucher.setEndDate(dto.endDate());
        voucher.setGlobalUsageLimit(dto.globalUsageLimit());
        voucher.setPerCustomerLimit(dto.perCustomerLimit());
        voucher.setMinOrderAmount(dto.minOrderAmount());
        voucher.setMaxDiscountAmount(dto.maxDiscountAmount());
        voucher.setCreatedByUser(createdBy);
        voucher.setRestaurant(restaurant);
        voucher.setStatus(dto.status());

        return voucherRepository.save(voucher);
    }

    @Override
    public List<Voucher> getVouchersByRestaurant(Integer restaurantId) {
        return voucherRepository.findByRestaurant_RestaurantId(restaurantId);
    }

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    @Transactional
    public void assignVoucherToCustomers(Integer voucherId, List<UUID> customerIds) {
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found with ID: " + voucherId));
        
        for (UUID customerId : customerIds) {
            // Check if already assigned
            Optional<CustomerVoucher> existing = customerVoucherRepository
                .findByCustomerIdAndVoucherIdForUpdate(customerId, voucherId);
            
            if (existing.isEmpty()) {
                // Create new assignment
                CustomerVoucher customerVoucher = new CustomerVoucher();
                customerVoucher.setCustomer(customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found")));
                customerVoucher.setVoucher(voucher);
                customerVoucher.setTimesUsed(0);
                customerVoucher.setLastUsedAt(null);
                
                customerVoucherRepository.save(customerVoucher);
            }
        }
    }

    @Override
    public Long countRedemptionsByVoucherId(Integer voucherId) {
        return redemptionRepository.countByVoucherId(voucherId);
    }

    @Override
    public Long countRedemptionsByVoucherIdAndCustomerId(Integer voucherId, UUID customerId) {
        return redemptionRepository.countByVoucherIdAndCustomerId(voucherId, customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> getVouchersByCustomer(UUID customerId) {
        // Get vouchers assigned to this customer
        List<CustomerVoucher> customerVouchers = customerVoucherRepository.findByCustomerId(customerId);
        
        // Get global vouchers (not assigned to specific customers)
        List<Voucher> globalVouchers = voucherRepository.findGlobalVouchers();
        
        // Combine both lists
        List<Voucher> allVouchers = new ArrayList<>();
        
        // Add assigned vouchers
        for (CustomerVoucher cv : customerVouchers) {
            if (cv.getVoucher().getStatus() == VoucherStatus.ACTIVE) {
                allVouchers.add(cv.getVoucher());
            }
        }
        
        // Add global vouchers
        for (Voucher voucher : globalVouchers) {
            if (voucher.getStatus() == VoucherStatus.ACTIVE) {
                allVouchers.add(voucher);
            }
        }
        
        return allVouchers;
    }
    
    @Override
    public Voucher getVoucherById(Integer voucherId) {
        return voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found with ID: " + voucherId));
    }

    @Override
    @Transactional
    public Voucher updateVoucher(Integer voucherId, VoucherCreateDto dto) {
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found with ID: " + voucherId));
        
        // Update voucher fields
        voucher.setDescription(dto.description());
        voucher.setDiscountType(DiscountType.valueOf(dto.discountType()));
        voucher.setDiscountValue(dto.discountValue());
        voucher.setStartDate(dto.startDate());
        voucher.setEndDate(dto.endDate());
        voucher.setGlobalUsageLimit(dto.globalUsageLimit());
        voucher.setPerCustomerLimit(dto.perCustomerLimit());
        voucher.setMinOrderAmount(dto.minOrderAmount());
        voucher.setMaxDiscountAmount(dto.maxDiscountAmount());
        voucher.setStatus(dto.status());
        
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher updateVoucher(Integer voucherId, VoucherEditDto dto) {
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found with ID: " + voucherId));
        
        // Update voucher fields
        voucher.setCode(dto.code());
        voucher.setDescription(dto.description());
        voucher.setDiscountType(DiscountType.valueOf(dto.discountType()));
        voucher.setDiscountValue(dto.discountValue());
        voucher.setStartDate(dto.startDate());
        voucher.setEndDate(dto.endDate());
        voucher.setGlobalUsageLimit(dto.globalUsageLimit());
        voucher.setPerCustomerLimit(dto.perCustomerLimit());
        voucher.setMinOrderAmount(dto.minOrderAmount());
        voucher.setMaxDiscountAmount(dto.maxDiscountAmount());
        voucher.setStatus(VoucherStatus.valueOf(dto.status()));
        
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(Integer voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found with ID: " + voucherId));
        
        // Check if voucher has been used
        // TODO: Add logic to check if voucher has been redeemed
        // For now, we'll allow deletion of any voucher
        
        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional
    public void pauseVoucher(Integer voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setStatus(VoucherStatus.INACTIVE);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void resumeVoucher(Integer voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void expireVoucher(Integer voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setStatus(VoucherStatus.EXPIRED);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void revokeVoucherFromCustomer(Integer voucherId, UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));

        Optional<CustomerVoucher> customerVoucher = customerVoucherRepository
            .findByCustomerAndVoucher(customer, voucher);
        
        if (customerVoucher.isPresent()) {
            customerVoucherRepository.delete(customerVoucher.get());
        }
    }

    @Override
    @Transactional
    public void activateScheduledVouchers() {
        List<Voucher> scheduledVouchers = voucherRepository.findActiveVouchersOnDate(
            VoucherStatus.SCHEDULED, LocalDate.now()
        );
        
        for (Voucher voucher : scheduledVouchers) {
            voucher.setStatus(VoucherStatus.ACTIVE);
            voucherRepository.save(voucher);
        }
    }

    @Override
    @Transactional
    public void expireVouchers() {
        List<Voucher> expiredVouchers = voucherRepository.findByStatus(VoucherStatus.ACTIVE)
            .stream()
            .filter(v -> v.getEndDate() != null && LocalDate.now().isAfter(v.getEndDate()))
            .collect(Collectors.toList());
        
        for (Voucher voucher : expiredVouchers) {
            voucher.setStatus(VoucherStatus.EXPIRED);
            voucherRepository.save(voucher);
        }
        
        // Log the number of expired vouchers
        if (!expiredVouchers.isEmpty()) {
            System.out.println("Expired " + expiredVouchers.size() + " vouchers");
        }
    }

    @Override
    public List<VoucherUsageStats> getVoucherUsageStats(Integer voucherId) {
        // Implementation for usage statistics
        return List.of(); // Placeholder
    }

    @Override
    public List<CustomerVoucherView> getCustomerVouchers(UUID customerId) {
        List<CustomerVoucher> customerVouchers = customerVoucherRepository
            .findActiveVouchersByCustomerId(customerId);
        
        return customerVouchers.stream()
            .map(cv -> new CustomerVoucherView(
                cv.getCustomerVoucherId(),
                cv.getVoucher().getCode(),
                cv.getVoucher().getDescription(),
                cv.getTimesUsed(),
                cv.getRemainingUses(),
                cv.getAssignedAt(),
                cv.getLastUsedAt()
            ))
            .collect(Collectors.toList());
    }
}