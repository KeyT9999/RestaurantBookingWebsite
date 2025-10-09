package com.example.booking.dto.payout;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO cho việc từ chối yêu cầu rút tiền
 */
public class RejectWithdrawalDto {
    
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
    
    // Constructors
    public RejectWithdrawalDto() {
    }
    
    public RejectWithdrawalDto(String reason) {
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}

