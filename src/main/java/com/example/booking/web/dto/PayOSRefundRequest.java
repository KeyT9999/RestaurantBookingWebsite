package com.example.booking.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO cho PayOS refund request
 */
public class PayOSRefundRequest {
    
    @JsonProperty("orderCode")
    private Long orderCode;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("reason")
    private String reason;
    
    public PayOSRefundRequest() {}
    
    public PayOSRefundRequest(Long orderCode, Long amount, String reason) {
        this.orderCode = orderCode;
        this.amount = amount;
        this.reason = reason;
    }
    
    public Long getOrderCode() {
        return orderCode;
    }
    
    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    @Override
    public String toString() {
        return "PayOSRefundRequest{" +
                "orderCode=" + orderCode +
                ", amount=" + amount +
                ", reason='" + reason + '\'' +
                '}';
    }
}
