package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.WaitlistTable;

@Repository
public interface WaitlistTableRepository extends JpaRepository<WaitlistTable, Integer> {
    
    /**
     * Find all tables for a waitlist entry
     */
    List<WaitlistTable> findByWaitlistWaitlistId(Integer waitlistId);
    
    /**
     * Delete all tables for a waitlist entry
     */
    void deleteByWaitlistWaitlistId(Integer waitlistId);
}
