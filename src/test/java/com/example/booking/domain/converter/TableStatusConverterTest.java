package com.example.booking.domain.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.TableStatus;

class TableStatusConverterTest {

    // TC DC-001
    @Test
    @DisplayName("convertToDatabaseColumn returns lowercase value (DC-001)")
    void toDb() {
        TableStatusConverter c = new TableStatusConverter();
        assertThat(c.convertToDatabaseColumn(TableStatus.AVAILABLE)).isEqualTo("available");
    }

    // TC DC-002
    @Test
    @DisplayName("convertToEntityAttribute handles case-insensitive (DC-002)")
    void toEntity() {
        TableStatusConverter c = new TableStatusConverter();
        assertThat(c.convertToEntityAttribute("AVAILABLE")).isEqualTo(TableStatus.AVAILABLE);
        assertThat(c.convertToEntityAttribute("available")).isEqualTo(TableStatus.AVAILABLE);
    }
}


