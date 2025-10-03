package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.example.booking.common.enums.PaymentType;

@Entity
@Table(name = "payment")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền không được âm")
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;
    
    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;
    
    // MoMo specific fields
    @Column(name = "momo_order_id")
    private String momoOrderId;
    
    @Column(name = "momo_request_id")
    private String momoRequestId;
    
    @Column(name = "momo_trans_id")
    private String momoTransId;
    
    @Column(name = "momo_result_code")
    private String momoResultCode;
    
    @Column(name = "momo_message")
    private String momoMessage;
    
    @Column(name = "pay_url")
    private String payUrl;
    
    @Column(name = "ipn_raw", columnDefinition = "jsonb")
    private String ipnRaw;
    
    @Column(name = "redirect_raw", columnDefinition = "jsonb")
    private String redirectRaw;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType = PaymentType.DEPOSIT;
    
    // Constructors
    public Payment() {
        this.paidAt = LocalDateTime.now();
    }
    
    public Payment(Customer customer, Booking booking, BigDecimal amount, 
                   PaymentMethod paymentMethod, PaymentStatus status, Voucher voucher) {
        this();
        this.customer = customer;
        this.booking = booking;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status != null ? status : PaymentStatus.PENDING;
        this.voucher = voucher;
    }
    
    // Getters and Setters
    public Integer getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public Voucher getVoucher() {
        return voucher;
    }
    
    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    // MoMo specific getters and setters
    public String getMomoOrderId() {
        return momoOrderId;
    }
    
    public void setMomoOrderId(String momoOrderId) {
        this.momoOrderId = momoOrderId;
    }
    
    public String getMomoRequestId() {
        return momoRequestId;
    }
    
    public void setMomoRequestId(String momoRequestId) {
        this.momoRequestId = momoRequestId;
    }
    
    public String getMomoTransId() {
        return momoTransId;
    }
    
    public void setMomoTransId(String momoTransId) {
        this.momoTransId = momoTransId;
    }
    
    public String getMomoResultCode() {
        return momoResultCode;
    }
    
    public void setMomoResultCode(String momoResultCode) {
        this.momoResultCode = momoResultCode;
    }
    
    public String getMomoMessage() {
        return momoMessage;
    }
    
    public void setMomoMessage(String momoMessage) {
        this.momoMessage = momoMessage;
    }
    
    public String getPayUrl() {
        return payUrl;
    }
    
    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }
    
    public String getIpnRaw() {
        return ipnRaw;
    }
    
    public void setIpnRaw(String ipnRaw) {
        this.ipnRaw = ipnRaw;
    }
    
    public String getRedirectRaw() {
        return redirectRaw;
    }
    
    public void setRedirectRaw(String redirectRaw) {
        this.redirectRaw = redirectRaw;
    }
    
    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }
    
    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
    
    public PaymentType getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }
}
