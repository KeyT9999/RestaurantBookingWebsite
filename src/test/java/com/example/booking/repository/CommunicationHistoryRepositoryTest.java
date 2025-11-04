package com.example.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.booking.entity.CommunicationHistory;
import com.example.booking.entity.CommunicationHistory.CommunicationDirection;
import com.example.booking.entity.CommunicationHistory.CommunicationStatus;
import com.example.booking.entity.CommunicationHistory.CommunicationType;

@DataJpaTest
class CommunicationHistoryRepositoryTest {

    @Autowired
    private CommunicationHistoryRepository repository;

    // TC AN-003
    @Test
    @DisplayName("should order communications by timestamp desc (AN-003)")
    void orderByTimestampDesc() {
        CommunicationHistory c1 = new CommunicationHistory(1, CommunicationType.MESSAGE, "hi", CommunicationDirection.OUTGOING, "a", CommunicationStatus.SENT);
        c1.setTimestamp(LocalDateTime.now().minusMinutes(10));
        CommunicationHistory c2 = new CommunicationHistory(1, CommunicationType.MESSAGE, "hello", CommunicationDirection.OUTGOING, "a", CommunicationStatus.SENT);
        c2.setTimestamp(LocalDateTime.now().minusMinutes(5));
        repository.saveAll(List.of(c1, c2));

        List<CommunicationHistory> result = repository.findByBookingIdOrderByTimestampDesc(1);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("hello");
    }

    // TC AN-004
    @Test
    @DisplayName("should handle null bookingId (AN-004)")
    void nullBookingId() {
        // Behavior depends on Spring Data/JPA; expect IllegalArgumentException typically
        assertThrows(IllegalArgumentException.class, () -> repository.findByBookingIdOrderByTimestampDesc(null));
    }
}


