package com.example.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * DTO for MoMo payment creation request
 * Based on MoMo API documentation: POST /v2/gateway/api/create
 */
public class MoMoCreateRequest {
    
    // MoMo required fields
    @NotBlank(message = "Partner Code không được để trống")
    private String partnerCode;
    
    @NotBlank(message = "Request ID không được để trống")
    private String requestId;
    
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1000", message = "Số tiền tối thiểu là 1,000 VNĐ")
    private Long amount; // MoMo requires Long, not BigDecimal
    
    @NotBlank(message = "Order ID không được để trống")
    private String orderId;
    
    @NotBlank(message = "Thông tin đơn hàng không được để trống")
    @Size(max = 255, message = "Thông tin đơn hàng không được quá 255 ký tự")
    private String orderInfo;
    
    @NotBlank(message = "Redirect URL không được để trống")
    private String redirectUrl;
    
    @NotBlank(message = "IPN URL không được để trống")
    private String ipnUrl;
    
    @NotBlank(message = "Request Type không được để trống")
    private String requestType = "captureWallet";
    
    @NotBlank(message = "Signature không được để trống")
    private String signature;
    
    // Optional fields
    private String subPartnerCode;
    private String storeName;
    private String storeId;
    private Long orderGroupId;
    private String extraData = "";
    private String lang = "vi";
    private Boolean autoCapture = true;
    
    // Internal fields for processing
    @NotNull(message = "Booking ID không được để trống")
    private Integer bookingId;
    
    @Size(max = 100, message = "Mã giảm giá không được quá 100 ký tự")
    private String voucherCode;
    
    // Constructors
    public MoMoCreateRequest() {}
    
    public MoMoCreateRequest(Integer bookingId, Long amount, String orderInfo) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.orderInfo = orderInfo;
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
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderInfo() {
        return orderInfo;
    }
    
    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    
    public String getIpnUrl() {
        return ipnUrl;
    }
    
    public void setIpnUrl(String ipnUrl) {
        this.ipnUrl = ipnUrl;
    }
    
    public String getRequestType() {
        return requestType;
    }
    
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public String getSubPartnerCode() {
        return subPartnerCode;
    }
    
    public void setSubPartnerCode(String subPartnerCode) {
        this.subPartnerCode = subPartnerCode;
    }
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    public String getStoreId() {
        return storeId;
    }
    
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    
    public Long getOrderGroupId() {
        return orderGroupId;
    }
    
    public void setOrderGroupId(Long orderGroupId) {
        this.orderGroupId = orderGroupId;
    }
    
    public String getExtraData() {
        return extraData;
    }
    
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
    
    public String getLang() {
        return lang;
    }
    
    public void setLang(String lang) {
        this.lang = lang;
    }
    
    public Boolean getAutoCapture() {
        return autoCapture;
    }
    
    public void setAutoCapture(Boolean autoCapture) {
        this.autoCapture = autoCapture;
    }
    
    public Integer getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getVoucherCode() {
        return voucherCode;
    }
    
    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
    
    @Override
    public String toString() {
        return "MoMoCreateRequest{" +
                "partnerCode='" + partnerCode + '\'' +
                ", requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", orderId='" + orderId + '\'' +
                ", orderInfo='" + orderInfo + '\'' +
                ", redirectUrl='" + redirectUrl + '\'' +
                ", ipnUrl='" + ipnUrl + '\'' +
                ", requestType='" + requestType + '\'' +
                ", bookingId=" + bookingId +
                ", voucherCode='" + voucherCode + '\'' +
                '}';
    }
}
