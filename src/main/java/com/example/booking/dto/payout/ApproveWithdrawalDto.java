package com.example.booking.dto.payout;

/**
 * DTO cho việc duyệt/từ chối yêu cầu rút tiền
 */
public class ApproveWithdrawalDto {
    
    private String notes;
    
    // Constructors
    public ApproveWithdrawalDto() {
    }
    
    public ApproveWithdrawalDto(String notes) {
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}

