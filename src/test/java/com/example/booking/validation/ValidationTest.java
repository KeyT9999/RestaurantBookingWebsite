package com.example.booking.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Validation: FuturePlus annotation and FuturePlusValidator
 */
class ValidationTest {

    private FuturePlusValidator validator;
    private FuturePlus annotation;

    @BeforeEach
    void setUp() {
        validator = new FuturePlusValidator();
        // Create annotation using reflection proxy or test annotation
        annotation = createFuturePlusAnnotation(30);
    }

    private FuturePlus createFuturePlusAnnotation(int minutes) {
        return new FuturePlus() {
            @Override
            public String message() {
                return "Thời gian đặt bàn phải từ {minutes} phút trở lên so với hiện tại";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public int minutes() {
                return minutes;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return FuturePlus.class;
            }
        };
    }

    @Test
    void testFuturePlusValidator_Initialize_WithDefaultMinutes() {
        validator.initialize(annotation);
        // Validator initialized successfully
        assertThat(validator).isNotNull();
    }

    @Test
    void testFuturePlusValidator_Initialize_WithCustomMinutes() {
        FuturePlus customAnnotation = createFuturePlusAnnotation(60);
        
        validator.initialize(customAnnotation);
        assertThat(validator).isNotNull();
    }

    @Test
    void testFuturePlusValidator_IsValid_NullValue() {
        validator.initialize(annotation);
        boolean result = validator.isValid(null, null);
        
        // Null values should return true (let @NotNull handle null validation)
        assertThat(result).isTrue();
    }

    @Test
    void testFuturePlusValidator_IsValid_FutureTimeExceedsMinimum() {
        validator.initialize(annotation);
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(60);
        
        boolean result = validator.isValid(futureTime, null);
        
        assertThat(result).isTrue();
    }

    @Test
    void testFuturePlusValidator_IsValid_FutureTimeEqualsMinimum() {
        validator.initialize(annotation);
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(30);
        
        boolean result = validator.isValid(futureTime, null);
        
        assertThat(result).isTrue();
    }

    @Test
    void testFuturePlusValidator_IsValid_FutureTimeLessThanMinimum() {
        validator.initialize(annotation);
        LocalDateTime pastTime = LocalDateTime.now().plusMinutes(15);
        
        boolean result = validator.isValid(pastTime, null);
        
        assertThat(result).isFalse();
    }

    @Test
    void testFuturePlusValidator_IsValid_PastTime() {
        validator.initialize(annotation);
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(30);
        
        boolean result = validator.isValid(pastTime, null);
        
        assertThat(result).isFalse();
    }

    @Test
    void testFuturePlusValidator_IsValid_WithCustomMinutes() {
        FuturePlus customAnnotation = createFuturePlusAnnotation(120); // 2 hours
        
        validator.initialize(customAnnotation);
        
        LocalDateTime futureTime1 = LocalDateTime.now().plusMinutes(60); // Less than minimum
        LocalDateTime futureTime2 = LocalDateTime.now().plusMinutes(120); // Exactly minimum
        LocalDateTime futureTime3 = LocalDateTime.now().plusMinutes(180); // More than minimum
        
        assertThat(validator.isValid(futureTime1, null)).isFalse();
        assertThat(validator.isValid(futureTime2, null)).isTrue();
        assertThat(validator.isValid(futureTime3, null)).isTrue();
    }

    @Test
    void testFuturePlus_AnnotationDefaults() {
        assertThat(annotation.message())
            .isEqualTo("Thời gian đặt bàn phải từ {minutes} phút trở lên so với hiện tại");
        assertThat(annotation.minutes()).isEqualTo(30);
        assertThat(annotation.groups()).isEmpty();
        assertThat(annotation.payload()).isEmpty();
    }
}

