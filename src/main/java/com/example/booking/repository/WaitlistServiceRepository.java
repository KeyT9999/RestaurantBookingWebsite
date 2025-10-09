package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.WaitlistServiceItem;

@Repository
public interface WaitlistServiceRepository extends JpaRepository<WaitlistServiceItem, Integer> {
    
    /**
     * Find all services for a waitlist entry
     */
    List<WaitlistServiceItem> findByWaitlistWaitlistId(Integer waitlistId);
    
    /**
     * Delete all services for a waitlist entry
     */
    void deleteByWaitlistWaitlistId(Integer waitlistId);
}
