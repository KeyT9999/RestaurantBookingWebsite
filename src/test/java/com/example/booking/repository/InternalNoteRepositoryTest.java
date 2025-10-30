package com.example.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.booking.entity.InternalNote;

@DataJpaTest
class InternalNoteRepositoryTest {

    @Autowired
    private InternalNoteRepository repository;

    // TC AN-001
    @Test
    @DisplayName("should return notes ordered by createdAt desc (AN-001)")
    void orderByCreatedAtDesc() {
        InternalNote n1 = new InternalNote(1, "a", "u");
        n1.setCreatedAt(LocalDateTime.now().minusHours(2));
        InternalNote n2 = new InternalNote(1, "b", "u");
        n2.setCreatedAt(LocalDateTime.now().minusHours(1));
        repository.saveAll(List.of(n1, n2));

        List<InternalNote> result = repository.findByBookingIdOrderByCreatedAtDesc(1);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("b");
    }

    // TC AN-002
    @Test
    @DisplayName("should return empty list for unknown booking (AN-002)")
    void emptyForUnknownBooking() {
        assertThat(repository.findByBookingIdOrderByCreatedAtDesc(999)).isEmpty();
    }
}


