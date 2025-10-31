package com.example.booking.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class FuturePlusValidatorTest {

    private FuturePlusValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new FuturePlusValidator();
        FuturePlus futurePlus = mock(FuturePlus.class);
        when(futurePlus.minutes()).thenReturn(30);

        validator.initialize(futurePlus);
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void shouldReturnTrueWhenValueIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void shouldReturnTrueWhenValueIsExactlyAtMinimumTime() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime minimumValue = fixedNow.plusMinutes(30);

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(fixedNow);

            assertTrue(validator.isValid(minimumValue, context));
        }
    }

    @Test
    void shouldReturnTrueWhenValueIsAfterMinimumTime() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime futureValue = fixedNow.plusMinutes(45);

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(fixedNow);

            assertTrue(validator.isValid(futureValue, context));
        }
    }

    @Test
    void shouldReturnFalseWhenValueIsBeforeMinimumTime() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime pastValue = fixedNow.plusMinutes(25);

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(fixedNow);

            assertFalse(validator.isValid(pastValue, context));
        }
    }
}

