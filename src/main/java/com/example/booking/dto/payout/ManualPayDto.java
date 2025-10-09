package com.example.booking.dto.payout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO cho thông tin chuyển khoản thủ công
 */
public class ManualPayDto {
    
    @NotBlank(message = "Mã tham chiếu không được để trống")
    @Size(max = 100, message = "Mã tham chiếu không được quá 100 ký tự")
    private String transferRef;
    
    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String note;
    
    @Size(max = 500, message = "URL chứng từ không được quá 500 ký tự")
    private String proofUrl;
    
    // Constructors
    public ManualPayDto() {}
    
    public ManualPayDto(String transferRef, String note, String proofUrl) {
        this.transferRef = transferRef;
        this.note = note;
        this.proofUrl = proofUrl;
    }
    
    // Getters and Setters
    public String getTransferRef() {
        return transferRef;
    }
    
    public void setTransferRef(String transferRef) {
        this.transferRef = transferRef;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getProofUrl() {
        return proofUrl;
    }
    
    public void setProofUrl(String proofUrl) {
        this.proofUrl = proofUrl;
    }
    
    @Override
    public String toString() {
        return "ManualPayDto{" +
                "transferRef='" + transferRef + '\'' +
                ", note='" + note + '\'' +
                ", proofUrl='" + proofUrl + '\'' +
                '}';
    }
}
