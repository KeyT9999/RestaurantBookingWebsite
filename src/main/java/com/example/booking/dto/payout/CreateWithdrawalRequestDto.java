package com.example.booking.dto.payout;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho việc tạo yêu cầu rút tiền
 */
public class CreateWithdrawalRequestDto {
    
    @NotNull(message = "Tài khoản ngân hàng không được để trống")
    private Integer bankAccountId;
    
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "100000", message = "Số tiền rút tối thiểu là 100,000 VNĐ")
    private BigDecimal amount;
    
    private String description;
    
    // Constructors
    public CreateWithdrawalRequestDto() {
    }
    
    // Getters and Setters
    public Integer getBankAccountId() {
        return bankAccountId;
    }
    
    public void setBankAccountId(Integer bankAccountId) {
        this.bankAccountId = bankAccountId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

