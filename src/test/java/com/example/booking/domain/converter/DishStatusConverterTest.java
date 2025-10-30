package com.example.booking.domain.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.domain.DishStatus;

class DishStatusConverterTest {

    // TC DC-006
    @Test
    @DisplayName("convertToDatabaseColumn uses enum name (DC-006)")
    void toDb() {
        DishStatusConverter c = new DishStatusConverter();
        assertThat(c.convertToDatabaseColumn(DishStatus.OUT_OF_STOCK)).isEqualTo("OUT_OF_STOCK");
    }

    // TC DC-007
    @Test
    @DisplayName("convertToEntityAttribute handles synonyms (DC-007)")
    void toEntity_synonyms() {
        DishStatusConverter c = new DishStatusConverter();
        assertThat(c.convertToEntityAttribute("out_of_stock")).isEqualTo(DishStatus.OUT_OF_STOCK);
        assertThat(c.convertToEntityAttribute("outofstock")).isEqualTo(DishStatus.OUT_OF_STOCK);
        assertThat(c.convertToEntityAttribute("AVAILABLE")).isEqualTo(DishStatus.AVAILABLE);
    }

    // TC DC-008
    @Test
    @DisplayName("convertToEntityAttribute throws on invalid (DC-008)")
    void toEntity_invalid() {
        DishStatusConverter c = new DishStatusConverter();
        assertThatThrownBy(() -> c.convertToEntityAttribute("invalid")).isInstanceOf(IllegalArgumentException.class);
    }
}


