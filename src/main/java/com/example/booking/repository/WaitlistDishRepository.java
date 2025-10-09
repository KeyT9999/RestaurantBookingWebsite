package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.WaitlistDish;

@Repository
public interface WaitlistDishRepository extends JpaRepository<WaitlistDish, Integer> {
    
    /**
     * Find all dishes for a waitlist entry
     */
    List<WaitlistDish> findByWaitlistWaitlistId(Integer waitlistId);
    
    /**
     * Delete all dishes for a waitlist entry
     */
    void deleteByWaitlistWaitlistId(Integer waitlistId);
}
