package com.example.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class FuturePlusValidator implements ConstraintValidator<FuturePlus, LocalDateTime> {
    
    private int minutes;
    
    @Override
    public void initialize(FuturePlus constraintAnnotation) {
        this.minutes = constraintAnnotation.minutes();
    }
    
    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        LocalDateTime minimumTime = LocalDateTime.now().plusMinutes(minutes);
        return value.isAfter(minimumTime) || value.isEqual(minimumTime);
    }
} 