package com.example.booking.test.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import java.time.LocalDate;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Dish;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RefundRequest;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.dto.BookingForm;

/**
 * Simplified test utilities for creating mock objects and test data
 */
public class TestDataFactory {

    // ==================== CUSTOMER FACTORY ====================

    public static Customer createTestCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setFullName("Test Customer");
        return customer;
    }

    // ==================== RESTAURANT FACTORY ====================

    public static RestaurantProfile createTestRestaurant() {
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("123 Test Street");
        restaurant.setPhone("0987654321");
        return restaurant;
    }

    public static RestaurantProfile createTestRestaurant(String name) {
        RestaurantProfile restaurant = createTestRestaurant();
        restaurant.setRestaurantName(name);
        return restaurant;
    }

    // ==================== TABLE FACTORY ====================

    public static RestaurantTable createTestTable(RestaurantProfile restaurant) {
        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setRestaurant(restaurant);
        return table;
    }

    // ==================== DISH FACTORY ====================

    public static Dish createTestDish(RestaurantProfile restaurant) {
        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setName("Test Dish");
        dish.setDescription("Test dish description");
        dish.setPrice(new BigDecimal("50000"));
        dish.setRestaurant(restaurant);
        return dish;
    }

    // ==================== BOOKING FACTORY ====================

    public static Booking createTestBooking() {
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setBookingTime(LocalDateTime.now().plusDays(1));
        booking.setDepositAmount(new BigDecimal("100000"));
        booking.setStatus(BookingStatus.PENDING);
        booking.setNote("Test booking");
        return booking;
    }

    public static Booking createTestBooking(Customer customer, RestaurantProfile restaurant) {
        Booking booking = createTestBooking();
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        return booking;
    }

    // ==================== BOOKING FORM FACTORY ====================

    public static BookingForm createValidBookingForm() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setNote("Test booking");
        return form;
    }

    public static BookingForm createValidBookingForm(Integer restaurantId, Integer tableId) {
        BookingForm form = createValidBookingForm();
        form.setRestaurantId(restaurantId);
        form.setTableId(tableId);
        return form;
    }

    // ==================== USER FACTORY ====================

    public static User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        return user;
    }

    public static User createTestUser(UserRole role) {
        User user = createTestUser();
        user.setRole(role);
        return user;
    }

    // ==================== UTILITY METHODS ====================

    public static LocalDateTime getFutureDateTime(int daysFromNow) {
        return LocalDateTime.now().plusDays(daysFromNow);
    }

    public static LocalDateTime getPastDateTime(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo);
    }

    public static BigDecimal createPrice(String amount) {
        return new BigDecimal(amount);
    }

    public static UUID createRandomUUID() {
        return UUID.randomUUID();
    }

    // ==================== VOUCHER FACTORY ====================

    public static Voucher createTestVoucher() {
        Voucher voucher = new Voucher();
        voucher.setVoucherId(1);
        voucher.setCode("TEST20");
        voucher.setDescription("Test voucher 20% off");
        voucher.setDiscountType(DiscountType.PERCENT);
        voucher.setDiscountValue(new BigDecimal("20"));
        voucher.setStartDate(LocalDate.now().minusDays(1));
        voucher.setEndDate(LocalDate.now().plusDays(30));
        voucher.setGlobalUsageLimit(100);
        voucher.setPerCustomerLimit(3);
        voucher.setMinOrderAmount(new BigDecimal("100000"));
        voucher.setMaxDiscountAmount(new BigDecimal("50000"));
        voucher.setStatus(VoucherStatus.ACTIVE);
        return voucher;
    }

    public static Voucher createTestVoucher(String code, DiscountType discountType) {
        Voucher voucher = createTestVoucher();
        voucher.setCode(code);
        voucher.setDiscountType(discountType);
        return voucher;
    }

    // ==================== PAYMENT FACTORY ====================

    public static Payment createTestPayment() {
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setOrderCode(1001L);
        payment.setAmount(new BigDecimal("100000"));
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentType(PaymentType.DEPOSIT);
        return payment;
    }

    public static Payment createTestPayment(PaymentStatus status, PaymentType paymentType) {
        Payment payment = createTestPayment();
        payment.setStatus(status);
        payment.setPaymentType(paymentType);
        return payment;
    }

    // ==================== REFUND REQUEST FACTORY ====================

    public static RefundRequest createTestRefundRequest() {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setRefundRequestId(1);
        refundRequest.setAmount(new BigDecimal("100000"));
        refundRequest.setReason("Customer cancelled");
        refundRequest.setCustomerBankCode("VCB");
        refundRequest.setCustomerAccountNumber("1234567890");
        refundRequest.setCustomerAccountHolder("Test Customer");
        return refundRequest;
    }

    public static RefundRequest createTestRefundRequest(BigDecimal amount) {
        RefundRequest refundRequest = createTestRefundRequest();
        refundRequest.setAmount(amount);
        return refundRequest;
    }
}