package com.example.booking.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for AI text improvement requests
 */
public class AITextImprovementRequest {
    
    @NotBlank(message = "Original text is required")
    @Size(max = 5000, message = "Text must not exceed 5000 characters")
    private String originalText;
    
    @NotBlank(message = "Field name is required")
    private String fieldName;
    
    private String context; // Optional context about the field
    
    // Constructors
    public AITextImprovementRequest() {}
    
    public AITextImprovementRequest(String originalText, String fieldName, String context) {
        this.originalText = originalText;
        this.fieldName = fieldName;
        this.context = context;
    }
    
    // Getters and Setters
    public String getOriginalText() {
        return originalText;
    }
    
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
}

