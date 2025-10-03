package com.example.booking.dto;

/**
 * DTO for MoMo payment creation response
 * Based on MoMo API documentation: POST /v2/gateway/api/create response
 */
public class MoMoCreateResponse {
    
    private String partnerCode;
    private String requestId;
    private String orderId;
    private Long amount;
    private Long responseTime;
    private String message;
    private Integer resultCode;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private String deeplinkMiniApp;
    private String signature;
    private Long userFee;
    
    // Constructors
    public MoMoCreateResponse() {}
    
    public MoMoCreateResponse(String orderId, String requestId, String payUrl, Integer resultCode, String message) {
        this.orderId = orderId;
        this.requestId = requestId;
        this.payUrl = payUrl;
        this.resultCode = resultCode;
        this.message = message;
    }
    
    // Getters and Setters
    public String getPartnerCode() {
        return partnerCode;
    }
    
    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public Long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getResultCode() {
        return resultCode;
    }
    
    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }
    
    public String getPayUrl() {
        return payUrl;
    }
    
    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }
    
    public String getDeeplink() {
        return deeplink;
    }
    
    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }
    
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }
    
    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
    
    public String getDeeplinkMiniApp() {
        return deeplinkMiniApp;
    }
    
    public void setDeeplinkMiniApp(String deeplinkMiniApp) {
        this.deeplinkMiniApp = deeplinkMiniApp;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public Long getUserFee() {
        return userFee;
    }
    
    public void setUserFee(Long userFee) {
        this.userFee = userFee;
    }
    
    @Override
    public String toString() {
        return "MoMoCreateResponse{" +
                "partnerCode='" + partnerCode + '\'' +
                ", requestId='" + requestId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", responseTime=" + responseTime +
                ", message='" + message + '\'' +
                ", resultCode=" + resultCode +
                ", payUrl='" + payUrl + '\'' +
                ", deeplink='" + deeplink + '\'' +
                ", qrCodeUrl='" + qrCodeUrl + '\'' +
                ", deeplinkMiniApp='" + deeplinkMiniApp + '\'' +
                ", signature='" + signature + '\'' +
                ", userFee=" + userFee +
                '}';
    }
}
