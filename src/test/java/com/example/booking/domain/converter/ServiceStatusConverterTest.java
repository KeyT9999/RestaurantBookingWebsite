package com.example.booking.domain.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.ServiceStatus;

class ServiceStatusConverterTest {

    // TC DC-003
    @Test
    @DisplayName("convertToDatabaseColumn uses enum name (DC-003)")
    void toDb() {
        ServiceStatusConverter c = new ServiceStatusConverter();
        assertThat(c.convertToDatabaseColumn(ServiceStatus.DISCONTINUED)).isEqualTo("DISCONTINUED");
    }

    // TC DC-004
    @Test
    @DisplayName("convertToEntityAttribute throws on invalid (DC-004)")
    void toEntity_invalid() {
        ServiceStatusConverter c = new ServiceStatusConverter();
        assertThatThrownBy(() -> c.convertToEntityAttribute("invalid-status")).isInstanceOf(IllegalArgumentException.class);
    }

    // TC DC-005
    @Test
    @DisplayName("convertToEntityAttribute handles lowercase (DC-005)")
    void toEntity_lowercase() {
        ServiceStatusConverter c = new ServiceStatusConverter();
        assertThat(c.convertToEntityAttribute("available")).isEqualTo(ServiceStatus.AVAILABLE);
        assertThat(c.convertToEntityAttribute("unavailable")).isEqualTo(ServiceStatus.UNAVAILABLE);
    }
}


