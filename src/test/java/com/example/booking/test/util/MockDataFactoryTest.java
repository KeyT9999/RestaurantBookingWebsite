package com.example.booking.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Dish;
import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.BookingForm;

/**
 * Demo test để verify MockDataFactory hoạt động đúng
 */
class MockDataFactoryTest {
    
    @Test
    void testCreateMockCustomer() {
        // Given & When
        Customer customer = MockDataFactory.createMockCustomer();
        
        // Then
        assertNotNull(customer);
        assertNotNull(customer.getCustomerId());
        assertEquals("John Doe", customer.getFullName());
    }
    
    @Test
    void testCreateMockRestaurant() {
        // Given & When
        RestaurantProfile restaurant = MockDataFactory.createMockRestaurant();
        
        // Then
        assertNotNull(restaurant);
        assertEquals(1, restaurant.getRestaurantId());
        assertEquals("Test Restaurant", restaurant.getRestaurantName());
        assertEquals("0987654321", restaurant.getPhone());
    }
    
    @Test
    void testCreateMockTable() {
        // Given & When
        RestaurantTable table = MockDataFactory.createMockTable();
        
        // Then
        assertNotNull(table);
        assertEquals(1, table.getTableId());
        assertEquals("Table 1", table.getTableName());
        assertEquals(4, table.getCapacity());
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
    }
    
    @Test
    void testCreateMockBooking() {
        // Given & When
        Booking booking = MockDataFactory.createMockBooking();
        
        // Then
        assertNotNull(booking);
        assertEquals(1, booking.getBookingId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(4, booking.getNumberOfGuests());
        assertNotNull(booking.getDepositAmount());
    }
    
    @Test
    void testCreateMockBookingForm() {
        // Given & When
        BookingForm form = MockDataFactory.createMockBookingForm();
        
        // Then
        assertNotNull(form);
        assertEquals(1, form.getRestaurantId());
        assertEquals(1, form.getTableId());
        assertEquals(4, form.getGuestCount());
        assertNotNull(form.getBookingTime());
        assertNotNull(form.getDepositAmount());
    }
    
    @Test
    void testCreateMockUser() {
        // Given & When
        User user = MockDataFactory.createMockUser();
        
        // Then
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("John Doe", user.getFullName());
        assertEquals(UserRole.CUSTOMER, user.getRole());
        assertTrue(user.getActive());
    }
    
    @Test
    void testCreateMockPayment() {
        // Given & When
        Payment payment = MockDataFactory.createMockPayment();
        
        // Then
        assertNotNull(payment);
        assertEquals(1, payment.getPaymentId());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        assertNotNull(payment.getAmount());
    }
    
    @Test
    void testCreateMockNotification() {
        // Given & When
        Notification notification = MockDataFactory.createMockNotification();
        
        // Then
        assertNotNull(notification);
        assertEquals(1, notification.getNotificationId());
        assertEquals("Booking Confirmation", notification.getTitle());
        assertEquals("Your booking has been confirmed", notification.getContent());
        assertEquals(NotificationType.BOOKING_CONFIRMED, notification.getType());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
    }
    
    @Test
    void testCreateMockDish() {
        // Given & When
        Dish dish = MockDataFactory.createMockDish();
        
        // Then
        assertNotNull(dish);
        assertEquals(1, dish.getDishId());
        assertEquals("Test Dish", dish.getName());
        assertEquals("A delicious test dish", dish.getDescription());
        assertNotNull(dish.getPrice());
        assertEquals("Main Course", dish.getCategory());
    }
    
    @Test
    void testUtilityMethods() {
        // Given & When
        var futureTime = MockDataFactory.getFutureDateTime(1);
        var pastTime = MockDataFactory.getPastDateTime(1);
        var price = MockDataFactory.createPrice("100000.00");
        var uuid = MockDataFactory.createRandomUUID();
        
        // Then
        assertNotNull(futureTime);
        assertNotNull(pastTime);
        assertNotNull(price);
        assertNotNull(uuid);
        
        assertTrue(futureTime.isAfter(pastTime));
        assertEquals(19, futureTime.getHour());
        assertEquals(0, futureTime.getMinute());
    }
}
