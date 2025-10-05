package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "voucher_redemption")
public class VoucherRedemption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "redemption_id")
    private Integer redemptionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    @Column(name = "discount_applied", precision = 18, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "Giá trị giảm giá không được âm")
    private BigDecimal discountApplied;
    
    @Column(name = "amount_before", precision = 18, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "Số tiền trước giảm giá không được âm")
    private BigDecimal amountBefore;
    
    @Column(name = "amount_after", precision = 18, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "Số tiền sau giảm giá không được âm")
    private BigDecimal amountAfter;
    
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    private String meta;
    
    // Constructors
    public VoucherRedemption() {
        this.usedAt = LocalDateTime.now();
    }
    
    public VoucherRedemption(Voucher voucher, UUID customerId, BigDecimal discountApplied, 
                            BigDecimal amountBefore, BigDecimal amountAfter) {
        this();
        this.voucher = voucher;
        this.customerId = customerId;
        this.discountApplied = discountApplied;
        this.amountBefore = amountBefore;
        this.amountAfter = amountAfter;
    }
    
    // Getters and Setters
    public Integer getRedemptionId() {
        return redemptionId;
    }
    
    public void setRedemptionId(Integer redemptionId) {
        this.redemptionId = redemptionId;
    }
    
    public Voucher getVoucher() {
        return voucher;
    }
    
    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    
    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }
    
    public void setDiscountApplied(BigDecimal discountApplied) {
        this.discountApplied = discountApplied;
    }
    
    public BigDecimal getAmountBefore() {
        return amountBefore;
    }
    
    public void setAmountBefore(BigDecimal amountBefore) {
        this.amountBefore = amountBefore;
    }
    
    public BigDecimal getAmountAfter() {
        return amountAfter;
    }
    
    public void setAmountAfter(BigDecimal amountAfter) {
        this.amountAfter = amountAfter;
    }
    
    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
    
    public String getMeta() {
        return meta;
    }
    
    public void setMeta(String meta) {
        this.meta = meta;
    }
}
