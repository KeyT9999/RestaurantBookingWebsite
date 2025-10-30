package com.example.booking.service;

import com.example.booking.common.enums.RefundStatus;
import com.example.booking.domain.*;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.RefundRequestRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnhancedRefundServiceUnitTest {
    @Mock private PaymentRepository paymentRepository;
    @Mock private RestaurantBalanceRepository balanceRepository;
    @Mock private RefundRequestRepository refundRequestRepository;

    @InjectMocks private EnhancedRefundService enhancedRefundService;

    private Payment completedPayosPayment;
    private RestaurantBalance balance;

    @BeforeEach
    void setup() {
        completedPayosPayment = new Payment();
        completedPayosPayment.setPaymentId(5);
        completedPayosPayment.setStatus(PaymentStatus.COMPLETED);
        completedPayosPayment.setAmount(new BigDecimal("500000"));
        completedPayosPayment.setCustomer(new Customer());
        Booking booking = new Booking();
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(10);
        booking.setRestaurant(restaurant);
        completedPayosPayment.setBooking(booking);
        completedPayosPayment.setPaymentMethod(PaymentMethod.PAYOS);

        balance = new RestaurantBalance();
        balance.setAvailableBalance(new BigDecimal("1000000"));

        lenient().when(paymentRepository.findById(5)).thenReturn(Optional.of(completedPayosPayment));
        lenient().when(balanceRepository.findByRestaurantRestaurantId(10)).thenReturn(Optional.of(balance));
        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void processRefund_with_commission_deduction_payos_ok() {
        var updated = enhancedRefundService.processRefundWithCommissionDeduction(5, new BigDecimal("200000"), "reason");
        assertThat(updated.getRefundAmount()).isEqualTo(new BigDecimal("200000"));
    }

    @Test
    void processRefund_unsupported_method_card_should_throw_wrapped_runtime() {
        completedPayosPayment.setPaymentMethod(PaymentMethod.CARD);
        assertThrows(RuntimeException.class, () ->
                enhancedRefundService.processRefundWithCommissionDeduction(5, new BigDecimal("100000"), "reason"));
    }

    @Test
    void processRefund_invalid_amount_should_throw() {
        assertThrows(IllegalArgumentException.class, () ->
                enhancedRefundService.processRefundWithCommissionDeduction(5, new BigDecimal("600000"), "reason"));
        assertThrows(IllegalArgumentException.class, () ->
                enhancedRefundService.processRefundWithCommissionDeduction(5, new BigDecimal("0"), "reason"));
    }
}
