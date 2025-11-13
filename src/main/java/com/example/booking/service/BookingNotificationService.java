package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.repository.NotificationRepository;

@Service
@Transactional
public class BookingNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Gửi thông báo khi có booking mới cho Restaurant Owner
     */
    public void notifyNewBookingToRestaurant(Booking booking) {
        try {
            UUID restaurantOwnerId = booking.getRestaurant().getOwner().getUser().getId();
            String restaurantName = booking.getRestaurant().getRestaurantName();
            String customerName = booking.getCustomer().getFullName();
            String bookingTime = booking.getBookingTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            createNotification(
                restaurantOwnerId,
                NotificationType.BOOKING_CONFIRMED,
                "Có đặt bàn mới",
                String.format("Khách hàng %s đã đặt bàn tại %s vào %s. Số khách: %d",
                    customerName, restaurantName, bookingTime, booking.getNumberOfGuests()),
                "/restaurant-owner/bookings/" + booking.getBookingId()
            );
            
            logger.info("✅ Sent new booking notification to restaurant owner for booking: {}", booking.getBookingId());
        } catch (Exception e) {
            logger.error("❌ Failed to send new booking notification to restaurant", e);
        }
    }
    
    /**
     * Gửi thông báo khi booking bị hủy cho Customer
     */
    public void notifyBookingCancelledToCustomer(Booking booking, String cancelReason) {
        try {
            UUID customerId = booking.getCustomer().getUser().getId();
            String restaurantName = booking.getRestaurant().getRestaurantName();
            String bookingTime = booking.getBookingTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            createNotification(
                customerId,
                NotificationType.BOOKING_CANCELLED,
                "Đặt bàn đã bị hủy",
                String.format("Đặt bàn tại %s vào %s đã bị hủy. Lý do: %s",
                    restaurantName, bookingTime, cancelReason != null ? cancelReason : "Không rõ"),
                "/booking/my"
            );
            
            logger.info("✅ Sent booking cancellation notification to customer for booking: {}", booking.getBookingId());
        } catch (Exception e) {
            logger.error("❌ Failed to send booking cancellation notification to customer", e);
        }
    }
    
    /**
     * Gửi thông báo khi booking bị hủy cho Restaurant Owner
     */
    public void notifyBookingCancelledToRestaurant(Booking booking, String cancelReason) {
        try {
            UUID restaurantOwnerId = booking.getRestaurant().getOwner().getUser().getId();
            String customerName = booking.getCustomer().getFullName();
            String bookingTime = booking.getBookingTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            createNotification(
                restaurantOwnerId,
                NotificationType.BOOKING_CANCELLED,
                "Đặt bàn đã bị hủy",
                String.format("Đặt bàn của khách hàng %s vào %s đã bị hủy. Lý do: %s",
                    customerName, bookingTime, cancelReason != null ? cancelReason : "Không rõ"),
                "/restaurant-owner/bookings/" + booking.getBookingId()
            );
            
            logger.info("✅ Sent booking cancellation notification to restaurant for booking: {}", booking.getBookingId());
        } catch (Exception e) {
            logger.error("❌ Failed to send booking cancellation notification to restaurant", e);
        }
    }
    
    /**
     * Gửi thông báo khi booking được xác nhận cho Customer
     */
    public void notifyBookingConfirmedToCustomer(Booking booking) {
        try {
            UUID customerId = booking.getCustomer().getUser().getId();
            String restaurantName = booking.getRestaurant().getRestaurantName();
            String bookingTime = booking.getBookingTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            createNotification(
                customerId,
                NotificationType.BOOKING_CONFIRMED,
                "Đặt bàn đã được xác nhận",
                String.format("Đặt bàn tại %s vào %s đã được nhà hàng xác nhận. Vui lòng đến đúng giờ!",
                    restaurantName, bookingTime),
                "/booking/my"
            );
            
            logger.info("✅ Sent booking confirmation notification to customer for booking: {}", booking.getBookingId());
        } catch (Exception e) {
            logger.error("❌ Failed to send booking confirmation notification to customer", e);
        }
    }
    
    /**
     * Gửi thông báo khi booking hoàn tất cho Customer
     */
    public void notifyBookingCompletedToCustomer(Booking booking) {
        try {
            UUID customerId = booking.getCustomer().getUser().getId();
            String restaurantName = booking.getRestaurant().getRestaurantName();
            
            createNotification(
                customerId,
                NotificationType.BOOKING_CONFIRMED,
                "Đặt bàn đã hoàn tất",
                String.format("Đặt bàn tại %s đã hoàn tất. Cảm ơn bạn đã sử dụng dịch vụ!",
                    restaurantName),
                "/booking/my"
            );
            
            logger.info("✅ Sent booking completion notification to customer for booking: {}", booking.getBookingId());
        } catch (Exception e) {
            logger.error("❌ Failed to send booking completion notification to customer", e);
        }
    }
    
    /**
     * Gửi thông báo khi booking hoàn tất cho Restaurant Owner
     */
    public void notifyBookingCompletedToRestaurant(Booking booking) {
        try {
            UUID restaurantOwnerId = booking.getRestaurant().getOwner().getUser().getId();
            String customerName = booking.getCustomer().getFullName();
            
            createNotification(
                restaurantOwnerId,
                NotificationType.BOOKING_CONFIRMED,
                "Đặt bàn đã hoàn tất",
                String.format("Đặt bàn của khách hàng %s đã hoàn tất.",
                    customerName),
                "/restaurant-owner/bookings/" + booking.getBookingId()
            );
            
            logger.info("✅ Sent booking completion notification to restaurant for booking: {}", booking.getBookingId());
        } catch (Exception e) {
            logger.error("❌ Failed to send booking completion notification to restaurant", e);
        }
    }
    
    private void createNotification(UUID userId, NotificationType type, String title, String content, String linkUrl) {
        Notification notification = new Notification();
        notification.setRecipientUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLinkUrl(linkUrl);
        notification.setStatus(NotificationStatus.SENT);
        notification.setPriority(1);
        notification.setPublishAt(LocalDateTime.now());
        notification.setExpireAt(LocalDateTime.now().plusDays(30));
        
        notificationRepository.save(notification);
    }
}

