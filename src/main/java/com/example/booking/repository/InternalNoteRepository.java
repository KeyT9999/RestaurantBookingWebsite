package com.example.booking.repository;

import com.example.booking.entity.InternalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InternalNote entity
 */
@Repository
public interface InternalNoteRepository extends JpaRepository<InternalNote, Long> {
    
    /**
     * Find internal notes by booking ID ordered by creation date descending
     * 
     * @param bookingId The booking ID
     * @return List of InternalNote entities ordered by creation date descending
     */
    List<InternalNote> findByBookingIdOrderByCreatedAtDesc(Integer bookingId);
}
