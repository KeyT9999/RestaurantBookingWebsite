package com.example.booking.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO cho PayOS refund response
 */
public class PayOSRefundResponse {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("desc")
    private String desc;
    
    @JsonProperty("data")
    private RefundData data;
    
    public PayOSRefundResponse() {}
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public RefundData getData() {
        return data;
    }
    
    public void setData(RefundData data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "PayOSRefundResponse{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                ", data=" + data +
                '}';
    }
    
    /**
     * Inner class for refund data
     */
    public static class RefundData {
        
        @JsonProperty("refundId")
        private String refundId;
        
        @JsonProperty("orderCode")
        private Long orderCode;
        
        @JsonProperty("amount")
        private Long amount;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("refundedAt")
        private String refundedAt;
        
        public RefundData() {}
        
        public String getRefundId() {
            return refundId;
        }
        
        public void setRefundId(String refundId) {
            this.refundId = refundId;
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
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getRefundedAt() {
            return refundedAt;
        }
        
        public void setRefundedAt(String refundedAt) {
            this.refundedAt = refundedAt;
        }
        
        @Override
        public String toString() {
            return "RefundData{" +
                    "refundId='" + refundId + '\'' +
                    ", orderCode=" + orderCode +
                    ", amount=" + amount +
                    ", status='" + status + '\'' +
                    ", refundedAt='" + refundedAt + '\'' +
                    '}';
        }
    }
}
