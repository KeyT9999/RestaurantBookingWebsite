package com.example.booking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DtosTest {

    // TC AN-007
    @Test
    @DisplayName("InternalNoteDto preserves nulls (AN-007)")
    void internalNoteDto_nulls() {
        InternalNoteDto dto = new InternalNoteDto();
        dto.setId(1L);
        dto.setContent("c");
        dto.setAuthor("a");
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getUpdatedAt()).isNull();
    }

    // TC AN-008
    @Test
    @DisplayName("CommunicationHistoryDto field round-trip (AN-008)")
    void communicationHistoryDto_roundTrip() {
        LocalDateTime now = LocalDateTime.now();
        CommunicationHistoryDto dto = new CommunicationHistoryDto(1L, "MESSAGE", "hi", "OUTGOING", now, "u", "SENT");
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getType()).isEqualTo("MESSAGE");
        assertThat(dto.getContent()).isEqualTo("hi");
        assertThat(dto.getDirection()).isEqualTo("OUTGOING");
        assertThat(dto.getTimestamp()).isEqualTo(now);
        assertThat(dto.getAuthor()).isEqualTo("u");
        assertThat(dto.getStatus()).isEqualTo("SENT");
    }
}


