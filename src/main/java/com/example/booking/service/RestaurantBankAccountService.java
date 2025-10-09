package com.example.booking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.RestaurantBankAccount;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.RestaurantBankAccountRepository;
import com.example.booking.repository.RestaurantProfileRepository;

/**
 * Service để quản lý tài khoản ngân hàng của nhà hàng
 */
@Service
public class RestaurantBankAccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantBankAccountService.class);
    
    private final RestaurantBankAccountRepository bankAccountRepository;
    private final RestaurantProfileRepository restaurantRepository;
    
    public RestaurantBankAccountService(
        RestaurantBankAccountRepository bankAccountRepository,
        RestaurantProfileRepository restaurantRepository
    ) {
        this.bankAccountRepository = bankAccountRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * Lấy danh sách tài khoản ngân hàng của nhà hàng
     */
    @Transactional(readOnly = true)
    public List<RestaurantBankAccountDto> getBankAccounts(Integer restaurantId) {
        List<RestaurantBankAccount> accounts = bankAccountRepository.findByRestaurantId(restaurantId);
        
        return accounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Thêm tài khoản ngân hàng mới
     */
    @Transactional
    public RestaurantBankAccountDto addBankAccount(Integer restaurantId, RestaurantBankAccountDto dto) {
        // Validate
        validateBankAccount(dto);
        
        // Check if restaurant exists
        RestaurantProfile restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà hàng"));
        
        // Check if account number already exists for this restaurant
        if (bankAccountRepository.existsByRestaurantIdAndAccountNumber(restaurantId, dto.getAccountNumber())) {
            throw new BadRequestException("Số tài khoản này đã tồn tại cho nhà hàng");
        }
        
        // If this is set as default, unset other default accounts
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            bankAccountRepository.unsetDefaultForRestaurant(restaurantId);
        }
        
        // Create new bank account
        RestaurantBankAccount account = new RestaurantBankAccount();
        account.setRestaurant(restaurant);
        account.setBankCode(dto.getBankCode());
        account.setBankName(getBankName(dto.getBankCode()));
        account.setAccountNumber(dto.getAccountNumber());
        account.setAccountHolderName(dto.getAccountHolderName());
        account.setIsVerified(false); // Default to unverified
        account.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault());
        
        // If this is the first account, make it default
        if (bankAccountRepository.countByRestaurantId(restaurantId) == 0) {
            account.setIsDefault(true);
        }
        
        account = bankAccountRepository.save(account);
        
        logger.info("💳 Added bank account {} for restaurant {}", account.getAccountNumber(), restaurantId);
        
        return convertToDto(account);
    }
    
    /**
     * Cập nhật tài khoản ngân hàng
     */
    @Transactional
    public RestaurantBankAccountDto updateBankAccount(Integer restaurantId, Integer accountId, RestaurantBankAccountDto dto) {
        RestaurantBankAccount account = bankAccountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ngân hàng"));
        
        // Check ownership
        if (!account.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new BadRequestException("Tài khoản ngân hàng không thuộc nhà hàng này");
        }
        
        // Validate
        validateBankAccount(dto);
        
        // Check if account number already exists for another account of this restaurant
        if (bankAccountRepository.existsByRestaurantIdAndAccountNumberAndAccountIdNot(
                restaurantId, dto.getAccountNumber(), accountId)) {
            throw new BadRequestException("Số tài khoản này đã tồn tại cho nhà hàng");
        }
        
        // If setting as default, unset other defaults
        if (dto.getIsDefault() != null && dto.getIsDefault() && !account.getIsDefault()) {
            bankAccountRepository.unsetDefaultForRestaurant(restaurantId);
        }
        
        // Update fields
        account.setBankCode(dto.getBankCode());
        account.setBankName(getBankName(dto.getBankCode()));
        account.setAccountNumber(dto.getAccountNumber());
        account.setAccountHolderName(dto.getAccountHolderName());
        account.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : account.getIsDefault());
        
        account = bankAccountRepository.save(account);
        
        logger.info("✏️ Updated bank account {} for restaurant {}", account.getAccountNumber(), restaurantId);
        
        return convertToDto(account);
    }
    
    /**
     * Xóa tài khoản ngân hàng
     */
    @Transactional
    public void deleteBankAccount(Integer restaurantId, Integer accountId) {
        RestaurantBankAccount account = bankAccountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ngân hàng"));
        
        // Check ownership
        if (!account.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new BadRequestException("Tài khoản ngân hàng không thuộc nhà hàng này");
        }
        
        // Check if it's the default account and there are other accounts
        if (account.getIsDefault() && bankAccountRepository.countByRestaurantId(restaurantId) > 1) {
            throw new BadRequestException("Không thể xóa tài khoản mặc định khi còn tài khoản khác");
        }
        
        // Check if account is being used in any pending withdrawals
        // TODO: Add check for pending withdrawals
        
        bankAccountRepository.delete(account);
        
        logger.info("🗑️ Deleted bank account {} for restaurant {}", account.getAccountNumber(), restaurantId);
    }
    
    /**
     * Lấy tài khoản mặc định của nhà hàng
     */
    @Transactional(readOnly = true)
    public RestaurantBankAccountDto getDefaultBankAccount(Integer restaurantId) {
        RestaurantBankAccount account = bankAccountRepository.findByRestaurantIdAndIsDefaultTrue(restaurantId)
            .orElse(null);
        
        return account != null ? convertToDto(account) : null;
    }
    
    /**
     * Validate bank account data
     */
    private void validateBankAccount(RestaurantBankAccountDto dto) {
        if (dto.getBankCode() == null || dto.getBankCode().trim().isEmpty()) {
            throw new BadRequestException("Mã ngân hàng không được để trống");
        }
        
        if (dto.getAccountNumber() == null || dto.getAccountNumber().trim().isEmpty()) {
            throw new BadRequestException("Số tài khoản không được để trống");
        }
        
        if (dto.getAccountHolderName() == null || dto.getAccountHolderName().trim().isEmpty()) {
            throw new BadRequestException("Tên chủ tài khoản không được để trống");
        }
        
        // Validate account number format (basic validation)
        if (!dto.getAccountNumber().matches("^[0-9]{9,20}$")) {
            throw new BadRequestException("Số tài khoản phải chứa từ 9-20 chữ số");
        }
        
        // Validate bank code
        if (!isValidBankCode(dto.getBankCode())) {
            throw new BadRequestException("Mã ngân hàng không hợp lệ");
        }
    }
    
    /**
     * Get bank name from bank code
     */
    private String getBankName(String bankCode) {
        return switch (bankCode) {
            case "970422" -> "MB Bank";
            case "970436" -> "Vietcombank";
            case "970415" -> "Techcombank";
            case "970416" -> "VietinBank";
            case "970423" -> "Agribank";
            case "970427" -> "ACB";
            case "970418" -> "Sacombank";
            case "970419" -> "BIDV";
            default -> "Ngân hàng khác";
        };
    }
    
    /**
     * Check if bank code is valid
     */
    private boolean isValidBankCode(String bankCode) {
        String[] validCodes = {"970422", "970436", "970415", "970416", "970423", "970427", "970418"};
        for (String code : validCodes) {
            if (code.equals(bankCode)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Convert entity to DTO
     */
    private RestaurantBankAccountDto convertToDto(RestaurantBankAccount account) {
        RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
        dto.setAccountId(account.getAccountId());
        dto.setRestaurantId(account.getRestaurant().getRestaurantId());
        dto.setBankCode(account.getBankCode());
        dto.setBankName(account.getBankName());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountHolderName(account.getAccountHolderName());
        dto.setIsVerified(account.getIsVerified());
        dto.setIsDefault(account.getIsDefault());
        
        // Create masked account number for display
        String accountNumber = account.getAccountNumber();
        if (accountNumber != null && accountNumber.length() > 4) {
            String masked = "*".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
            dto.setMaskedAccountNumber(masked);
        }
        
        return dto;
    }
}