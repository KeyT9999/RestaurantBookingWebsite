package com.example.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.entity.CommunicationHistory;
import com.example.booking.entity.CommunicationHistory.CommunicationDirection;
import com.example.booking.entity.CommunicationHistory.CommunicationStatus;
import com.example.booking.entity.CommunicationHistory.CommunicationType;

class CommunicationHistoryTest {

    // TC AN-006
    @Test
    @DisplayName("entity getters and setters round-trip (AN-006)")
    void roundTrip() {
        CommunicationHistory c = new CommunicationHistory(1, CommunicationType.EMAIL, "hello", CommunicationDirection.INCOMING, "u", CommunicationStatus.DELIVERED);
        LocalDateTime ts = LocalDateTime.now();
        c.setTimestamp(ts);
        assertThat(c.getBookingId()).isEqualTo(1);
        assertThat(c.getType()).isEqualTo(CommunicationType.EMAIL);
        assertThat(c.getContent()).isEqualTo("hello");
        assertThat(c.getDirection()).isEqualTo(CommunicationDirection.INCOMING);
        assertThat(c.getAuthor()).isEqualTo("u");
        assertThat(c.getStatus()).isEqualTo(CommunicationStatus.DELIVERED);
        assertThat(c.getTimestamp()).isEqualTo(ts);
    }
}


