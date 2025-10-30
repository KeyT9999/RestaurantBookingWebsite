package com.example.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.entity.InternalNote;

class InternalNoteTest {

    // TC AN-005
    @Test
    @DisplayName("should set updatedAt on preUpdate (AN-005)")
    void preUpdate_setsTimestamp() {
        InternalNote note = new InternalNote(1, "c", "u");
        note.setUpdatedAt(null);
        note.preUpdate();
        assertThat(note.getUpdatedAt()).isNotNull();
    }
}


