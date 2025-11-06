package com.example.booking.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for AI auto-fill requests
 */
public class AIAutoFillRequest {
    
    @NotBlank(message = "Text content is required")
    @Size(max = 10000, message = "Text must not exceed 10000 characters")
    private String longText;
    
    // Constructors
    public AIAutoFillRequest() {}
    
    public AIAutoFillRequest(String longText) {
        this.longText = longText;
    }
    
    // Getters and Setters
    public String getLongText() {
        return longText;
    }
    
    public void setLongText(String longText) {
        this.longText = longText;
    }
}

