package com.example.booking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Waitlist;
import com.example.booking.domain.WaitlistStatus;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Integer> {
    
    /**
     * Lấy waitlist theo nhà hàng, sắp xếp theo thời gian join
     */
    List<Waitlist> findByRestaurantIdAndStatusOrderByJoinTimeAsc(Integer restaurantId, WaitlistStatus status);
    
    /**
     * Lấy waitlist của customer với multiple status
     */
    List<Waitlist> findByCustomerCustomerIdAndStatusIn(UUID customerId, List<WaitlistStatus> statuses);
    
    /**
     * Lấy waitlist của customer với single status
     */
    List<Waitlist> findByCustomerCustomerIdAndStatus(UUID customerId, WaitlistStatus status);
    
    /**
     * Lấy customer đầu tiên trong waitlist
     */
    Optional<Waitlist> findFirstByRestaurantIdAndStatusOrderByJoinTimeAsc(Integer restaurantId, WaitlistStatus status);
    
    /**
     * Đếm số người trong waitlist
     */
    long countByRestaurantIdAndStatus(Integer restaurantId, WaitlistStatus status);
    
    /**
     * Kiểm tra customer đã có trong waitlist chưa
     */
    boolean existsByCustomerCustomerIdAndRestaurantIdAndStatus(UUID customerId, Integer restaurantId, WaitlistStatus status);
    
    /**
     * Lấy tất cả waitlist entries của customer (cho booking list)
     */
    List<Waitlist> findByCustomerCustomerIdOrderByJoinTimeDesc(UUID customerId);
}
