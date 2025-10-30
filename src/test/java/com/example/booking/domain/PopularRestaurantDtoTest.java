package com.example.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.dto.PopularRestaurantDto;

class PopularRestaurantDtoTest {

    // TC HL-010
    @Test
    @DisplayName("should format rating to one decimal (HL-010)")
    void formattedRating() {
        PopularRestaurantDto dto = new PopularRestaurantDto(1, "A", "Viet", "Addr", 4.05, 10, "$", null, null, "g");
        assertThat(dto.getFormattedRating()).isEqualTo("4.1");
    }

    // TC HL-011
    @Test
    @DisplayName("should detect cover image presence (HL-011)")
    void hasCover() {
        PopularRestaurantDto dto1 = new PopularRestaurantDto(1, "A", "Viet", "Addr", 4.0, 10, "$", null, null, "g");
        PopularRestaurantDto dto2 = new PopularRestaurantDto(1, "A", "Viet", "Addr", 4.0, 10, "$", null, " ", "g");
        PopularRestaurantDto dto3 = new PopularRestaurantDto(1, "A", "Viet", "Addr", 4.0, 10, "$", null, "url", "g");
        assertThat(dto1.hasCoverImage()).isFalse();
        assertThat(dto2.hasCoverImage()).isFalse();
        assertThat(dto3.hasCoverImage()).isTrue();
    }

    // TC HL-012
    @Test
    @DisplayName("should clamp and round rating to 0..5 (HL-012)")
    void roundedRating() {
        PopularRestaurantDto d1 = new PopularRestaurantDto(1, "A", "Viet", "Addr", -1.0, 0, "$", null, null, "g");
        PopularRestaurantDto d2 = new PopularRestaurantDto(1, "A", "Viet", "Addr", 5.7, 0, "$", null, null, "g");
        PopularRestaurantDto d3 = new PopularRestaurantDto(1, "A", "Viet", "Addr", 3.49, 0, "$", null, null, "g");
        assertThat(d1.getRoundedRating()).isEqualTo(0);
        assertThat(d2.getRoundedRating()).isEqualTo(5);
        assertThat(d3.getRoundedRating()).isEqualTo(3);
    }
}


