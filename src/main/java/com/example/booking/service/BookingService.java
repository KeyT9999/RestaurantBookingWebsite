package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingStatus;
import com.example.booking.dto.BookingForm;
import com.example.booking.mapper.BookingMapper;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.RestaurantRepository;

@Service
@Transactional
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final DiningTableRepository diningTableRepository;
    private final BookingMapper bookingMapper;
    
    @Autowired
    public BookingService(BookingRepository bookingRepository,
                         RestaurantRepository restaurantRepository,
                         DiningTableRepository diningTableRepository,
                         BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
        this.diningTableRepository = diningTableRepository;
        this.bookingMapper = bookingMapper;
    }
    
    public Booking createBooking(BookingForm form, UUID customerId) {
        // Validate restaurant exists
        if (!restaurantRepository.existsById(form.getRestaurantId())) {
            throw new IllegalArgumentException("Nhà hàng không tồn tại");
        }
        
        // Validate table exists if specified
        if (form.getTableId() != null && !diningTableRepository.existsById(form.getTableId())) {
            throw new IllegalArgumentException("Bàn không tồn tại");
        }
        
        // Check for conflicting bookings if table is specified
        if (form.getTableId() != null) {
            validateNoConflictingBooking(form.getTableId(), form.getBookingTime(), null);
        }
        
        Booking booking = bookingMapper.toEntity(form, customerId);
        return bookingRepository.save(booking);
    }
    
    public Booking updateBooking(UUID bookingId, BookingForm form, UUID customerId) {
        Booking existingBooking = findById(bookingId);
        
        // Check ownership
        if (!existingBooking.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa booking này");
        }
        
        // Check if booking can be edited
        if (!existingBooking.canBeEdited()) {
            throw new IllegalArgumentException("Booking này không thể chỉnh sửa");
        }
        
        // Validate restaurant exists
        if (!restaurantRepository.existsById(form.getRestaurantId())) {
            throw new IllegalArgumentException("Nhà hàng không tồn tại");
        }
        
        // Validate table exists if specified
        if (form.getTableId() != null && !diningTableRepository.existsById(form.getTableId())) {
            throw new IllegalArgumentException("Bàn không tồn tại");
        }
        
        // Check for conflicting bookings if table is specified
        if (form.getTableId() != null) {
            validateNoConflictingBooking(form.getTableId(), form.getBookingTime(), bookingId);
        }
        
        bookingMapper.updateEntityFromForm(existingBooking, form);
        return bookingRepository.save(existingBooking);
    }
    
    public void cancelBooking(UUID bookingId, UUID customerId) {
        Booking booking = findById(bookingId);
        
        // Check ownership
        if (!booking.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy booking này");
        }
        
        // Check if booking can be cancelled
        if (!booking.canBeCancelled()) {
            throw new IllegalArgumentException("Booking này không thể hủy");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
    
    @Transactional(readOnly = true)
    public Booking findById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
    }
    
    @Transactional(readOnly = true)
    public List<Booking> findAllByCustomer(UUID customerId) {
        return bookingRepository.findByCustomerIdOrderByBookingTimeDesc(customerId);
    }
    
    @Transactional(readOnly = true)
    public List<Booking> findActiveBookingsByCustomer(UUID customerId) {
        return bookingRepository.findActiveBookingsByCustomer(customerId);
    }
    
    @Transactional(readOnly = true)
    public List<Booking> findByRestaurantAndDate(UUID restaurantId, LocalDateTime date) {
        return bookingRepository.findByRestaurantAndDate(restaurantId, date);
    }
    
    private void validateNoConflictingBooking(UUID tableId, LocalDateTime bookingTime, UUID excludeId) {
        LocalDateTime startTime = bookingTime.minusHours(1);
        LocalDateTime endTime = bookingTime.plusHours(1);
        UUID excludeBookingId = excludeId != null ? excludeId : UUID.randomUUID();
        
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                tableId, startTime, endTime, excludeBookingId);
        
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException(
                    "Bàn này đã được đặt trong khung thời gian từ " + 
                    startTime + " đến " + endTime + ". Vui lòng chọn thời gian khác.");
        }
    }
} 