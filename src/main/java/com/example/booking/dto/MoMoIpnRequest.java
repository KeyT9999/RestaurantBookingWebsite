package com.example.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for MoMo IPN (Instant Payment Notification) request
 */
public class MoMoIpnRequest {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private String amount;
    
    @JsonProperty("orderInfo")
    private String orderInfo;
    
    @JsonProperty("orderType")
    private String orderType;
    
    @JsonProperty("transId")
    private String transId;
    
    @JsonProperty("resultCode")
    private String resultCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("payType")
    private String payType;
    
    @JsonProperty("responseTime")
    private String responseTime;
    
    @JsonProperty("extraData")
    private String extraData;
    
    @JsonProperty("signature")
    private String signature;
    
    // Constructors
    public MoMoIpnRequest() {}
    
    // Getters and Setters
    public String getPartnerCode() {
        return partnerCode;
    }
    
    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getAmount() {
        return amount;
    }
    
    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    public String getOrderInfo() {
        return orderInfo;
    }
    
    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
    
    public String getOrderType() {
        return orderType;
    }
    
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
    
    public String getTransId() {
        return transId;
    }
    
    public void setTransId(String transId) {
        this.transId = transId;
    }
    
    public String getResultCode() {
        return resultCode;
    }
    
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPayType() {
        return payType;
    }
    
    public void setPayType(String payType) {
        this.payType = payType;
    }
    
    public String getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getExtraData() {
        return extraData;
    }
    
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    @Override
    public String toString() {
        return "MoMoIpnRequest{" +
                "partnerCode='" + partnerCode + '\'' +
                ", orderId='" + orderId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", amount='" + amount + '\'' +
                ", orderInfo='" + orderInfo + '\'' +
                ", orderType='" + orderType + '\'' +
                ", transId='" + transId + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", message='" + message + '\'' +
                ", payType='" + payType + '\'' +
                ", responseTime='" + responseTime + '\'' +
                ", extraData='" + extraData + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
