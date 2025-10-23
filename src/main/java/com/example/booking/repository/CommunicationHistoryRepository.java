package com.example.booking.repository;

import com.example.booking.entity.CommunicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CommunicationHistory entity
 */
@Repository
public interface CommunicationHistoryRepository extends JpaRepository<CommunicationHistory, Long> {
    
    /**
     * Find communication history by booking ID ordered by timestamp descending
     * 
     * @param bookingId The booking ID
     * @return List of CommunicationHistory entities ordered by timestamp descending
     */
    List<CommunicationHistory> findByBookingIdOrderByTimestampDesc(Integer bookingId);
}
