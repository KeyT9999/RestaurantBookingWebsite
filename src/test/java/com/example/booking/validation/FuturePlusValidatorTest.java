package com.example.booking.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FuturePlusValidatorTest {

    private FuturePlusValidator validator = new FuturePlusValidator();

    static class Sample {
        @FuturePlus(minutes = 30)
        private LocalDateTime value;
    }

    @BeforeEach
    void initialise() throws Exception {
        Field field = Sample.class.getDeclaredField("value");
        FuturePlus annotation = field.getAnnotation(FuturePlus.class);
        validator.initialize(annotation);
    }

    @Test
    @DisplayName("should treat null values as valid")
    void shouldAcceptNull() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("should validate times at or after configured offset")
    void shouldValidateBoundary() {
        LocalDateTime boundary = LocalDateTime.now().plusMinutes(30).plusSeconds(5);
        assertThat(validator.isValid(boundary, null)).isTrue();

        LocalDateTime tooEarly = LocalDateTime.now().plusMinutes(29);
        assertThat(validator.isValid(tooEarly, null)).isFalse();
    }
}
