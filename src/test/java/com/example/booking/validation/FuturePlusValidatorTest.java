package com.example.booking.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FuturePlusValidator.
 * Coverage Target: 100%
 * Test Cases: 8
 *
 * @author Professional Test Engineer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FuturePlusValidator Tests")
class FuturePlusValidatorTest {

    @Mock
    private FuturePlus futurePlusAnnotation;

    @Mock
    private ConstraintValidatorContext context;

    private FuturePlusValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FuturePlusValidator();
    }

    @Nested
    @DisplayName("Default Minutes (30) Tests")
    class DefaultMinutesTests {

        @BeforeEach
        void setUp() {
            when(futurePlusAnnotation.minutes()).thenReturn(30);
            validator.initialize(futurePlusAnnotation);
        }

        @Test
        @DisplayName("Should return true for null value (let @NotNull handle it)")
        void isValid_NullValue_ReturnsTrue() {
            // When
            boolean result = validator.isValid(null, context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true for time exactly at minimum (30 minutes from now)")
        void isValid_ExactlyAtMinimum_ReturnsTrue() {
            // Given
            LocalDateTime value = LocalDateTime.now().plusMinutes(30);

            // When
            boolean result = validator.isValid(value, context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true for time after minimum (31 minutes from now)")
        void isValid_AfterMinimum_ReturnsTrue() {
            // Given
            LocalDateTime value = LocalDateTime.now().plusMinutes(31);

            // When
            boolean result = validator.isValid(value, context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true for time far in future (1 day from now)")
        void isValid_FarInFuture_ReturnsTrue() {
            // Given
            LocalDateTime value = LocalDateTime.now().plusDays(1);

            // When
            boolean result = validator.isValid(value, context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for time before minimum (29 minutes from now)")
        void isValid_BeforeMinimum_ReturnsFalse() {
            // Given
            LocalDateTime value = LocalDateTime.now().plusMinutes(29);

            // When
            boolean result = validator.isValid(value, context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for current time (0 minutes from now)")
        void isValid_CurrentTime_ReturnsFalse() {
            // Given
            LocalDateTime value = LocalDateTime.now();

            // When
            boolean result = validator.isValid(value, context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for past time")
        void isValid_PastTime_ReturnsFalse() {
            // Given
            LocalDateTime value = LocalDateTime.now().minusMinutes(10);

            // When
            boolean result = validator.isValid(value, context);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom Minutes Tests")
    class CustomMinutesTests {

        @Test
        @DisplayName("Should work with custom minutes = 60")
        void isValid_CustomMinutes60_WorksCorrectly() {
            // Given
            when(futurePlusAnnotation.minutes()).thenReturn(60);
            validator.initialize(futurePlusAnnotation);

            // When & Then - exactly 60 minutes
            assertThat(validator.isValid(LocalDateTime.now().plusMinutes(60), context)).isTrue();

            // When & Then - 61 minutes
            assertThat(validator.isValid(LocalDateTime.now().plusMinutes(61), context)).isTrue();

            // When & Then - 59 minutes
            assertThat(validator.isValid(LocalDateTime.now().plusMinutes(59), context)).isFalse();
        }
    }
}

